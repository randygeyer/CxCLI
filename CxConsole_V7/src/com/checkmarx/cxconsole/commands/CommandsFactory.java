package com.checkmarx.cxconsole.commands;

/**
 * 
 * @author Oleksiy Mysnyk
 *
 */
public class CommandsFactory {

	public static CxConsoleCommand getCommand(String commandName) {
		return new ScanCommand();
	}
	
	public static String getCommnadNames() {
		return ScanCommand.COMMAND_SCAN + "\n";
	}
}
