package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.logging.CxConsoleLoggerFactory;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.utils.DynamicAuthSupplier;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import static com.checkmarx.cxconsole.commands.GeneralScanCommand.PARAM_LOG_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PRJ_NAME;

/**
 * Base class for all CLI commands.<br>
 * Defines methods for
 *
 * @author Oleksiy Mysnyk
 */
public abstract class CxConsoleCommand {

    public static final String KEY_DESCR_INTEND_SINGLE = "\t";
    public static final String KEY_DESCR_INTEND_SMALL = "\t\t";
    public static final String KEY_DESCR_INTEND = "\t\t\t";
    /**
     * Error code indicating command executed successfully
     */
    public static final int CODE_OK = 0;

    /**
     * Error code indicating that error occurred during command execution
     */
    public static final int CODE_ERRROR = 1;

    /*
     * Error code indicating whether command execution was successful
     */
    protected int errorCode = CODE_OK;

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    protected Options commandLineOptions;

    /**
     * Actual command line option values after parsing the arguments
     */
    protected CommandLine commandLineArguments;

    protected Logger log;


    /**
     * Base constructor.<br>
     * Constructs object instance. Extract parameters from cliArgs
     */
    public CxConsoleCommand() {
        commandLineOptions = new Options();
    }

    public void parseArguments(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        commandLineArguments = parser.parse(commandLineOptions, args, true);
    }

    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        String header = "\nThe \"Scan\" command allows to scan new and existing projects. It accepts all project settings as an arguments, similar to Web interface.";
        String footer = "\n(c) 2014 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.setLeftPadding(4);
        helpFormatter.printHelp(120, getCommandName(), header, commandLineOptions, footer, true);

    }

    public int execute() throws Exception {
        initLogging();
        printCommandsDebug();
        try {
            executeCommand();
            return getErrorCode();
        } finally {
            releaseLog();
        }
    }

    /**
     * Command specific operations. Should be implemented by every
     * complete executable command.
     */
    protected abstract void executeCommand();

    public abstract void checkParameters() throws CommandLineArgumentException;

    public abstract void resolveServerUrl() throws Exception;

    /**
     * Check whether provided key is flag - i.e. it doesn't have followed
     * value in CLI (like "-verbose" flag)
     *
     * @return true if current key is a flag
     */
    protected abstract boolean isKeyFlag(String key);

    private void printCommandsDebug() {
        log.debug("----------------------------Configured Commands:-----------------------------");
        for (Option opt : commandLineArguments.getOptions()) {
            String option = opt.getOpt();
            if (option != "CxPassword") {
                log.debug("Option: " + opt.getOpt() + " value: " + opt.getValue());
            }
        }
        log.debug("-----------------------------------------------------------------------------");
    }

    public void initKerberos() {
        final boolean isUsingKerberos = "true".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH));
        if (isUsingKerberos) {
            System.setProperty("java.security.auth.login.config", System.class.getResource("/login.conf").toString());
            System.setProperty("java.security.krb5.conf", System.getProperty("user.dir") + "/config/krb5.conf");
            //System.setProperty("sun.security.krb5.debug", "false");
            System.setProperty("auth.spnego.requireCredDelegation", "true");

            final String username = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_KERBEROS_USERNAME);
            System.setProperty("cxf.kerberos.username", username);
            final String password = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_KERBEROS_PASSWORD);
            System.setProperty("cxf.kerberos.password", password);

        }
        DynamicAuthSupplier.setKerberosActive(isUsingKerberos);
    }


    /*
     * Usage string building methods
     */
    public abstract String getDescriptionString();

    public abstract String getCommandName();

    public abstract String getMandatoryParams();

    public abstract String getOptionalParams();

    public abstract String getKeyDescriptions();

    public abstract String getOptionalKeyDescriptions();

    public abstract String getUsageExamples();

    public int getErrorCode() {
        return errorCode;
    }

    protected void initLogging() {
        if (commandLineArguments.hasOption(PARAM_LOG_FILE.getOpt())) {
            log = CxConsoleLoggerFactory.getLoggerFactory().getLogger(getLogFileLocation());
        } else {
            log = Logger.getLogger("com.checkmarx.cxconsole.commands");
            log.setLevel(Level.ERROR);
        }
    }

    protected abstract void releaseLog();

    /**
     * Method defining log file location.
     *
     * @return <code>String</code> - log file location
     */
    protected String getLogFileLocation() {
        String logFileLocation = commandLineArguments.getOptionValue(PARAM_LOG_FILE.getOpt());
        String projectName = commandLineArguments.getOptionValue(PARAM_PRJ_NAME.getOpt());
        if (projectName != null) {
            projectName = projectName.replaceAll("/", "\\\\");
        }
        // String usrHomeDir = System.getProperty("user.home");
        // CxLogger.getLogger().info("Log user dir: " +
        // System.getProperty("user.dir"));

        String[] parts = projectName.split("\\\\");
        String usrDir = System.getProperty("user.dir") + File.separator + normalizeLogPath(parts[parts.length - 1]) + File.separator;

        // String usrHomeDir = "";
        if (logFileLocation == null) {
            logFileLocation = usrDir + normalizeLogPath(parts[parts.length - 1]) + ".log";
        } else {
            File logpath = new File(logFileLocation);
            if (logpath.isAbsolute()) {
                // Path is absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }
                }
            } else {
                // Path is not absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = usrDir + logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(usrDir + dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }

                    logFileLocation = usrDir + logFileLocation;
                }
            }
        }

        return logFileLocation;
    }

    private String normalizeLogPath(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return "cx_scan.log";
        }

        String normalPathName = "";
        normalPathName = projectName.replace("\\", "_");
        normalPathName = normalPathName.replace("/", "_");
        normalPathName = normalPathName.replace(":", "_");
        normalPathName = normalPathName.replace("?", "_");
        normalPathName = normalPathName.replace("*", "_");
        normalPathName = normalPathName.replace("\"", "_");
        normalPathName = normalPathName.replace("<", "_");
        normalPathName = normalPathName.replace(">", "_");
        normalPathName = normalPathName.replace("|", "_");
        return normalPathName;
    }

}
