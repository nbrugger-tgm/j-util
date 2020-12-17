package com.niton.util.config;

import com.niton.util.DockerUtil;
import com.niton.util.Logging;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Config {
	public static UtilConfig                 cfg;
	public static com.typesafe.config.Config plainCfg;
	public static void init(){
		if(!DockerUtil.detectDocker()){
			Logging.log(Logging.Level.WARNING,"Configuration files are only useable with docker! Fall back to default config");
			return;
		}
		cfg = new UtilConfig(plainCfg = ConfigFactory.parseFile(new File("/app/config.cfg")));
	}

	public static Logging.Level getConsoleLoggingLevel() {
		return Logging.Level.valueOf(cfg.logging.console);
	}

	public static Logging.Level getFileLoggingLevel(Logging.LogContext context) {
		switch (context){
			case SECURITY:
				return Logging.Level.valueOf(cfg.logging.file.security);
			case FATAL:
				return Logging.Level.valueOf(cfg.logging.file.fatal);
			case GENERAL:
				return Logging.Level.valueOf(cfg.logging.file.general);
			default:
				throw new IllegalStateException("Unexpected value: " + context);
		}
	}
}
