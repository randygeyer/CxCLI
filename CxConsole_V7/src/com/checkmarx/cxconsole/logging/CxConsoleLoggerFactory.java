package com.checkmarx.cxconsole.logging;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.checkmarx.cxconsole.utils.ConfigMgr;

public class CxConsoleLoggerFactory {

	public static CxConsoleLoggerFactory logFactory;
	
	public void initLogger(Logger log, String logFilePath) {
		log.setLevel(Level.TRACE);
				
		//BasicConfigurator.configure();
		RollingFileAppender fileapp = null;
		try {
			fileapp = new RollingFileAppender(new PatternLayout(ConfigMgr
					.getCfgMgr().getProperty(ConfigMgr.KEY_FILE_APP_PATTERN)),
					logFilePath, true);
			fileapp.setName("RA");
			// Restrict messages level output to console by TRACE messages
			fileapp.setThreshold(Level.TRACE);
			fileapp.setMaxFileSize(ConfigMgr.getCfgMgr().getProperty(
					ConfigMgr.KEY_FILE_APP_MAX_SIZE));
			fileapp.setMaxBackupIndex(ConfigMgr.getCfgMgr().getIntProperty(
					ConfigMgr.KEY_FILE_APP_MAX_ROLLS));
			fileapp.activateOptions();
		} catch (IOException e) {
			// ignore
			Logger.getRootLogger().error("Unable to create file appender for storing log events.", e);
		}
		if (fileapp != null) {
			log.addAppender(fileapp);
		}
		
		ConsoleAppender consoleApp = new ConsoleAppender(new PatternLayout(
				ConfigMgr.getCfgMgr()
						.getProperty(ConfigMgr.KEY_CLI_APP_PATTERN)));
		// Restrict messages level output to console only by INFO messages
		consoleApp.setThreshold(Level.INFO);
		log.addAppender(consoleApp);
	}
	
	public Logger getLogger(String logFilePath) {
		Logger log = Logger.getLogger("com.checkmarx.cxconsole.commands");
		initLogger(log, logFilePath);
		return log;
	}
	
	public static CxConsoleLoggerFactory getLoggerFactory() {
		if (logFactory == null) {
			logFactory = new CxConsoleLoggerFactory();
		}
		return logFactory;
	}
}
