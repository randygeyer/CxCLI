package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.logging.CxConsoleLoggerFactory;
import com.checkmarx.login.soap.exceptions.CxSoapLoginClientException;
import com.checkmarx.parameters.CLIScanParameters;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.checkmarx.exitcodes.Constants.ErrorMassages.SERVER_CONNECTIVITY_VALIDATION_ERROR;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.login.soap.utils.SoapClientUtils.resolveServerProtocol;

/**
 * Created by nirli on 30/10/2017.
 */
public abstract class CLICommand {

    protected Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");

    protected CLIScanParameters params;

    protected int exitCode;

    protected String commandName;

    protected Integer timeoutInSeconds;

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    protected HelpFormatter helpFormatter = new HelpFormatter();

    protected boolean isAsyncScan = false;

    private static final int UNASSIGNED_EXIT_CODE = -1;
    protected static final String HELP_HEADER = "\nThe \"Scan\" command allows to scan new and existing projects. It accepts all project settings as an arguments, similar to Web interface.";

    public CLICommand(CLIScanParameters params) {
        this.params = params;
        exitCode = UNASSIGNED_EXIT_CODE;
        initHelpMessage();
    }

    private void initHelpMessage() {
        helpFormatter.setLeftPadding(4);
    }

    public final int execute() throws CLICommandException {
        try {
            initLogging();
        } catch (IOException e) {
            log.error("Error initiate the logger: " + e.getMessage());
            throw new CLICommandException("Error initiate the logger: " + e.getMessage());
        }

        try {
            String hostWithProtocol = resolveServerProtocol(params.getCliMandatoryParameters().getOriginalHost());
            params.getCliMandatoryParameters().setOriginalHost(hostWithProtocol);
            log.info("Server connectivity test succeeded to: " + params.getCliMandatoryParameters().getOriginalHost());
        } catch (CxSoapLoginClientException e) {
            log.error(SERVER_CONNECTIVITY_VALIDATION_ERROR + e.getMessage());
            throw new CLICommandException(SERVER_CONNECTIVITY_VALIDATION_ERROR + e.getMessage());
        }

        printCommandsDebug();
        try {
            return executeCommand();
        } catch (CLICommandException e) {
            return errorCodeResolver(e.getMessage());
        } finally {
            releaseLog();
            executor.shutdown();
        }
    }

    /**
     * Command specific operations. Should be implemented by every
     * complete executable command.
     */
    protected abstract int executeCommand() throws CLICommandException;

    public abstract void checkParameters() throws CLICommandParameterValidatorException;

    private void printCommandsDebug() {
        log.debug("----------------------------Configured Commands:-----------------------------");
        log.debug("Command type: " + getCommandName());
        for (Option opt : params.getParsedCommandLineArguments().getOptions()) {
            String option = opt.getOpt();
            if (!Objects.equals(option, "cxpassword")) {
                if (opt.getValue() == null) {
                    log.debug("Option: " + StringUtils.capitalize(opt.getOpt()) + "   Value: True");
                } else {
                    log.debug("Option: " + StringUtils.capitalize(opt.getOpt()) + "   Value: " + opt.getValue());
                }
            } else {
                log.debug("Option: Cxpassword   Value: **********");
            }
        }
        log.debug("-----------------------------------------------------------------------------");
    }

    public abstract String getCommandName();

    public abstract String getMandatoryParams();

    public abstract String getOptionalParams();

    public abstract String getKeyDescriptions();

    public abstract String getOptionalKeyDescriptions();

    public abstract String getUsageExamples();

    public abstract void printHelp();

    private void initLogging() throws IOException {
        String logPath = "";
        String logPathFromParam = params.getCliSharedParameters().getLogFile();
        if (logPathFromParam != null) {
            logPath = getLogFileLocation(logPathFromParam, params.getCliMandatoryParameters().getProjectName());
        }

        log = CxConsoleLoggerFactory.getLoggerFactory().getLogger(logPath);
    }

    private void releaseLog() {
        log.removeAllAppenders();
    }

    private String getLogFileLocation(String logPath, String projectNameFromParam) {
        String logFileLocation = logPath;
        String projectName = projectNameFromParam;
        if (projectName != null) {
            projectName = projectName.replaceAll("/", "\\\\");
        }
        String[] parts = projectName.split("\\\\");
        String usrDir = System.getProperty("user.dir") + File.separator + normalizeLogPath(parts[parts.length - 1]) + File.separator;

        if (logFileLocation == null) {
            logFileLocation = usrDir + normalizeLogPath(parts[parts.length - 1]) + ".log";
        } else {

            String origPath = logFileLocation;

            try {
                logFileLocation = Paths.get(logFileLocation).toFile().getCanonicalPath();
            } catch (IOException e) {
                logFileLocation = origPath;
            }

            File logpath = new File(logFileLocation);

            if (logpath.isAbsolute()) {
                // Path is absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }
                }
            } else {
                // Path is not absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = usrDir + logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(usrDir + dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }

                    logFileLocation = usrDir + logFileLocation;
                }
            }
        }

        return logFileLocation;
    }

    private String normalizeLogPath(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return "cx_scan.log";
        }

        String normalPathName = "";
        normalPathName = projectName.replace("\\", "_");
        normalPathName = normalPathName.replace("/", "_");
        normalPathName = normalPathName.replace(":", "_");
        normalPathName = normalPathName.replace("?", "_");
        normalPathName = normalPathName.replace("*", "_");
        normalPathName = normalPathName.replace("\"", "_");
        normalPathName = normalPathName.replace("<", "_");
        normalPathName = normalPathName.replace(">", "_");
        normalPathName = normalPathName.replace("|", "_");
        return normalPathName;
    }


}