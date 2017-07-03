package com.checkmarx.cxconsole.logging;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.*;
import java.io.File;
import java.io.IOException;

public class CxConsoleLoggerFactory {

    public static CxConsoleLoggerFactory logFactory;

    public void initLogger(Logger log, String logFilePath) {
        String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";
        String defaultLogLocation = System.getProperty("user.dir") + File.separator + "logs" + File.separator + "cx_console.log";
        if (!defaultLogLocation.equals(logFilePath)) {
            try {
                RollingFileAppender appender = new RollingFileAppender(new PatternLayout(ConfigMgr
                        .getCfgMgr().getProperty(ConfigMgr.KEY_FILE_APP_PATTERN)),
                        logFilePath, true);
                appender.setName("RA");
                appender.setThreshold(Level.TRACE);
                appender.setMaxFileSize(ConfigMgr.getCfgMgr().getProperty(
                        ConfigMgr.KEY_FILE_APP_MAX_SIZE));
                appender.setMaxBackupIndex(ConfigMgr.getCfgMgr().getIntProperty(
                        ConfigMgr.KEY_FILE_APP_MAX_ROLLS));
                appender.activateOptions();
                log.addAppender(appender);
                log.getParent().removeAppender("FA");
            } catch (IOException e) {
                log.warn("The Log path is invalid. Default path for log: " + defaultLogLocation);
            }
        }else {
        /*    try {
                PropertyConfigurator.configure(log4jConfigFile);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }*/
        }
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

