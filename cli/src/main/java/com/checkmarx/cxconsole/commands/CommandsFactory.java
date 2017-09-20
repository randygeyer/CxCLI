package com.checkmarx.cxconsole.commands;


import org.apache.commons.lang3.StringUtils;

/**
 * @author Oleksiy Mysnyk
 */
public class CommandsFactory {

    public static CxConsoleCommand getCommand(String commandName) {
        if (Commands.SCAN.value().equalsIgnoreCase(commandName)) {
            return new ScanCommand(false);
        } else if (Commands.ASYNC_SCAN.value().equalsIgnoreCase(commandName)) {
            return new ScanCommand(true);
        } else if (Commands.OSASCAN.value().equalsIgnoreCase(commandName)) {
            return new OsaScanCommand(false);
        } else if (Commands.ASYNC_OSA_SCAN.value().equalsIgnoreCase(commandName)) {
            return new OsaScanCommand(true);
        }
        return null;
    }

    public static String getCommandNames() {
        return StringUtils.join(Commands.class.getEnumConstants(), ", ");
    }
}
