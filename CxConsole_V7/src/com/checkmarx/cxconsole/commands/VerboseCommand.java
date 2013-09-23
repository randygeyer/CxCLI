package com.checkmarx.cxconsole.commands;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.checkmarx.cxconsole.logging.CxConsoleLoggerFactory;

/**
 * 
 * @author Oleksiy Mysnyk
 */
public abstract class VerboseCommand extends CxConsoleCommand {


	/**
	 * CLI flag which switch verbose mode
	 */
	public static final String PARAM_VERBOSE_FULL = "-verbose";
	
	/**
	 * CLI flag which switch verbose mode. Short form
	 */
	public static final String PARAM_VERBOSE_SHORT = "-v";

    public static final Option PARAM_VERBOSE = OptionBuilder.withDescription("Turn on verbose mode. All messages and events will be logged to console/log file. Optional.").withLongOpt("verbose").create("v");


	/**
	 * @param cliArgs
	 */
	public VerboseCommand(String[] cliArgs) {
		super(cliArgs);   // cli mode
        this.commandLineOptions.addOption(PARAM_VERBOSE);
	}
	
	
	public VerboseCommand(String cliArgs) {
		super(cliArgs); // interactive console mode
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#isKeyFlag(java.lang.String)
	 */
	@Override
	protected boolean isKeyFlag(String key) {
		return PARAM_VERBOSE_FULL.equals(key) || PARAM_VERBOSE_SHORT.equals(key);
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#getCLIKeys()
	 */
	@Override
	protected Set<String> initCLIKeys() {
		Set<String> cliKeysSet = new HashSet<String>();
		cliKeysSet.add(PARAM_VERBOSE_FULL.toUpperCase());
		cliKeysSet.add(PARAM_VERBOSE_SHORT.toUpperCase());
		return cliKeysSet;
	}

	public String getOptionalParams() {
		return "[ " + PARAM_VERBOSE_FULL + " | " + PARAM_VERBOSE_SHORT + " ]";
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#initLogging()
	 */
	@Override
	protected void initLogging() {
		if (parameters.containsKey(PARAM_VERBOSE_FULL.toUpperCase()) || parameters.containsKey(PARAM_VERBOSE_SHORT.toUpperCase())) {
			log = CxConsoleLoggerFactory.getLoggerFactory().getLogger(getLogFileLocation());
		} else {
			log = Logger.getLogger("com.checkmarx.cxconsole.commands");
			log.setLevel(Level.ERROR);
		}
	}
	
	@Override
	protected void releaseLog() {
		log.removeAllAppenders();
		//LogManager.shutdown();
	}
	
	@Override
	public String getOptionalKeyDescriptions() {
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);
		keys.append(PARAM_VERBOSE_FULL);
		keys.append(" | ");
		keys.append(PARAM_VERBOSE_SHORT);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Turn on verbose mode. All messages and events will be logged\n" +
				KEY_DESCR_INTEND + "  to console/log file. Optional.\n");
		
		return keys.toString();
	}
	
	/**
	 * Method defining log file location. All ancestors should implement.
	 *  
	 * @return <code>String</code> - log file location
	 */
	protected abstract String getLogFileLocation();

}
