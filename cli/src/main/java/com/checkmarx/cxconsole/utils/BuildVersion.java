package com.checkmarx.cxconsole.utils;

/**
 * Created by Galn on 22/08/2017.
 */

public class BuildVersion {

    public static String getBuildVersion(){
        return BuildVersion.class.getPackage().getImplementationVersion();
    }

}