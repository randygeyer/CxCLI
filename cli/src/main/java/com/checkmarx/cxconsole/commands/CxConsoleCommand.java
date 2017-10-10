package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.utils.DynamicAuthSupplier;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

/**
 * Base class for all CLI commands.<br>
 * Defines methods for
 *
 * @author Oleksiy Mysnyk
 */
public abstract class CxConsoleCommand {

    public static final String KEY_DESCR_INTEND_SINGLE = "\t";
    public static final String KEY_DESCR_INTEND_SMALL = "\t\t";

    /*
     * Error code indicating whether command execution was successful
     */
    protected int errorCode = SCAN_SUCCEEDED_EXIT_CODE;

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
        log.debug("Command type: " + getCommandName());
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

    protected abstract void initLogging() throws IOException;

    protected abstract void releaseLog();

}
