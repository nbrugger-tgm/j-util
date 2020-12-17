package com.niton.util.db;

import com.niton.util.DockerUtil;
import com.niton.util.Logging;
import com.niton.util.ScriptRunner;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.niton.util.Logging.log;

/**
 * A Utility to keep an open Database connection and automatic schema insertion
 * @param <C> the type of the connection
 */
public class DatabaseConnector<C> {
	private final  Connector<C>                        connector;
	private final DatabaseTester<C> tester;
	public         boolean                             docker            = false;
	public         C                                   db;
	private        KeepAliveService                    keepAlive;
	private final Object                              conWaiter         = new Object();
	private final java.util.function.Supplier<Reader> schemaProvider;
	private final  Function<C,Connection>              connectionSupplier;
	private final  DatabaseLinker<C>                   linker;
	private        TimeZoneAdaptation<C>                  timeZoneRegulator = new UTCTimeZoneRegulator<>();
	private final DatabaseInitializer<C> setUpFunction;
	private final DatabaseCloser<C> closer;

	public DatabaseConnector(Connector<C> connector,
	                         DatabaseTester<C> tester,
	                         Supplier<Reader> schemaProvider,
	                         Function<C, Connection> connectionSupplier,
	                         DatabaseLinker<C> linker,
	                         DatabaseInitializer<C> setUpFunction,
	                         DatabaseCloser<C> closer) {
		this.connector          = connector;
		this.tester             = tester;
		this.schemaProvider     = schemaProvider;
		this.connectionSupplier = connectionSupplier;
		this.linker             = linker;
		this.setUpFunction      = setUpFunction;
		this.closer             = closer;
	}

	public DatabaseConnector(FullDatabase<C> implementation) {
		this.connector          = implementation;
		this.tester             = implementation;
		this.schemaProvider     = implementation;
		this.connectionSupplier = implementation;
		this.linker             = implementation;
		this.setUpFunction      = implementation;
		this.closer             = implementation;
	}


	public static<C> Builder<C> create(){
		return new Builder<>();
	}

	public TimeZoneAdaptation<C> getTimeZoneRegulator() {
		return timeZoneRegulator;
	}

	public void setTimeZoneRegulator(TimeZoneAdaptation<C> timeZoneRegulator) {
		this.timeZoneRegulator = timeZoneRegulator;
	}

	private static class Builder<C> {
		private String                 host;
		private String                 user;
		private String                 password;
		private Connector<C>           connector;
		private Supplier<Reader>       schemaProvider;
		private Function<C,Connection> plainConnectorFunction;
		private DatabaseLinker<C>      linker;
		private DatabaseTester<C>      tester;
		private DatabaseInitializer<C> setUpFunction;
		private DatabaseCloser<C> closer;

		public Builder<C> connection(String host, String userName, String password){
			this.host = host;
			this.user = userName;
			this.password = password;
			return this;
		}
		public Builder<C> connector(Connector<C> c){
			this.connector = c;
			return this;
		}

		public Builder<C> connector(Supplier<Reader> c){
			this.schemaProvider = c;
			return this;
		}

		public Builder<C> connectionProvider(Function<C,Connection> c){
			this.plainConnectorFunction = c;
			return this;
		}
		public Builder<C> connectionProvider(DatabaseTester<C> tester){
			this.tester = tester;
			return this;
		}
		public Builder<C> linker(DatabaseLinker<C> c){
			this.linker = c;
			return this;
		}

		public Builder<C> setUp(DatabaseInitializer<C> c){
			this.setUpFunction = c;
			return this;
		}
		public Builder<C> closer(DatabaseCloser<C> c){
			this.closer = c;
			return this;
		}

		public DatabaseConnector<C> build(){
			DatabaseConnector<C> c =  new DatabaseConnector<>(connector,tester,schemaProvider,plainConnectorFunction,linker,setUpFunction,closer);
			c.init(host,user,password);
			return c;
		}
	}


	private synchronized void init(String host, String userName, String password){
		//Logger.getGlobal().setLevel(Level.ALL);
		if (keepAlive != null) {
			Logging.log(Logging.Level.WARNING, "Database can only be initialised once!");
			return;
		}

		docker = DockerUtil.detectDocker();
		Logging.log("-------------[" + (docker ? "DOCKER" : "NORMAL") + " MODE]--------------");

		keepAlive = new KeepAliveService(host, userName, password, this);
		keepAlive.start();
	}

	public synchronized void connect(String host, String userName, String password) throws SQLException, IOException {
		buildBaseConnection(host, userName, password);
		timeZoneRegulator.adaptTimeZone(db);
		if (!testSchema()) {
			if(!pushSchema())
				return;
		}
		linkUp();
		if (!isSetup()) {
			setUpDB();
		} else {
			Logging.log("Database is ready! Continue without setup");
		}
	}

	public boolean pushSchema() {
		Logging.log("Create Database ... (this may takes a while)");
		try {
			Reader r      = this.schemaProvider.get();
			ScriptRunner   runner = new ScriptRunner(connectionSupplier.apply(db), false, true);
			runner.runScript(r);
			Logging.log("Dump completed");
			return true;
		}catch (Exception e){
			Logging.log(Logging.Level.ERROR, "Dumping failed! Please insert the default dump file");
			Logging.log(e);
			return false;
		}
	}

	private void buildBaseConnection(String host, String userName, String password) throws SQLException {
		Logging.log("Connect to DB ...");
		db = connector.connect(host,userName, password);
		Logging.log("Connected!");
	}

	public void linkUp() {
		linker.linkUp(db);
		Logging.log("Linked to Database");
	}

	public boolean testConnection() {
		Logging.log("Test database connection");
		try {
			tester.testConnection(db);
		} catch (Exception e) {
			Logging.log(Logging.Level.WARNING, "Database is not connected");
			return false;
		}
		return true;
	}

	public boolean isSetup() {
		try {
			Logging.log("Test if database is ready");
			return tester.isReadyForUse(db);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean testSchema() {
		try {
			tester.testSchemaExistence(db);
		} catch (Exception e) {
			Logging.log(Logging.Level.WARNING, "The database does not contains the schema");
			return false;
		}
		return true;
	}

	public synchronized void setUpDB() throws IOException {
		Logging.log("Start database setup ... (adding initial data)");
		setUpFunction.addInitialData(db);
		Logging.log("Database setup done!");
		notifyConnection();
	}


	public void close() {
		closer.close(db);
		keepAlive.end();
	}

	public void notifyConnection() {
		synchronized (conWaiter) {
			conWaiter.notify();
		}
	}

	public void waitForConnection() {
		synchronized (conWaiter) {
			while (db == null) {
				Thread.yield();
				try {
					conWaiter.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean anviable() {
		return testConnection() && testSchema() && isSetup();
	}

}
