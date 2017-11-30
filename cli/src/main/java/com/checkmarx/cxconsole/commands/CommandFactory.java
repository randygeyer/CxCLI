package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.constants.Commands;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandFactoryException;
import com.checkmarx.parameters.CLIScanParametersSingleton;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nirli on 01/11/2017.
 */
public class CommandFactory {

    private CommandFactory() {
        throw new IllegalStateException("Utility class");
    }

    private static final String GENERATE_TOKEN_COMMAND = "generatetoken";
    private static final String REVOKE_TOKEN_COMMAND = "revoketoken";
    private static final String OSA_SCAN_COMMAND = "osascan";
    private static final String ASYNC_OSA_SCAN_COMMAND = "asyncosascan";
    private static final String SAST_SCAN_COMMAND = "scan";
    private static final String ASYNC_SAST_SCAN_COMMAND = "asyncscan";

    private static final String COMMANDS_LIST = GENERATE_TOKEN_COMMAND + " " + REVOKE_TOKEN_COMMAND + " " + OSA_SCAN_COMMAND + " " +
            ASYNC_OSA_SCAN_COMMAND + " " + SAST_SCAN_COMMAND + " " + ASYNC_SAST_SCAN_COMMAND + " ";

    public static CLICommand getCommand(String commandName, CLIScanParametersSingleton parameters) throws CLICommandFactoryException {
        switch (commandName.toLowerCase()) {
            case (GENERATE_TOKEN_COMMAND):
                return new GenerateTokenCommand(parameters);
            case (REVOKE_TOKEN_COMMAND):
                return new RevokeTokenCommand(parameters);
            case (OSA_SCAN_COMMAND):
                return new OSAScanCommand(parameters, false);
            case (ASYNC_OSA_SCAN_COMMAND):
                return new OSAScanCommand(parameters, true);
            case (SAST_SCAN_COMMAND):
                return new SASTScanCommand(parameters, false);
            case (ASYNC_SAST_SCAN_COMMAND):
                return new SASTScanCommand(parameters, true);
            default:
                throw new CLICommandFactoryException("Command " + commandName + " was not found. Available commands are:\n"
                        + getCommandNames());
        }
    }

    public static String getCommandNames() {
        return StringUtils.join(Commands.class.getEnumConstants(), ", ");
    }

    public static void verifyCommand(String command) throws CLICommandFactoryException {
        if (!COMMANDS_LIST.contains(command.toLowerCase())) {
            throw new CLICommandFactoryException("Command " + command + " was not found. Available commands are:\n"
                    + getCommandNames());
        }
    }
}
