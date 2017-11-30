package com.checkmarx.cxconsole;

import com.checkmarx.clients.soap.login.utils.SSLUtilities;
import com.checkmarx.cxconsole.commands.CLICommand;
import com.checkmarx.cxconsole.commands.CommandFactory;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandFactoryException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ConsoleUtils;
import com.checkmarx.cxconsole.utils.CustomStringList;
import com.checkmarx.parameters.CLIScanParametersSingleton;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import static com.checkmarx.exitcodes.Constants.ExitCodes.GENERAL_ERROR_EXIT_CODE;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.exitcodes.ErrorHandler.errorMsgResolver;

/**
 * @author Oleksiy Mysnyk
 */
public class CxConsoleLauncher {

    public static final String LOG_NAME = "com.checkmarx.cxconsole.CxConsoleLauncher";

    private static final Logger log = Logger.getLogger(LOG_NAME);
    private static final String INVALID_COMMAND_PARAMETERS_MSG = "Command parameters are invalid: ";
    private static String[] argumentsLessCommandName;

    /**
     * Entry point to CxScan Console
     *
     * @param args
     */
    public static void main(String[] args) {
        int exitCode = -1;
        log.setLevel(Level.TRACE);

        exitCode = runCli(args);
        if (exitCode == SCAN_SUCCEEDED_EXIT_CODE) {
            log.info("Job completed successfully - exit code " + exitCode);
        } else {
            log.error("Failure - " + errorMsgResolver(exitCode) + " - error code " + exitCode);
        }

        System.exit(exitCode);
    }

    /**
     * Entry point to CxScan Console that returns exitCode
     * This entry point is used by Jenkins plugin
     *
     * @param args
     */
    public static int runCli(String[] args) {

        log.info("CxConsole version " + ConsoleUtils.getBuildVersion());
        log.info("CxConsole scan session started");
        log.info("");

        if (args == null || args.length == 0) {
            log.fatal("Missing command name. Available commands: " + CommandFactory.getCommandNames());
            return GENERAL_ERROR_EXIT_CODE;
        }

        validateVerboseCommand(args);
        initConfigurationManager(args);

        // Temporary solution
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        String commandName = args[0];
        argumentsLessCommandName = java.util.Arrays.copyOfRange(args, 1, args.length);
        makeArgumentsLowCase(argumentsLessCommandName);
        CLICommand command = null;
        CLIScanParametersSingleton cliScanParametersSingleton;
        try {
            CommandFactory.verifyCommand(commandName);
            cliScanParametersSingleton = CLIScanParametersSingleton.getCLIScanParameter();
            command = CommandFactory.getCommand(commandName, cliScanParametersSingleton);
            command.checkParameters();
            log.trace("Parameters were checked successfully");
        } catch (ExceptionInInitializerError | CLICommandFactoryException | CLICommandParameterValidatorException e) {
            if (e instanceof CLICommandParameterValidatorException) {
                if (command != null) {
                    command.printHelp();
                }
                log.fatal(INVALID_COMMAND_PARAMETERS_MSG + e.getMessage() + "\n");
            } else {
                log.fatal(e.getMessage());
            }
            return errorCodeResolver(e.getMessage());
        }

        int exitCode;
        try {
            exitCode = command.execute();
            log.info("CxConsole session finished");
            return exitCode;
        } catch (CLICommandException e) {
            log.error(e.getMessage());
            return errorCodeResolver(e.getMessage());
        }
    }

    private static void makeArgumentsLowCase(String[] argumentsLessCommandName) {
        for (int i = 0; i < argumentsLessCommandName.length; i++) {
            if (argumentsLessCommandName[i].startsWith("-")) {
                argumentsLessCommandName[i] = argumentsLessCommandName[i].toLowerCase();
            }
        }
    }

    private static void initConfigurationManager(String[] args) {
        int configIndx = Arrays.asList(args).indexOf("-config");
        String confPath = null;
        if (configIndx != -1 && args.length > (configIndx + 1) && args[configIndx + 1] != null && !args[configIndx + 1].startsWith("-")) {
            confPath = args[configIndx + 1];
        }
        ConfigMgr.initCfgMgr(confPath);
    }


    private static void validateVerboseCommand(String[] args) {
        ArrayList<String> customArgs = new CustomStringList(Arrays.asList(args));
        if (!customArgs.contains("-v".trim()) && !customArgs.contains("-verbose")) {
            ((AppenderSkeleton) Logger.getRootLogger().getAppender("CA"))
                    .setThreshold(Level.ERROR);
        } else {
            log.info("Verbose mode is activated. All messages and events will be sent to the console or log file.");
        }
    }

    public static String[] getArgumentsLessCommandName() {
        return argumentsLessCommandName;
    }
}