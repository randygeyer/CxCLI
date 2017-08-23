package com.checkmarx.cxconsole.logging;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

public class CxConsoleLoggerFactory {

    public static CxConsoleLoggerFactory logFactory;

    public void initLogger(Logger log, String logFilePath) throws IOException {
        try {

            RollingFileAppender appender= null;

            try{
            appender = new RollingFileAppender(new PatternLayout(ConfigMgr
                    .getCfgMgr().getProperty(ConfigMgr.KEY_FILE_APP_PATTERN)),
                    logFilePath, true);
            }catch (IOException ex){
                logFilePath = System.getProperty("user.dir") + File.separator + "log4j.properties";
                appender = new RollingFileAppender(new PatternLayout(ConfigMgr
                        .getCfgMgr().getProperty(ConfigMgr.KEY_FILE_APP_PATTERN)),
                        logFilePath, true);
                log.warn("The Log path is invalid. Default path for log: " + logFilePath + ". ");
            }

            Logger.getRootLogger().info("Log file location: " + logFilePath);

            appender.setName("RA");
            appender.setThreshold(Level.TRACE);
            appender.setMaxFileSize(ConfigMgr.getCfgMgr().getProperty(
                    ConfigMgr.KEY_FILE_APP_MAX_SIZE));
            appender.setMaxBackupIndex(ConfigMgr.getCfgMgr().getIntProperty(
                    ConfigMgr.KEY_FILE_APP_MAX_ROLLS));
            appender.activateOptions();
            log.addAppender(appender);

          //    PropertyConfigurator.configure(logFilePath);

        } catch (Exception e) {
            log.warn("The Log path is invalid.");
            throw e;
        }




    }

    public Logger getLogger(String logFilePath) throws IOException {
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

