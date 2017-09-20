package com.checkmarx.cxconsole.commands;

/**
 * Created by Galn on 06/04/2017.
 */
public enum Commands {
    SCAN("Scan"),
    ASYNC_SCAN("AsyncScan"),
    OSASCAN("OsaScan"),
    ASYNC_OSA_SCAN("AsyncOsaScan");

    private String value;

    public String value() {
        return value;
    }

    Commands(String value) {
        this.value = value;
    }
}
