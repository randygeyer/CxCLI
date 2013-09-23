package com.checkmarx.cxconsole;

import java.io.Console;

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
				// Console interactive mode
				Console console = System.console();
				if (console != null) {
					console.printf("Welcome to CxConsole. Enter '" + COMM_QUIT +"' to exit console.\n\n");
					console.printf("CxConsle>");
					
					// start interactive session
					String commandLine = "";					
					while (!(commandLine = console.readLine()).contains(COMM_QUIT)) {
						CxConsoleCommand command = CommandsFactory.getCommand(commandLine);
						if (command == null) {
							log.error("Command \"" + CommandsFactory.extractCommandName(commandLine) + "\" was not found. Available commands:\n" 
									+ CommandsFactory.getCommnadNames());
							errorCode = CxConsoleCommand.CODE_ERRROR;
						} else if (!command.commandAbleToRun()) {
							log.error(command.getCommandName()
									+ " command parameters are insufficient.\nSee command usage:\n\n" 
									+ command.getUsageString());
						} else {
							try {
								command.checkParameters();
							} catch (Exception e) {
								log.info("Command parameters are invalid: "
										+ e.getMessage());
								console.printf("\nCxConsle>");
								continue;
							}
							errorCode = command.execute();
						}
						console.printf("\nCxConsle>");
					}
				} else {
					log.error("Unable to open console: make sure that application " +
							"is running in console mode.");
					errorCode = CxConsoleCommand.CODE_ERRROR;
					return;
				}
			} else {
				String commandName = args[0];
                String[] argumentsLessCommandName = java.util.Arrays.copyOfRange(args,1,args.length);
				CxConsoleCommand command = CommandsFactory.getCommand(commandName,argumentsLessCommandName);
				if (command == null) {					
					log.error("Command \"" + commandName + "\" was not found. Available commands:\n" 
							+ CommandsFactory.getCommnadNames());
					errorCode = CxConsoleCommand.CODE_ERRROR;
					return;
				}

                try {
                    command.parseArguments(argumentsLessCommandName);
                } catch (ParseException e)
                {
                    log.fatal("Command parameters are invalid: " + e.getMessage());
                    command.printHelp();
                    errorCode = CxConsoleCommand.CODE_ERRROR;
                    return;
                }



				errorCode = command.execute();
			}
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
