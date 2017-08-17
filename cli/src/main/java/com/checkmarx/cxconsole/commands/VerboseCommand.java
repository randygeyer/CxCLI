package com.checkmarx.cxconsole.commands;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * @author Oleksiy Mysnyk
 */
public abstract class VerboseCommand extends CxConsoleCommand {


    public static final Option PARAM_VERBOSE = OptionBuilder.withDescription("Turns on verbose mode. All messages and events will be sent to the console/log file.  Optional.").withLongOpt("verbose").create("v");

    public VerboseCommand() {
        super();
        this.commandLineOptions.addOption(PARAM_VERBOSE);
    }

    @Override
    protected void releaseLog() {
        log.removeAllAppenders();
        //LogManager.shutdown();
    }
}
