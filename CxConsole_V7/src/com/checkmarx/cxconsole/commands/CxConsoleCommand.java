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
	public CxConsoleCommand(String[] cliArgs) {
		commandLineOptions = new Options();
        //parseParameters(cliArgs);    // cli mode

	}
	
	/**
	 * Base constructor.<br>
	 * Constructs instance using single-line with arguments
	 * 
	 * @param lineArgs - parameters of application read from CLI, single line
	 */
	public CxConsoleCommand(String lineArgs) {
		parseParameters(parseLine(lineArgs)); // interactive console mode
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
        helpFormatter.printUsage(printWriter, 120, "Scan", commandLineOptions);
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
	
	/**
	 * Get set of command parameter CLI keys
	 * 
	 * @return <code>Set<String></code> which contains all command CLI keys 
	 */
	protected abstract Set<String> initCLIKeys();
	
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
	
	private Map<String, String> parseParameters(String[] args) {
		parameters = new HashMap<String, String>();
		
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				String key = args[i].trim().toUpperCase();
				String value = null;
				if ((i + 1) < args.length) {
					value = args[i + 1].trim();
				}
				if (isKeyFlag(key)) {
					parameters.put(key, "true");
				} else if (initCLIKeys().contains(key)) {
					// Check whether followed argument is not a key
					if (value != null && !initCLIKeys().contains(value.toUpperCase())) {
						parameters.put(key, value);
					} else /*if (value == null)*/ {
						parameters.put(key, null);
					}
				}
			}
		}
		
		return parameters;
	}
	
	public String[] parseLine(String args) {
		
		Set<String> cliKeys = initCLIKeys();
		String[] parsedArgs = null;
		List<String> parsedArgsTemp = new ArrayList<String>();
		
		args = args.trim();
		// Look for first space that should separate command name from arguments 
		int cmdNameIdx = args.indexOf(" ");
		if (cmdNameIdx == -1) {
			return null;
		}
		String commandName = args.substring(0, cmdNameIdx);
		if (!getCommandName().equals(commandName)) {
			return null;
		}
		
		String cmdParameters = args.substring(cmdNameIdx, args.length());
		Map<Integer, String> keysToIdMapping = new Hashtable<Integer, String>();
		int index = 0;
		for(String comandKey : cliKeys) {
			if (cmdParameters.contains(comandKey)) {
				cmdParameters = cmdParameters.replace(comandKey, "<" + index + ">");
				keysToIdMapping.put(index, comandKey);
				index++;
			}
		}
		
		cmdParameters = cmdParameters.trim();
		int placeHolerIdx = cmdParameters.indexOf('<');
		List<Integer> idsOrder = new ArrayList<Integer>();
		do {
			int nextPlaceholderIdx = cmdParameters.indexOf('>', placeHolerIdx + 1);
			if (nextPlaceholderIdx != -1) {
				String keyIdStr = cmdParameters.substring(placeHolerIdx + 1, nextPlaceholderIdx);
				try {
					idsOrder.add(Integer.parseInt(keyIdStr));
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			placeHolerIdx = cmdParameters.indexOf('<', nextPlaceholderIdx + 1);
		} while (placeHolerIdx != -1);
		
		String[] parts = cmdParameters.split("<\\d>");
		if (parts != null) {
			// Ignore first part since it'll be empty string - splitting artefact
			for (int i = 1; i < parts.length; i++) {
				Integer keyId = idsOrder.get(i - 1);
				String cliKey = keysToIdMapping.get(keyId);
				// put cli key
				parsedArgsTemp.add(cliKey);
				// put corresponding to key value
				parsedArgsTemp.add(parts[i]);
			}
		}

		if (parsedArgsTemp.size() > 0) {
			parsedArgs = new String[parsedArgsTemp.size()];
			parsedArgs = parsedArgsTemp.toArray(parsedArgs);
		}

		return parsedArgs;
	}
	
	public static String[] parseCommandLine(String cl) {
		String[] commands = null;
		
		return commands;
	}
}
