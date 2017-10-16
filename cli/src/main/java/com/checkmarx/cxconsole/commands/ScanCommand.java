package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.job.CxCLIOsaScanJob;
import com.checkmarx.cxconsole.commands.job.CxCLIScanJob;
import com.checkmarx.cxconsole.commands.job.CxScanJob;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.LocationType;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Level;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;

public class ScanCommand extends GeneralScanCommand {

    private String command;
    private boolean isAsyncScan;

    public static final Option PARAM_PRJ_NAME = OptionBuilder.withArgName("project name").hasArg().isRequired().withDescription("A full absolute name of a project. " +
            "The full Project name includes the whole path to the project, including Server, service provider, company, and team. " +
            "Example:  -ProjectName \"CxServer\\SP\\Company\\Users\\bs java\" " +
            "If project with such a name doesn't exist in the system, new project will be created.").create("ProjectName");

    public static final Option PARAM_LOCATION_TYPE = OptionBuilder.withArgName(LocationType.stringOfValues()).hasArg()
            .withDescription("Source location type: folder, shared, SVN, TFS, GIT, Perforce").create("LocationType");    // TODO: Check if CLI lib can check for correct param value

    public static final Option PARAM_LOCATION_PATH = OptionBuilder.withArgName("path").hasArg()
            .withDescription("Local or shared path to sources or source repository branch. Required if -LocationType is folder/shared.").create("LocationPath");

    public static final Option PARAM_LOCATION_USER = OptionBuilder.withArgName("username").hasArg()
            .withDescription("Source control or network username. Required if -LocationType is TFS/Perforce/shared.").create("LocationUser");

    public static final Option PARAM_LOCATION_PWD = OptionBuilder.withArgName("password").hasArg()
            .withDescription("Source control or network password. Required if -LocationType is TFS/Perforce/shared.").create("LocationPassword");

    public static final Option PARAM_LOCATION_URL = OptionBuilder.withArgName("url").hasArg()
            .withDescription("Source control URL. Required if -LocationType is TFS/SVN/GIT/Perforce. For Perforce SSL, set ssl:<URL> .").create("LocationURL");

    public static final Option PARAM_LOCATION_PORT = OptionBuilder.withArgName("url").hasArg()
            .withDescription("Source control system port. Default 8080/80/1666 (TFS/SVN/Perforce). Optional.").create("LocationPort");

    public static final Option PARAM_LOCATION_BRANCH = OptionBuilder.withArgName("branch").hasArg()
            .withDescription("Sources GIT branch. Required if -LocationType is GIT. Optional.").create("LocationBranch");

    public static final Option PARAM_LOCATION_PRIVATE_KEY = OptionBuilder.withArgName("file").hasArg()
            .withDescription("GIT/SVN private key location. Required  if -LocationType is GIT/SVN in SSH mode.").create("LocationPrivateKey");

    //  PARAM_LOCATION_PUBLIC_KEY option disabled because the private key parameter contains both the private and the public keys
    //  public static final Option PARAM_LOCATION_PUBLIC_KEY = OptionBuilder.withArgName("file").hasArg()
    //          .withDescription("GIT public key location. Required  if -LocationType is GIT in SSH mode.").create("LocationPublicKey");

    public static final Option PARAM_PRESET = OptionBuilder.withArgName("preset").hasArg()
            .withDescription("If preset is not specified, will use the predefined preset for an existing project, and Default preset for a new project. Optional.").create("Preset");

    public static final Option PARAM_CONFIGURATION = OptionBuilder.withArgName("configuration").hasArg()
            .withDescription("If configuration is not set, \"Default Configuration\" will be used for a new project. Possible values: [ \"Default Configuration\" | \"Japanese (Shift-JIS)\" ] Optional.").create("Configuration");

    public static final Option PARAM_INCREMENTAL = OptionBuilder.withDescription("Run incremental scan instead of full scan. Optional.").create("Incremental");

    public static final Option PARAM_PRIVATE = OptionBuilder.withDescription("Scan will not be visible to other users. Optional.").create("Private");

