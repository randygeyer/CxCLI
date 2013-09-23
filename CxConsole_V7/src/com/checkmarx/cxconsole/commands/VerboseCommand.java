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


    public static final Option PARAM_VERBOSE = OptionBuilder.withDescription("Turns on verbose mode. All messages and events will be sent to the console/log file.").withLongOpt("verbose").create("v");


	/**
	 * @param cliArgs
	 */
	public VerboseCommand() {
		super();   // cli mode
        this.commandLineOptions.addOption(PARAM_VERBOSE);
	}


	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#initLogging()
	 */
	@Override
	protected void initLogging() {
		if (commandLineArguments.hasOption(PARAM_VERBOSE.getOpt())) {
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
	

	
	/**
	 * Method defining log file location. All ancestors should implement.
	 *  
	 * @return <code>String</code> - log file location
	 */
	protected abstract String getLogFileLocation();

}
