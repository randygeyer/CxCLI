package com.checkmarx.cxconsole.utils;

/**
 * Created by nirli on 31/10/2017.
 */
public class ConsoleUtils {

    private ConsoleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getBuildVersion(){
        return ConsoleUtils.class.getPackage().getImplementationVersion();
    }


}