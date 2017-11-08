package com.checkmarx.cxconsole.logger;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

public class CxConsoleLoggerFactory {

    private static CxConsoleLoggerFactory logFactory;

    private void initLogger(Logger log, String logFilePath) throws IOException {
        String defaultPath = System.getProperty("user.dir") + File.separator + "logs" + File.separator + "cx_console.log";
        if (!StringUtils.isEmpty(logFilePath)) {
            try {
                RollingFileAppender appender;
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
        Logger log = Logger.getLogger(LOG_NAME);
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