    public static final Option PARAM_USE_SSO = OptionBuilder.withDescription("SSO login method is used, available only on Windows. Optional.").create("UseSSO");

    public static final Option PARAM_SCAN_COMMENT = OptionBuilder.withArgName("text").withDescription("Scan comment. Example: -comment 'important scan1'. Optional.").hasArg().create("Comment");

    public static final Option PARAM_FORCE_SCAN = OptionBuilder.withDescription("Force scan on source code, which has not been changed since the last scan of the same project. Optional.").create("ForceScan");

    public static final Option PARAM_WORKSPACE = OptionBuilder.withDescription("Use location path to specify Perforce workspace name. Optional.").create("WorkspaceMode");

    public static final Option PARAM_ENABLE_OSA = OptionBuilder.withDescription("Enable Open Source Analysis (OSA). It requires the -LocationType to be folder/shared.  Optional.)").create("EnableOsa");

    public static final Option PARAM_OSA_LOCATION_PATH = OptionBuilder.hasArgs().withArgName("folders list").withDescription("Comma separated list of folder path patterns(Local or shared path ) to OSA sources.").withValueSeparator(',').create("OsaLocationPath");


    public static final Option PARAM_SAST_LOW_THRESHOLD = OptionBuilder.hasArgs().withArgName("number of low SAST vulnerabilities").withDescription("SAST low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").create("SASTLow");

    public static final Option PARAM_SAST_MEDIUM_THRESHOLD = OptionBuilder.hasArgs().withArgName("number of medium SAST vulnerabilities").withDescription("SAST medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").create("SASTMedium");

    public static final Option PARAM_SAST_HIGH_THRESHOLD = OptionBuilder.hasArgs().withArgName("number of high SAST vulnerabilities").withDescription("SAST high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").create("SASTHigh");


    public static String MSG_ERR_FOLDER_NOT_EXIST = "Specified source folder does not exist.";

    public static String MSG_ERR_SSO_WINDOWS_SUPPORT = "SSO login method is available only on Windows";

    public static String MSG_ERR_MISSING_USER_PASSWORD = "Missing username/password parameters";

    public ScanCommand(boolean isAsyncScan) {
        super();
        this.isAsyncScan = isAsyncScan;
        if (isAsyncScan) {
            command = Commands.ASYNC_SCAN.value();
        } else {
            command = Commands.SCAN.value();
        }
        initCommandLineOptions();
    }

    private void initCommandLineOptions() {
        this.commandLineOptions.addOption(PARAM_PRJ_NAME);
        this.commandLineOptions.addOption(PARAM_LOCATION_TYPE);
        this.commandLineOptions.addOption(PARAM_LOCATION_PATH);
        this.commandLineOptions.addOption(PARAM_LOCATION_USER);
        this.commandLineOptions.addOption(PARAM_LOCATION_PWD);
        this.commandLineOptions.addOption(PARAM_LOCATION_URL);
        this.commandLineOptions.addOption(PARAM_LOCATION_PORT);
        this.commandLineOptions.addOption(PARAM_LOCATION_BRANCH);
        this.commandLineOptions.addOption(PARAM_LOCATION_PRIVATE_KEY);
        this.commandLineOptions.addOption(PARAM_PRESET);
        this.commandLineOptions.addOption(PARAM_CONFIGURATION);
        this.commandLineOptions.addOption(PARAM_INCREMENTAL);
        this.commandLineOptions.addOption(PARAM_PRIVATE);
        this.commandLineOptions.addOption(PARAM_SCAN_COMMENT);
        this.commandLineOptions.addOption(PARAM_USE_SSO);
        this.commandLineOptions.addOption(PARAM_FORCE_SCAN);
        this.commandLineOptions.addOption(PARAM_WORKSPACE);
        this.commandLineOptions.addOption(PARAM_ENABLE_OSA);
        this.commandLineOptions.addOption(PARAM_OSA_LOCATION_PATH);
        this.commandLineOptions.addOption(PARAM_SAST_LOW_THRESHOLD);
        this.commandLineOptions.addOption(PARAM_SAST_MEDIUM_THRESHOLD);
        this.commandLineOptions.addOption(PARAM_SAST_HIGH_THRESHOLD);
    }

