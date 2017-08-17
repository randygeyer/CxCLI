package com.checkmarx.cxconsole;

import com.checkmarx.cxconsole.commands.CommandsFactory;
import com.checkmarx.cxconsole.commands.CxConsoleCommand;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.SSLUtilities;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Objects;

import static com.checkmarx.cxconsole.commands.CxConsoleCommand.CODE_ERRROR;

/**
 * @author Oleksiy Mysnyk
 */
public class CxConsoleLauncher {

    public static Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
    public static String MSG_ERR_SRV_NAME_OR_NETWORK = "Server Name is invalid or network is unavailable.";

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
        log.setLevel(Level.TRACE);
        initConfiguration(args[0], args[1]);
        runCli(args);
    }

    private static void initConfiguration(String firstArg, String secondArg) {
        String fileName = null;
        StringBuffer filePath = new StringBuffer();

        if (Objects.equals(firstArg, "-cp")) {
            String[] fileNameAndPath = secondArg.split("/");
            fileName = fileNameAndPath[fileNameAndPath.length - 1];
            for (int i = 0; i < fileNameAndPath.length - 1; i++) {
                filePath.append(fileNameAndPath[i] + "/");
            }
        }
        String path = getPropertiesFileLocation(filePath.toString(), fileName);
        ConfigMgr.createCfgMgr(path);
    }

    private static String getPropertiesFileLocation(String path, String name) {
        String propFileLocation = path;
        if (name != null) {
            name = name.replaceAll("/", "\\\\");
            name = normalizePath(name);
        }
        if (propFileLocation != null) {
            propFileLocation = propFileLocation.replaceAll("/", "\\\\");
        }

        String usrDir = System.getProperty("user.dir");

        // String usrHomeDir = "";
        if (propFileLocation == null) {
            propFileLocation = usrDir + name;
        } else {
            File propPath = new File(propFileLocation);
            if (propPath.isAbsolute()) {
                // Path is absolute
                if (propFileLocation.endsWith(File.separator)) {
                    // Directory path
                    propFileLocation = propFileLocation + name;
                } else {
                    // File path
                    if (propFileLocation.contains(File.separator)) {
                        String dirPath = propFileLocation.substring(0, propFileLocation.lastIndexOf(File.separator));
                        File propDirs = new File(dirPath);
                        if (!propDirs.exists()) {
                            propDirs.mkdirs();
                        }
                    }
                }
            } else {
                // Path is not absolute
                if (propFileLocation.endsWith(File.separator)) {
                    // Directory path
                    propFileLocation = usrDir + propFileLocation + name;
                } else {
                    // File path
                    if (propFileLocation.contains(File.separator)) {
                        String dirPath = propFileLocation.substring(0, propFileLocation.lastIndexOf(File.separator));
                        File propDirs = new File(usrDir + dirPath);
                        if (!propDirs.exists()) {
                            propDirs.mkdirs();
                        }
                    }

                    propFileLocation = usrDir + propFileLocation;
                }
            }
        }

        return propFileLocation;
    }

    private static String normalizePath(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return "";
        }

        String normalPathName = "";
        normalPathName = projectName.replace("\\", "_");
        normalPathName = normalPathName.replace("/", "\\");
        normalPathName = normalPathName.replace(":", "_");
        normalPathName = normalPathName.replace("?", "_");
        normalPathName = normalPathName.replace("*", "_");
        normalPathName = normalPathName.replace("\"", "_");
        normalPathName = normalPathName.replace("<", "_");
        normalPathName = normalPathName.replace(">", "_");
        normalPathName = normalPathName.replace("|", "_");
        normalPathName = normalPathName.replace(";", "");

        return normalPathName;
    }

    /**
     * Entry point to CxScan Console that returns exitCode
     * This entry point is used by Jenkins plugin
     *
     * @param args
     */
    public static int runCli(String[] args) {
        try {

            log.info("CxConsole version " + ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_VERSION));
            log.info("CxConsole scan session started");
            if (args == null || args.length == 0) {
                log.fatal("Missing command name. Available commands: " + CommandsFactory.getCommandNames());
                return CODE_ERRROR;
            }

            // Temporary solution
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();

            String commandName = args[2];
            String[] argumentsLessCommandName = java.util.Arrays.copyOfRange(args, 3, args.length);
            CxConsoleCommand command = CommandsFactory.getCommand(commandName);
            if (command == null) {
                log.error("Command \"" + commandName + "\" was not found. Available commands:\n"
                        + CommandsFactory.getCommandNames());
                return CODE_ERRROR;
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
                    return CODE_ERRROR;
                }
                command.checkParameters();
            } catch (ParseException e) {
                log.fatal("Command parameters are invalid: " + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERRROR;
            } catch (CommandLineArgumentException e) {
                log.fatal("Command parameters are invalid: " + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERRROR;
            } catch (Exception e) {
                log.fatal("Command parameters are invalid: " + e.getMessage() + "\n");
                command.printHelp();
                return CODE_ERRROR;
            }


            int exitCode = command.execute();
            log.info("CxConsole scan session finished");
            return exitCode;

        } catch (org.apache.commons.cli.ParseException e) {
            // Ignore, the exception is handled in above catch statement
            return CODE_ERRROR;
        } catch (Throwable e) {
            log.error("Unexpected error occurred during console session.Error message:\n" + e.getMessage());
            log.info("", e);
            return CODE_ERRROR;
        }
    }
}
