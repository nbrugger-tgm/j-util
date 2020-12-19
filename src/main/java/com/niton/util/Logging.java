package com.niton.util;

import com.niton.util.config.Config;
import com.niton.util.config.UtilConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Random;

import static com.niton.util.Logging.Level.*;

public class Logging {
	private static       Path               logFolder;
	private static       String             module;
	private static final char[]             idChars   = "abcdefghijklmnorstuxz123456789".toCharArray();
	private static final LinkedList<String> usedIDs   = new LinkedList<>();
	private static       boolean            inited    = false;
	private static       boolean            activated = false;


	public static void init(String module,String path){
		if(inited){
			log(WARNING,Logging.class," init() methods are only allowed to be called once in a runtime");
			return;
		}
		Logging.module = module;

		logFolder = Paths.get(path);

		inited = true;
	}
	public static void activate(){
		activated = true;
	}


	public static void log(Level level,LogContext context,Class<?> loggingClass, String message) {
		if(!inited || !activated){
			return;
		}
		UtilConfig.Logging cfg = Config.utilCfg.logging;

		if(level == null)
			level = INFO;
		if(context == null)
			context = LogContext.GENERAL;


		if(Config.getConsoleLoggingLevel().index <= level.index){
			String out = buildMsg(cfg, false, context, message, loggingClass, level);
			if(level.index >= 3)
				System.err.print(out);
			else
				System.out.print(out);
		}
		if(Config.getFileLoggingLevel(context).index <= level.index){
			String out = buildMsg(cfg, true, context, message, loggingClass, level);
			try {
				Path file = logFolder.resolve(getDay()).resolve(context.name+ ".log");
				if(!Files.exists(file)) {
					Files.createDirectories(file.getParent());
					Files.createFile(file);
				}
				Files.write(file, out.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				System.err.println("[LOGGING-ERROR]");
				e.printStackTrace();
				System.err.println("Unprintable message was : "+out);
			}
		}
	}

	private static String buildMsg(UtilConfig.Logging cfg, boolean file, LogContext context, String message, Class<?> loggingClass, Level level) {
		String out = "";
		if((file ? cfg.options.file.date : cfg.options.console.date) || (file ? cfg.options.file.time : cfg.options.console.time))
			out += getDate(file);
		if(file ? cfg.options.file.module : cfg.options.console.module)
			out += "{"+module+"} ";
		if((file ? cfg.options.file.class_ : cfg.options.console.class_) && loggingClass != null) {
			if (file ? cfg.options.file.package_ : cfg.options.console.package_)
				out += loggingClass.getPackage() + ".";
			out+= loggingClass.getSimpleName();
		}
		if(file ? cfg.options.file.level : cfg.options.console.level)
			out += "["+level+"]";
		out += " > "+message;
		if(file ? cfg.options.file.context : cfg.options.console.context){
			out += " ("+context.name()+")";
		}
		out += '\n' ;
		return  out;
	}

	private static String getDay() {
		Calendar d = GregorianCalendar.getInstance();
		int
				day = d.get(Calendar.DAY_OF_MONTH),
				m = d.get(Calendar.MONTH)+1,
				y = d.get(Calendar.YEAR);
		return day+"."+m+"."+y;
	}

	public static String getDate(boolean file){
		Calendar d = GregorianCalendar.getInstance();
		int
				day = d.get(Calendar.DAY_OF_MONTH),
				m = d.get(Calendar.MONTH)+1,
				y = d.get(Calendar.YEAR),
				hr = d.get(Calendar.HOUR_OF_DAY),
				min = d.get(Calendar.MINUTE),
				s = d.get(Calendar.SECOND);

		boolean time = file ? Config.utilCfg.logging.options.file.time : Config.utilCfg.logging.options.console.time;
		boolean date =file ? Config.utilCfg.logging.options.file.date : Config.utilCfg.logging.options.console.date;
		String out = "(";
		if(date)
			out += (day<10 ? "0" : "")+day+"."+(m<10 ? "0" : "")+m+"."+(y<10 ? "0" : "")+y;
		if(date && time)
			out += " ";
		if(time)
			out += (hr<10 ? "0" : "")+hr+":"+(min<10 ? "0" : "")+min+((file ? Config.utilCfg.logging.options.file.seconds : Config.utilCfg.logging.options.console.seconds) ? ":"+(s<10 ? "0"+s : s) : "");
		out += ")";
		return(out);
	}
 	public static void log(LogContext context,Class<?> loggingClass, String message) {
		log(null, context, loggingClass, message);
	}
	public static void log(Level level,Class<?> loggingClass, String message) {
		log(level, null, loggingClass, message);
	}
	public static void log(Level level,LogContext context, String message) {
		log(level, context, null, message);
	}
	public static void log(Level level, String message) {
		log(level, null, null, message);
	}
	public static void log(LogContext context, String message) {
		log(null, context, null, message);}
	public static void log(String message) {
		log(INFO, LogContext.GENERAL, null, message);
	}
	public static void log(Level l,Exception e) {
		if(!inited)
			return;
		log(l, "An "+e.getClass().getTypeName()+" occured!");
		try {
			String id = newID();
			Path exceptionFile;
			do {
				exceptionFile = logFolder.resolve("exceptions").resolve(id + ".log");

			}while(Files.exists(exceptionFile));

			Files.createDirectories(exceptionFile.getParent());
			Files.createFile(exceptionFile);

			PrintWriter fileStream = new PrintWriter(exceptionFile.toFile());
			e.printStackTrace(fileStream);
			fileStream.flush();
			fileStream.close();
			log(l,"Exception trace saved at 'exceptions/"+id+".log");
		}catch (IOException fex){
			log(EXCEPTION, "Unable to print exception to file ("+fex.getClass().getSimpleName()+": "+fex.getMessage()+")");
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			log(l, writer.toString());
		}
	}

	public static String newID(){
		Random r = new Random();
		StringBuilder bld;
		do{
			bld = new StringBuilder();
			for (int i = 0; i <Config.utilCfg.logging.exception_id_size; i++){
				bld.append(idChars[r.nextInt(idChars.length)]);
			}
		}while (usedIDs.contains(bld.toString()));
		return bld.toString();
	}

	public static void log(Exception e) {
		log(EXCEPTION, e);
	}

	public enum Level {
		NONE(Integer.MAX_VALUE),
		ERROR(4),
		EXCEPTION(3),
		WARNING(2),
		INFO(1),
		DEBUG(-1);

		private final int index;

		Level(int i) {
			this.index = i;
		}

		public int getIndex() {
			return index;
		}
	}
	public enum LogContext {
		SECURITY("security"),
		FATAL("fatal"),
		GENERAL("common");

		private final String name;

		LogContext(String common) {
			this.name = common;
		}

		public String getName() {
			return name;
		}
	}
}
