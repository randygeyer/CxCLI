package com.checkmarx.cxconsole.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

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
	 * Command parameters stored in map by keys corresponding to CLI parameters 
	 * keys/flags
	 */
	protected Map<String, String> parameters;    //TODO: Remove

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
	 * 
	 * @param cliArgs - parameters of application read from CLI
	 */
	public CxConsoleCommand() {
		commandLineOptions = new Options();
	}

	
	/**
	 * Check whether current command is being fulfilled with all parameters 
	 * required for execution
	 * 
	 * @return <code>true</code> if command can be executed with current 
	 * parameters set, <code>false</code> otherwise
	 */
	public abstract boolean commandAbleToRun();  //TODO: Remove

    public void parseArguments(String[] args) throws ParseException
    {
        CommandLineParser parser = new BasicParser();
        commandLineArguments = parser.parse(commandLineOptions, args,true);
    }

    public void printHelp()
    {
        HelpFormatter helpFormatter = new HelpFormatter();
        PrintWriter printWriter = new PrintWriter(System.out,true);
        helpFormatter.printUsage(printWriter, 120, getCommandName(), commandLineOptions);
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
	
	public abstract void checkParameters() throws Exception;
	
	/**
	 * Check whether provided key is flag - i.e. it doesn't have followed 
	 * value in CLI (like "-verbose" flag)
	 * 
	 * @return true if current key is a flag
	 */
	protected abstract boolean isKeyFlag(String key);
	

	
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
	
	/**
	 * Builds usage string for help tips.
	 * To build usage string correctly command should implement correctly next methods:
	 * <ul>
	 * <li><code>getCommandName()</code></li>
	 * <li><code>getMandatoryParams()</code></li>
	 * <li><code>getOptionalParams()</code></li>
	 * <li><code>getKeyDescriptions()</code></li>
	 * <li><code>getUsageExamples()</code></li>
	 * </ul>
	 * @return command usage string
	 */
	public String getUsageString() {
		StringBuilder usage = new StringBuilder(getCommandName());
		usage.append("\n");
		usage.append(getDescriptionString());
		usage.append("\n\n");
		usage.append(getCommandName());
		usage.append(" ");
		usage.append(getMandatoryParams());
		usage.append(" ");
		usage.append(getOptionalParams());
		usage.append("\n\nKeys:\n");
		usage.append(getKeyDescriptions());
		usage.append(getOptionalKeyDescriptions());
		String examples = getUsageExamples();
		if (!examples.isEmpty()) {
			usage.append("\n\nUsage examples:");
			usage.append(getUsageExamples());
		}
		
		return usage.toString();
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	protected abstract void initLogging();
	protected abstract void releaseLog();
	
}
