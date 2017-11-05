package com.checkmarx.cxconsole.utils;

/**
 * Created by nirli on 31/10/2017.
 */
public class CommandUtils {

    private CommandUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getBuildVersion(){
        return CommandUtils.class.getPackage().getImplementationVersion();
    }


}