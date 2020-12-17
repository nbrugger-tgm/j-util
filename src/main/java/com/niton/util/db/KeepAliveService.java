package com.niton.util.db;

import com.niton.util.Database;
import com.niton.util.Logging;
import com.niton.util.config.Config;

public class KeepAliveService extends Thread {
    private static int instances = 0;

    private final String password;
    private final String url;
    private final String userName;
    private final DatabaseConnector<?> db;

    public KeepAliveService(String url,
                            String userName,
                            String password,
                            DatabaseConnector<?> db) {
        super("keep-alive");
        this.db = db;
        instances ++;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }
    public boolean running = true;
    @Override
    public synchronized void run() {
        if(instances > 1){
            Logging.log(Logging.Level.WARNING,"There should only be one instance of Keep Alive Service");
            return;
        }
        while (running) {
            try {
                if (!db.testConnection())
                    db.connect(url, userName, password);
                else {
                    if (!db.testSchema()) {
                        if (db.pushSchema())
                            if (!db.isSetup()) {
                                db.linkUp();
                                db.setUpDB();
                            }
                    } else if (!db.isSetup()) {
                        db.linkUp();
                        db.setUpDB();
                    }
                }
            } catch (Exception e) {
                Logging.log(Logging.Level.EXCEPTION, "Database Reconnecting failed!");
                Logging.log(Logging.Level.EXCEPTION,e);
            }
            try {
                Thread.sleep(Config.cfg.general.db_check_interval *1000);
            } catch (InterruptedException e) {
            }
        }
    }
    public void end(){
        running = false;
    }
}
