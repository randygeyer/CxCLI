package com.checkmarx.cxconsole.commands;


import org.apache.commons.lang3.StringUtils;

import static com.checkmarx.cxconsole.commands.Commands.*;

/**
 * @author Oleksiy Mysnyk
 */
public class CommandsFactory {

    public static CxConsoleCommand getCommand(String commandName) {
        if (Commands.SCAN.value().equals(commandName)) {
            return new ScanCommand();
        } else if (Commands.OSASCAN.value().equals(commandName)) {
            return new OsaScanCommand();
        }
       return null;
    }

    public static String getCommandNames() {
        return StringUtils.join(Commands.class.getEnumConstants(), ", ");
    }
}
