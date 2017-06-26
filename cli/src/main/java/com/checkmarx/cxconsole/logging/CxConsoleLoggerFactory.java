package com.checkmarx.cxconsole.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class CxConsoleLoggerFactory {

    public static CxConsoleLoggerFactory logFactory;

    public void initLogger( Logger log) {
       String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";
        try {
            PropertyConfigurator.configure(log4jConfigFile);
        }catch (Exception e){
            log.warn(e.getMessage());
        }
    }

    public Logger getLogger() {
        Logger log = Logger.getLogger("com.checkmarx.cxconsole.commands");
        initLogger(log);

        return log;
    }

    public static CxConsoleLoggerFactory getLoggerFactory() {
        if (logFactory == null) {
            logFactory = new CxConsoleLoggerFactory();
        }
        return logFactory;
    }
}

