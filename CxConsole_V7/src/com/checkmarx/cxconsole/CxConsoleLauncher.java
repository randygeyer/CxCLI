package com.checkmarx.cxconsole;

import java.io.Console;

import com.checkmarx.cxconsole.commands.ScanCommand;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.checkmarx.cxconsole.commands.CommandsFactory;
import com.checkmarx.cxconsole.commands.CxConsoleCommand;
import com.checkmarx.cxconsole.utils.ConfigMgr;

/**
 * @author Oleksiy Mysnyk
 *
 */
public class CxConsoleLauncher {
	
	public static Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
	
	/**
	 * CxConsole commands
	 */
	public static String COMM_CONNECT = "connect";
	public static String COMM_QUIT = "quit";
	
	
	/**
	 * Entry point to CxScan Console
	 * @param args
	 */
	public static void main(String[] args) {
		int errorCode = CxConsoleCommand.CODE_OK;
		
		try {
			log.info("CxConsole version " + ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_VERSION));
			log.info("CxConsole scan session started");
			if (args == null || args.length == 0) {
                log.fatal("No command line arguments were specified");
                new ScanCommand().printHelp();
                errorCode = CxConsoleCommand.CODE_ERRROR;
                return;

			}
            String commandName = args[0];
            String[] argumentsLessCommandName = java.util.Arrays.copyOfRange(args,1,args.length);
            CxConsoleCommand command = CommandsFactory.getCommand(commandName);
            if (command == null) {
                log.error("Command \"" + commandName + "\" was not found. Available commands:\n"
                        + CommandsFactory.getCommnadNames());
                errorCode = CxConsoleCommand.CODE_ERRROR;
                return;
            }

            try {
                command.parseArguments(argumentsLessCommandName);

                command.checkParameters();
            } catch (ParseException e)
            {
                log.fatal("Command parameters are invalid: " + e.getMessage());
                command.printHelp();
                errorCode = CxConsoleCommand.CODE_ERRROR;
                return;
            }



            errorCode = command.execute();

		}
        catch (org.apache.commons.cli.ParseException e)
        {

        } catch (Throwable e) {
			log.fatal("Unexpected error occurred during console session.Error message:\n" + e.getMessage());
			log.info("", e);
			errorCode = CxConsoleCommand.CODE_ERRROR;
		} finally {			
			log.info("CxConsole scan session finished");
			log.info("");
			System.exit(errorCode);
		}
	}
}
