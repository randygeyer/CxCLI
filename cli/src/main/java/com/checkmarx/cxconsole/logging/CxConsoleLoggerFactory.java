package com.checkmarx.cxconsole.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class CxConsoleLoggerFactory {

    public static CxConsoleLoggerFactory logFactory;

    public void initLogger() {
        String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";
        PropertyConfigurator.configure(log4jConfigFile);
    }

    public Logger getLogger() {
        Logger log = Logger.getLogger("com.checkmarx.cxconsole.commands");
        initLogger();

        return log;
    }

    public static CxConsoleLoggerFactory getLoggerFactory() {
        if (logFactory == null) {
            logFactory = new CxConsoleLoggerFactory();
        }
        return logFactory;
    }
}

