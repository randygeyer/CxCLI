package com.checkmarx.cxconsole;

import com.checkmarx.cxconsole.commands.CommandsFactory;
import com.checkmarx.cxconsole.commands.CxConsoleCommand;
import com.checkmarx.cxconsole.utils.BuildVersion;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.CustomStringList;
import com.checkmarx.cxviewer.ws.SSLUtilities;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import static com.checkmarx.cxconsole.commands.CxConsoleCommand.CODE_ERROR;

/**
 * @author Oleksiy Mysnyk
 */
public class CxConsoleLauncher {

    public static Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");

    private static final String MSG_ERR_SRV_NAME_OR_NETWORK = "Server Name is invalid or network is unavailable.";
    private static final String INVALID_COMMAND_PARAMETERS_MSG = "Command parameters are invalid: ";
    private static final int SCAN_SUCCEEDED = 0;

    /**
     * CxConsole commands
     */
    public static String COMM_CONNECT = "connect";
    public static String COMM_QUIT = "quit";


    /**
     * Entry point to CxScan Console
     *
     * @param args
     */

    public static void main(String[] args) {
        int exitCode = -1;
        log.setLevel(Level.TRACE);
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                log.error("Failure - batch job terminated - error code " + 130);
//            }
//        });

        exitCode = runCli(args);
        if (exitCode == SCAN_SUCCEEDED) {
            log.info("Scan completed successfully - exit code " + exitCode);
        } else {
            log.error("Failure - {error description - to be added} - error code " + exitCode);
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
        try {

            log.info("CxConsole version " + BuildVersion.getBuildVersion());
            log.info("CxConsole scan session started");
            log.info("");

            if (args == null || args.length == 0) {
                log.fatal("Missing command name. Available commands: " + CommandsFactory.getCommandNames());
                return CODE_ERROR;
            }

            ArrayList<String> customArgs = new CustomStringList(Arrays.asList(args));

            if (!customArgs.contains("-v".trim()) && !customArgs.contains("-verbose")) {
                ((AppenderSkeleton) Logger.getRootLogger().getAppender("CA"))
                        .setThreshold(Level.ERROR);
            }


            int configIndx = Arrays.asList(args).indexOf("-config");
            String confPath = null;
            if (configIndx != -1 && args.length > (configIndx + 1) && args[configIndx + 1] != null && !args[configIndx + 1].startsWith("-")) {
                confPath = args[configIndx + 1];
            }
            ConfigMgr.initCfgMgr(confPath);

            // Temporary solution
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();

            String commandName = args[0];
            String[] argumentsLessCommandName = java.util.Arrays.copyOfRange(args, 1, args.length);
            CxConsoleCommand command = CommandsFactory.getCommand(commandName);
            if (command == null) {
                log.error("Command \"" + commandName + "\" was not found. Available commands:\n"
                        + CommandsFactory.getCommandNames());
                return CODE_ERROR;
            }

            try {
                command.parseArguments(argumentsLessCommandName);

                try {
                    command.initKerberos();
                    command.resolveServerUrl();
                } catch (Exception e) {
                    log.trace("", e);
                    log.fatal(MSG_ERR_SRV_NAME_OR_NETWORK + " Error message: " + e.getMessage() + "\n");
                    command.printHelp();
                    return CODE_ERROR;
                }
                command.checkParameters();
            } catch (ParseException e) {
                log.fatal(INVALID_COMMAND_PARAMETERS_MSG + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERROR;
            } catch (CommandLineArgumentException e) {
                log.fatal(INVALID_COMMAND_PARAMETERS_MSG + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERROR;
            } catch (Exception e) {
                log.fatal(INVALID_COMMAND_PARAMETERS_MSG + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERROR;
            }


            int exitCode = command.execute();
            log.info("CxConsole scan session finished");
            return exitCode;

        } catch (org.apache.commons.cli.ParseException e) {
            // Ignore, the exception is handled in above catch statement
            return CODE_ERROR;
        } catch (Throwable e) {
            log.error("Unexpected error occurred during console session.Error message:\n" + e.getMessage());
            log.info("", e);
            return CODE_ERROR;
        }
    }
}
