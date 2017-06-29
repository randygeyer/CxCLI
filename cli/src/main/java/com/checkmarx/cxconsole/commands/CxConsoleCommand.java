package com.checkmarx.cxconsole.commands;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.utils.DynamicAuthSupplier;

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


    public void parseArguments(String[] args, Logger log) throws ParseException
    {
        CommandLineParser parser = new BasicParser();
        commandLineArguments = parser.parse(commandLineOptions, args,true);
    }

    public void printHelp()
    {
        HelpFormatter helpFormatter = new HelpFormatter();
        String header = "\nThe \"Scan\" command allows to scan new and existing projects. It accepts all project settings as an arguments, similar to Web interface.";
        String footer = "\n(c) 2014 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.setLeftPadding(4);
        helpFormatter.printHelp(120,getCommandName(),header,commandLineOptions,footer,true);

    }
	
	public int execute() throws Exception {
		initLogging();
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

    public void initKerberos()
    {
        final boolean isUsingKerberos = "true".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH));
        if (isUsingKerberos)
        {
            System.setProperty("java.security.auth.login.config", System.class.getResource("/login.conf").toString());
            System.setProperty("java.security.krb5.conf",System.getProperty("user.dir") + "/config/krb5.conf");
            //System.setProperty("sun.security.krb5.debug", "false");
            System.setProperty("auth.spnego.requireCredDelegation", "true");

            final String username = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_KERBEROS_USERNAME);
            System.setProperty("cxf.kerberos.username",username);
            final String password = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_KERBEROS_PASSWORD);
            System.setProperty("cxf.kerberos.password",password);

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
	
	protected abstract void initLogging();
	protected abstract void releaseLog();
	
}
