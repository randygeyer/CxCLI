package com.checkmarx.cxconsole;

import java.io.Console;

import com.checkmarx.cxconsole.commands.GeneralScanCommand;
import com.checkmarx.cxconsole.commands.ScanCommand;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxviewer.ws.SSLUtilities;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
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
    public static String MSG_ERR_SRV_NAME_OR_NETWORK = "Server Name is invalid or network is unavailable.";
	
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
        log.setLevel(Level.TRACE);
        runCli(args);
    }

    /**
     * Entry point to CxScan Console that returns exitCode
     * This entry point is used by Jenkins plugin
     * @param args
     */

    public static int runCli(String[] args) {
		try {

            log.info("CxConsole version " + ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_VERSION));
			log.info("CxConsole scan session started");
			if (args == null || args.length == 0) {
                log.fatal("Missing command name. Available commands: " + CommandsFactory.getCommnadNames());
                return CxConsoleCommand.CODE_ERRROR;
            }

            javaVersionWarning();

            System.setProperty("java.security.auth.login.config", System.class.getResource("/login.conf").toString());
            System.setProperty("java.security.krb5.conf",System.getProperty("user.dir") + "/config/krb5.conf");
            System.setProperty("sun.security.krb5.debug", "true");   // TODO: Remove the debug option
            System.setProperty("auth.spnego.requireCredDelegation", "true");

            // Temporary solution
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();

            String commandName = args[0];
            String[] argumentsLessCommandName = java.util.Arrays.copyOfRange(args,1,args.length);
            CxConsoleCommand command = CommandsFactory.getCommand(commandName);
            if (command == null) {
                log.error("Command \"" + commandName + "\" was not found. Available commands:\n"
                        + CommandsFactory.getCommnadNames());
                return CxConsoleCommand.CODE_ERRROR;
            }

            try {
                command.parseArguments(argumentsLessCommandName);

                try{
                    command.initKerberos();
                    command.resolveServerUrl();
                }
                catch (Exception e){
                    log.trace("",e);
                    log.fatal(MSG_ERR_SRV_NAME_OR_NETWORK + " Error message: " + e.getMessage()+"\n");
                    command.printHelp();
                    return CxConsoleCommand.CODE_ERRROR;
                }
                command.checkParameters();
            } catch (ParseException e)
            {
                log.fatal("Command parameters are invalid: " + e.getMessage()+"\n");
                command.printHelp();
                return CxConsoleCommand.CODE_ERRROR;
            } catch (CommandLineArgumentException e)
            {
                log.fatal("Command parameters are invalid: " + e.getMessage()+"\n");
                command.printHelp();
                return CxConsoleCommand.CODE_ERRROR;
            }catch (Exception e)
            {
                log.fatal("Command parameters are invalid: " + e.getMessage() + "\n");
                command.printHelp();
                return CxConsoleCommand.CODE_ERRROR;
            }



            int exitCode =  command.execute();
            log.info("CxConsole scan session finished");
            return exitCode;

		} catch (org.apache.commons.cli.ParseException e) {
           // Ignore, the exception is handled in above catch statement
            return CxConsoleCommand.CODE_ERRROR;
        } catch (Throwable e) {
			log.error("Unexpected error occurred during console session.Error message:\n" + e.getMessage());
			log.info("", e);
            return CxConsoleCommand.CODE_ERRROR;
		}
	}

    private static void javaVersionWarning()
    {
        try {
            final String javaVersion = System.getProperty("java.version");
            final int minorVersion = Integer.parseInt(javaVersion.split("\\.")[1]);
            if (minorVersion < 7 )
            {
                log.info("Kerberos authentication is supported on JVM 1.7 and above, current JVM version: " + javaVersion);
            }
            // TODO: Add Java 8 warning - Pre-authentication feature is supported only on java 8
        } catch (Throwable e) {
            // This is just a warning, in case of errors, do nothing
        }
    }
}
