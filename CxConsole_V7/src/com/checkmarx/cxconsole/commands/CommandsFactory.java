package com.checkmarx.cxconsole.commands;

/**
 * 
 * @author Oleksiy Mysnyk
 *
 */
public class CommandsFactory {

	public static CxConsoleCommand getCommand(String commandName, String[] args) {
		return getCommand(commandName, args, null);
	}
	
	public static CxConsoleCommand getCommand(String cliLine) {
		String commName = extractCommandName(cliLine);
		return getCommand(commName, null, cliLine);
	}
	
	private static CxConsoleCommand getCommand(String commandName, String[] args, String cli) {
		if (ScanCommand.COMMAND_SCAN.equals(commandName)) {
			if (args != null) {
				return new ScanCommand(args);
			} else if (cli != null) {
				return new ScanCommand(cli);
			}
		}/* else if (SomeOtherCommand.OTHER_COMMAND_NAME.equals(commandName)) {
			... ScanCommand
		}*/
		
		return null;
	}
	
	public static String extractCommandName(String args) {
		args = args.trim();
		// Look for first space that should separate command name from arguments 
		int cmdNameIdx = args.indexOf(" ");
		if (cmdNameIdx == -1) {
			return args;
		}
		String commandName = args.substring(0, cmdNameIdx);
		if (!CommandsFactory.getCommnadNames().contains(commandName)) {
			return null;
		}
		
		return commandName;
	}
	
	public static String getCommnadNames() {
		return ScanCommand.COMMAND_SCAN + "\n";
	}
}
