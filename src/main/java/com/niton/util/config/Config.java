package com.niton.util.config;

import com.niton.util.DockerUtil;
import com.niton.util.Logging;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Config {
	public static UtilConfig                 utilCfg;
	public static com.typesafe.config.Config cfg;
	public static void init(String path){
		cfg = ConfigFactory.parseFile(new File(path));
		if(cfg == null)
			cfg = ConfigFactory.load();
		utilCfg = new UtilConfig(cfg);
	}

	public static Logging.Level getConsoleLoggingLevel() {
		return Logging.Level.valueOf(utilCfg.logging.console);
	}

	public static Logging.Level getFileLoggingLevel(Logging.LogContext context) {
		switch (context){
			case SECURITY:
				return Logging.Level.valueOf(utilCfg.logging.file.security);
			case FATAL:
				return Logging.Level.valueOf(utilCfg.logging.file.fatal);
			case GENERAL:
				return Logging.Level.valueOf(utilCfg.logging.file.general);
			default:
				throw new IllegalStateException("Unexpected value: " + context);
		}
	}
}
