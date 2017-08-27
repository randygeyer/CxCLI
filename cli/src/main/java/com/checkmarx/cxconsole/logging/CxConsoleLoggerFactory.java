package com.checkmarx.cxconsole.logging;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;

public class CxConsoleLoggerFactory {

    public static CxConsoleLoggerFactory logFactory;

    public void initLogger(Logger log, String logFilePath) throws IOException {
        String defaultPath = System.getProperty("user.dir") + File.separator + "logs" + File.separator + "cx_console.log";
        if (!StringUtils.isEmpty(logFilePath)) {
            try {

                RollingFileAppender appender = null;

                appender = new RollingFileAppender(new PatternLayout(ConfigMgr
                        .getCfgMgr().getProperty(ConfigMgr.KEY_FILE_APP_PATTERN)),
                        logFilePath, true);

                Logger.getRootLogger().info("Log file location: " + logFilePath);

                appender.setName("RA");
                appender.setThreshold(Level.TRACE);
                appender.setMaxFileSize(ConfigMgr.getCfgMgr().getProperty(
                        ConfigMgr.KEY_FILE_APP_MAX_SIZE));
                appender.setMaxBackupIndex(ConfigMgr.getCfgMgr().getIntProperty(
                        ConfigMgr.KEY_FILE_APP_MAX_ROLLS));
                appender.activateOptions();
                log.addAppender(appender);

            } catch (Exception e) {
                log.warn("The Log path is invalid. Default path for log: " + defaultPath);
            }
        } else {
            log.info("Default path for log: " + defaultPath);
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