    @Override
    protected void executeCommand() {

        String scanType = "";
        if (scParams.getLocationPrivateKey() != null) {
            BufferedReader in = null;
            File keyFile = new File(scParams.getLocationPrivateKey());
            try {
                in = new BufferedReader(new FileReader(keyFile));
                String line;
                StringBuilder keyData = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    keyData.append(line);
                    keyData.append("\n");
                }
                scParams.setPrivateKey(keyData.toString());
            } catch (FileNotFoundException ex) {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Error reading private key file.", ex);
                }
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Private key file not found [ " + scParams.getLocationPrivateKey() + "]");
                }
                errorCode = errorCodeResolver(ex.getCause().getMessage());
                return;
            } catch (IOException ex) {
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Error reading private key file.", ex);
                }
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Error reading private key file. " + ex.getMessage());
                }
                errorCode = errorCodeResolver(ex.getCause().getMessage());
                return;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CxScanJob job = null;
        if (this instanceof OsaScanCommand) {
            if (Objects.equals(this.getCommandName(), Commands.OSASCAN.value())) {
                job = new CxCLIOsaScanJob(scParams, false);
                scanType = "OSA";
            } else if (Objects.equals(this.getCommandName(), Commands.ASYNC_OSA_SCAN.value())) {
                job = new CxCLIOsaScanJob(scParams, true);
                scanType = "Async OSA";
            }
        } else if (this instanceof ScanCommand) {
            if (Objects.equals(this.getCommandName(), Commands.SCAN.value())) {
                job = new CxCLIScanJob(scParams, false);
                scanType = "SAST";
            } else if (Objects.equals(this.getCommandName(), Commands.ASYNC_SCAN.value())) {
                job = new CxCLIScanJob(scParams, true);
                scanType = "Async SAST";
            }
        } else {
            log.error("Command was not found. Available commands:\n" + CommandsFactory.getCommandNames());
            errorCode = errorCodeResolver("Command was not found. Available commands:\n" + CommandsFactory.getCommandNames());
            return;
        }
        job.setLog(log);

        Future<Integer> future = executor.submit(job);
        try {
            if (timeout != null) {
                errorCode = future.get(timeout, TimeUnit.SECONDS);
            } else {
                errorCode = future.get();
            }
        } catch (InterruptedException e) {
            if (log.isEnabledFor(Level.DEBUG)) {
                log.debug(scanType + "Scan job was interrupted.", e);
            }
            errorCode = errorCodeResolver(e.getCause().getMessage());
        } catch (ExecutionException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                if (e.getCause().getMessage() != null) {
                    log.error("Error during " + scanType + " scan job execution: "
                            + e.getCause().getMessage());
                } else {
                    log.error("Error during " + scanType + " scan job execution: "
                            + e.getCause());
                }
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Error during " + scanType + " scan job execution.", e);
            }
            errorCode = errorCodeResolver(e.getCause().getMessage());
        } catch (TimeoutException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(scanType + "Scan job failed due to timeout.");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace(scanType + "Scan job failed due to timeout.", e);
            }
            errorCode = errorCodeResolver(e.getCause().getMessage());
        } catch (Exception e) {
            if (log.isEnabledFor(Level.ERROR)) {
                if (e.getCause().getMessage() != null) {
                    log.error("Error during " + scanType + " scan job execution: "
                            + e.getCause().getMessage());
                } else {
                    log.error("Error during " + scanType + " scan job execution: "
                            + e.getCause());
                }
            }
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
        }

    }

    @Override
    public String getCommandName() {
        return command;
    }

    @Override
    public String getUsageExamples() {
        return "\n\nCxConsole Scan -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin@cx -cxpassword admin -locationtype folder -locationpath C:\\cx -preset All -incremental -reportpdf a.pdf\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype tfs -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ -preset default -reportxml a.xml -reportpdf b.pdf -incremental -forcescan\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype share -locationpath '\\\\storage\\path1;\\\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ -preset \"Sans 25\" -reportxls a.xls -reportpdf b.pdf -private -verbose -log a.log\n -LocationPathExclude test*, *log* -LocationFilesExclude web.config , *.class\n";
    }

    @Override
    protected boolean isKeyFlag(String key) {
        return /*super.isKeyFlag(key) || */PARAM_INCREMENTAL.getOpt().equalsIgnoreCase(key)
                || PARAM_PRIVATE.getOpt().equalsIgnoreCase(key) || PARAM_USE_SSO.getOpt().equalsIgnoreCase(key)
                || PARAM_FORCE_SCAN.getOpt().equalsIgnoreCase(key) || PARAM_WORKSPACE.getOpt().equalsIgnoreCase(key);
    }

    /*
     * No logging inside: logger for command won't be created at the moment
     */
    @Override
    public void checkParameters() throws CommandLineArgumentException {
        super.checkParameters();
        if (scParams.getSpFolderName() != null) {
            File projectDir = new File(scParams.getSpFolderName().trim());
            if (!projectDir.exists()) {
                throw new CommandLineArgumentException(MSG_ERR_FOLDER_NOT_EXIST + "["
                        + scParams.getSpFolderName() + "]");
            }

            if (!projectDir.isDirectory()) {
                throw new CommandLineArgumentException(MSG_ERR_FOLDER_NOT_EXIST + "["
                        + scParams.getSpFolderName() + "]");
            }
        }
        if (scParams.getLocationType() == LocationType.folder
                && scParams.getLocationPath() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PATH.getOpt() + " is missing. Parameter should be specified since "
                    + PARAM_LOCATION_TYPE.getOpt() + " is [" + scParams.getLocationType() + "]");
        }

        if ((scParams.getLocationType() == LocationType.svn || scParams.getLocationType() == LocationType.tfs || scParams.getLocationType() == LocationType.perforce) &&
                scParams.getLocationURL() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_URL.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is SVN/TFS/Perforce");
        }

        if ((scParams.getLocationType() == LocationType.tfs || scParams.getLocationType() == LocationType.perforce) &&
                scParams.getLocationUser() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_USER.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is TFS/Perforce");
        }
        if ((scParams.getLocationType() == LocationType.tfs) &&
                scParams.getLocationPassword() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PWD.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is TFS");
        }

        if ((scParams.getLocationType() == LocationType.svn || scParams.getLocationType() == LocationType.tfs || scParams.getLocationType() == LocationType.perforce) &&
                scParams.getLocationPath() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PATH.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is SVN/TFS/Perforce");
        }

        if ((scParams.getLocationType() == LocationType.git) &&
                scParams.getLocationURL() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_URL.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is GIT");
        }

        if ((scParams.getLocationType() == LocationType.git) &&
                scParams.getLocationBranch() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_BRANCH.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is GIT");
        }

        if ((scParams.getLocationType() == LocationType.shared) &&
                scParams.getLocationPath() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PATH.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is shared");
        }

        if ((scParams.getLocationType() == LocationType.shared) &&
                scParams.getLocationUser() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_USER.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is shared");
        }

        if ((scParams.getLocationType() == LocationType.shared) &&
                scParams.getLocationPassword() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PWD.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is shared");
        }

        if ((scParams.getLocationType() == LocationType.folder) &&
                scParams.getLocationPath() == null) {
            throw new CommandLineArgumentException(PARAM_LOCATION_PATH.getOpt() + " is not specified. Required when " + PARAM_LOCATION_TYPE.getOpt() + " is folder");
        }

        if ((scParams.getLocationType() == LocationType.svn || scParams.getLocationType() == LocationType.tfs || scParams.getLocationType() == LocationType.perforce)
                && scParams.getLocationPort() == null) {
            throw new CommandLineArgumentException("Invalid location port ["
                    + commandLineArguments.getOptionValue(PARAM_LOCATION_PORT.getOpt()) + "]");
        }

        if (scParams.getLocationPrivateKey() != null
                && scParams.getLocationType() != null
                && (scParams.getLocationType() == LocationType.git || scParams.getLocationType() == LocationType.svn)) {
            File keyFile = new File(scParams.getLocationPrivateKey().trim());
            if (!keyFile.exists()) {
                throw new CommandLineArgumentException("Private key file is not found " + "["
                        + scParams.getLocationPrivateKey() + "]");
            }
            if (keyFile.isDirectory()) {
                throw new CommandLineArgumentException("Private key file preferences folder "
                        + "[" + scParams.getLocationPrivateKey() + "]");
            }


        }

        if (scParams.getIsPerforceWorkspaceMode() &&
                scParams.getLocationType() != null &&
                scParams.getLocationType() != LocationType.perforce) {
            throw new CommandLineArgumentException(PARAM_WORKSPACE.getOpt() + " should be specified only when " + PARAM_LOCATION_TYPE.getOpt() + " is Perforce");
        }

        if (scParams.isSsoLoginUsed()) {
            if (!isWindows()) {
                throw new CommandLineArgumentException(MSG_ERR_SSO_WINDOWS_SUPPORT);
            }
        } else if (!scParams.hasUserParam() || !scParams.hasPasswordParam()) {
            throw new CommandLineArgumentException(MSG_ERR_MISSING_USER_PASSWORD);
        }

        if (scParams.isOsaEnabled() && (scParams.getLocationPath() == null || (scParams.getLocationType() != LocationType.folder && scParams.getLocationType() != LocationType.shared))) {
            throw new CommandLineArgumentException("For OSA Scan (" + PARAM_ENABLE_OSA.getOpt() + "), provide  " + PARAM_OSA_LOCATION_PATH.getOpt() + "  or " + PARAM_LOCATION_TYPE.getOpt() + " ( values: folder/shared)");
        }

        if (isAsyncScan && (scParams.getReportFile() != null || scParams.getXmlFile() != null || scParams.getReportType() != null)) {
            throw new CommandLineArgumentException("Asynchronous run does not allow report creation. Please remove the report parameters and run again");
        }
    }

    @Override
    public String getLogFileLocation() {
        String logFileLocation = commandLineArguments.getOptionValue(PARAM_LOG_FILE.getOpt());
        String projectName = commandLineArguments.getOptionValue(PARAM_PRJ_NAME.getOpt());
        if (projectName != null) {
            projectName = projectName.replaceAll("/", "\\\\");
        }
        // String usrHomeDir = System.getProperty("user.home");
        // CxLogger.getLogger().info("Log user dir: " +
        // System.getProperty("user.dir"));

        String[] parts = projectName.split("\\\\");
        String usrDir = System.getProperty("user.dir") + File.separator + normalizeLogPath(parts[parts.length - 1]) + File.separator;

        // String usrHomeDir = "";
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

    public static boolean isWindows() {
        boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
        return isWindows;
    }


    @Override
    public String getMandatoryParams() {
        return super.getMandatoryParams() + PARAM_PRJ_NAME
                + " fullProjectName "/* + PARAM_LOCATION_TYPE + " ltype" */;
    }

    @Override
    public String getKeyDescriptions() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(super.getKeyDescriptions());

        keys.append(leftSpacing);
        keys.append(PARAM_PRJ_NAME);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Full Project name. Mandatory\n");

        // keys.append(leftSpacing);
        // keys.append(PARAM_LOCATION_TYPE);
        // keys.append(KEY_DESCR_INTEND_SMALL);
        // keys.append("- Source location type [" +
        // LocationType.folder.getLocationType()
        // + "/" + LocationType.shared.getLocationType()
        // + "/" + LocationType.tfs.getLocationType()
        // + "/" + LocationType.svn.getLocationType()
        // + "/" + LocationType.git.getLocationType() +"]. Mandatory\n");

        return keys.toString();
    }
}