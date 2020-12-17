package com.niton.util.config;

import com.niton.util.DockerUtil;
import com.niton.util.Logging;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Config {
	public static UtilConfig                 utilCfg;
	public static com.typesafe.config.Config cfg;
	public static void init(String path){
		if(!DockerUtil.isDocker()){
			Logging.log(Logging.Level.WARNING,"Configuration files are only useable with docker! Fall back to default config");
			return;
		}
		utilCfg = new UtilConfig(cfg = ConfigFactory.parseFile(new File(path)));
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
