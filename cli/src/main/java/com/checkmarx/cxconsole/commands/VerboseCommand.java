package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.logging.CxConsoleLoggerFactory;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import java.io.IOException;

/**
 * @author Oleksiy Mysnyk
 */
public abstract class VerboseCommand extends CxConsoleCommand {


    public static final Option PARAM_VERBOSE = OptionBuilder.withDescription("Turns on verbose mode. All messages and events will be sent to the console/log file.  Optional.").withLongOpt("verbose").create("v");
    public static final Option PARAM_LOG_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Log file. Optional.").create("Log");


    public VerboseCommand() {
        super();
        this.commandLineOptions.addOption(PARAM_VERBOSE);
    }


    /* (non-Javadoc)
     * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#initLogging()
     */
    @Override
    protected void initLogging() throws IOException {

        String logPath = "";
        if (commandLineArguments.hasOption(PARAM_LOG_FILE.getOpt())) {
            logPath = getLogFileLocation();
        }

        log = CxConsoleLoggerFactory.getLoggerFactory().getLogger(logPath);
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
