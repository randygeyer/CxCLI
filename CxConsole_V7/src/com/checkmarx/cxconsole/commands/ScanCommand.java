package com.checkmarx.cxconsole.commands;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.cli.*;

import org.apache.log4j.Level;

import com.checkmarx.cxconsole.commands.job.CxCLIScanJob;
import com.checkmarx.cxconsole.utils.LocationType;

public class ScanCommand extends GeneralScanCommand {

	public static String COMMAND_SCAN = "Scan";

    public static final Option PARAM_PRJ_NAME = OptionBuilder.withArgName("project name").hasArg().isRequired().withDescription("An existing or new full project name." +
            "The full Project name is conducted with the whole path to the project including Server, service provider, company and team. " +
            "Example:  -ProjectName \"CxServer\\SP\\Company\\Users\\bs java\"" +
            "If project with such a name doesn't exist in the system, new project will be created.").create("ProjectName");

    public static final Option PARAM_LOCATION_TYPE = OptionBuilder.withArgName("< " + LocationType.stringOfValues() + ">").hasArg().isRequired()
            .withDescription("Source location type: folder, shared folder, source repository: SVN, TFS, GIT").create("LocationType");    // TODO: Check if CLI lib can check for correct param value

    public static final Option PARAM_LOCATION_PATH = OptionBuilder.withArgName("path").hasArg()
            .withDescription("Local or shared path to sources or source repository branch. Use semicolon \";\" to separate values. Required if -LocationType is folder/shared").create("LocationPath");  //TODO: Check if ; separator is appropriate

    public static final Option PARAM_LOCATION_USER = OptionBuilder.withArgName("username").hasArg()
            .withDescription("Source control or network username. Required if -LocationType is TFS/SVN/shared").create("LocationUser");

    public static final Option PARAM_LOCATION_PWD = OptionBuilder.withArgName("password").hasArg()
            .withDescription("Source control or network password. Required if -LocationType is TFS/SVN/shared").create("LocationPassword");

    public static final Option PARAM_LOCATION_URL = OptionBuilder.withArgName("url").hasArg()
            .withDescription("Source control URL. Required if -LocationType is TFS/SVN/GIT").create("LocationURL");

    public static final Option PARAM_LOCATION_PORT = OptionBuilder.withArgName("url").hasArg()
            .withDescription("Source control system port. Default 8080/80 (TFS/SVN).").create("LocationPort");

    public static final Option PARAM_LOCATION_BRANCH = OptionBuilder.withArgName("branch").hasArg()
            .withDescription("Sources GIT branch. Required if -LocationType is GIT.").create("LocationBranch");

    public static final Option PARAM_LOCATION_PRIVATE_KEY = OptionBuilder.withArgName("file").hasArg()
            .withDescription("GIT private key location. Required  if -LocationType is GIT in SSH mode.").create("LocationPrivateKey");

    public static final Option PARAM_LOCATION_PUBLIC_KEY = OptionBuilder.withArgName("file").hasArg()
            .withDescription("GIT public key location. Required  if -LocationType is GIT in SSH mode.").create("LocationPublicKey");

    public static final Option PARAM_PRESET = OptionBuilder.withArgName("preset").hasArg()
            .withDescription("If preset is not specified, will use the predefined preset for an existing project, and Default preset for a new project.").create("Preset");

    public static final Option PARAM_CONFIGURATION = OptionBuilder.withArgName("configuration").hasArg()
            .withDescription("If configuration is not set \"Default Configuration\" will be used for a new project.").create("Configuration");

    public static final Option PARAM_INCREMENTAL = OptionBuilder.withDescription("Will run an incremental scan instead of full scan").create("incremental");

    public static final Option PARAM_PRIVATE = OptionBuilder.withDescription("Scan will not be visible to other users").create("private");

    public static final Option PARAM_SCAN_COMMENT = OptionBuilder.withArgName("text").withDescription("Scan comment. Example: -comment 'important scan1'").hasArg().create("comment");

    public static String MSG_ERR_FOLDER_NOT_EXIST = "Specified source folder does not exist.";

    public ScanCommand() {
		super();
        initCommandLineOptions();
	}

    private void initCommandLineOptions()
    {
        this.commandLineOptions.addOption(PARAM_PRJ_NAME);
        this.commandLineOptions.addOption(PARAM_LOCATION_TYPE);
        this.commandLineOptions.addOption(PARAM_LOCATION_PATH);
        this.commandLineOptions.addOption(PARAM_LOCATION_USER);
        this.commandLineOptions.addOption(PARAM_LOCATION_PWD);
        this.commandLineOptions.addOption(PARAM_LOCATION_URL);
        this.commandLineOptions.addOption(PARAM_LOCATION_PORT);
        this.commandLineOptions.addOption(PARAM_LOCATION_BRANCH);
        this.commandLineOptions.addOption(PARAM_LOCATION_PRIVATE_KEY);
        this.commandLineOptions.addOption(PARAM_LOCATION_PUBLIC_KEY);
        this.commandLineOptions.addOption(PARAM_PRESET);
        this.commandLineOptions.addOption(PARAM_CONFIGURATION);
        this.commandLineOptions.addOption(PARAM_INCREMENTAL);
        this.commandLineOptions.addOption(PARAM_PRIVATE);
        this.commandLineOptions.addOption(PARAM_SCAN_COMMENT);
    }

	@Override
	protected void executeCommand() {

		if (scParams.getLocationPrivateKey() != null
				&& scParams.getLocationPublicKey() != null) {
			File keyFile = new File(scParams.getLocationPublicKey());
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(keyFile));
				String line;
				StringBuilder keyData = new StringBuilder();
				while ((line = in.readLine()) != null) {
					keyData.append(line);
					keyData.append("\n");
				}
				scParams.setPublicKey(keyData.toString());
			} catch (FileNotFoundException ex) {
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error reading public key file.", ex);
				}
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Public key file not found [ "
							+ scParams.getLocationPublicKey() + "]");
				}
				errorCode = CODE_ERRROR;
				return;
			} catch (IOException ex) {
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error reading public key file.", ex);
				}
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Error reading public key file. "
							+ ex.getMessage());
				}
				errorCode = CODE_ERRROR;
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

			keyFile = new File(scParams.getLocationPrivateKey());
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
					log.error("Private key file not found [ " + scParams.getLocationPublicKey() + "]");
				}
				errorCode = CODE_ERRROR;
				return;
			} catch (IOException ex) {
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error reading private key file.", ex);
				}
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Error reading private key file. " + ex.getMessage());
				}
				errorCode = CODE_ERRROR;
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
		CxCLIScanJob job = new CxCLIScanJob(scParams);
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
				log.debug("Scan job was interrupted.", e);
			}
			errorCode = CODE_ERRROR;
		} catch (ExecutionException e) {
			if (log.isEnabledFor(Level.ERROR)) {
				if (e.getCause().getMessage() != null) {
					log.error("Error during scan job execution: "
							+ e.getCause().getMessage());
				} else {
					log.error("Error during scan job execution: "
							+ e.getCause());
				}
			}
			if (log.isEnabledFor(Level.TRACE)) {
				log.trace("Error during scan job execution.", e);
			}
			errorCode = CODE_ERRROR;
		} catch (TimeoutException e) {
			if (log.isEnabledFor(Level.ERROR)) {
				log.error("Scan job failed due to timeout.");
			}
			if (log.isEnabledFor(Level.TRACE)) {
				log.trace("Scan job failed due to timeout.", e);
			}
			errorCode = CODE_ERRROR;
		} finally {
			if (executor != null) {
				executor.shutdownNow();
			}
		}
	}

	@Override
	public String getCommandName() {
		return COMMAND_SCAN;
	}

	@Override
	public String getUsageExamples() {
		return "\n\nCxConsole Scan -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin@cx -cxpassword admin -locationtype folder -locationpath C:\\cx -preset All -incremental -reportpdf a.pdf\n"
				+ "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype tfs -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ -preset default -reportxml a.xml -reportpdf b.pdf -incremental\n"
				+ "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype share -locationpath '\\\\storage\\path1;\\\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ -preset \"Sans 25\" -reportxls a.xls -reportpdf b.pdf -private -verbose -log a.log\n";
	}



	@Override
	protected boolean isKeyFlag(String key) {
		return /*super.isKeyFlag(key) || */PARAM_INCREMENTAL.getOpt().equalsIgnoreCase(key)
				|| PARAM_PRIVATE.getOpt().equalsIgnoreCase(key);
	}

	/*
	 * No logging inside: logger for command won't be created at the moment
	 */
	@Override
	public void checkParameters() throws Exception {
		super.checkParameters();
		if (scParams.getSpFolderName() != null) {
			File projectDir = new File(scParams.getSpFolderName().trim());
			if (!projectDir.exists()) {
				throw new Exception(MSG_ERR_FOLDER_NOT_EXIST + "["
						+ scParams.getSpFolderName() + "]");
			}

			if (!projectDir.isDirectory()) {
				throw new Exception(MSG_ERR_FOLDER_NOT_EXIST + "["
						+ scParams.getSpFolderName() + "]");
			}
		}
		if (scParams.getLocationType() != null
				&& scParams.getLocationType() == LocationType.folder
				&& scParams.getLocationPath() == null) {
			throw new Exception(PARAM_LOCATION_PATH + " is missed. Parameter should be specified since " 
				+ PARAM_LOCATION_TYPE + " is [" + scParams.getLocationType() + "]");
		}
		if ((scParams.getLocationType() == LocationType.svn || scParams.getLocationType() == LocationType.tfs)
				&& scParams.getLocationPort() == null) {
			throw new Exception("Invalid location port ["
					+ commandLineArguments.getOptionValue(PARAM_LOCATION_PORT.getOpt()) + "]");
		}
		if ((scParams.getLocationPrivateKey() != null && scParams
				.getLocationPublicKey() == null)
				|| (scParams.getLocationPrivateKey() == null && scParams
						.getLocationPublicKey() != null)) {
			throw new Exception("Both private and public key must be specified");
		}
		if (scParams.getLocationPrivateKey() != null
				&& scParams.getLocationPublicKey() != null
				&& scParams.getLocationType() != null
				&& scParams.getLocationType() == LocationType.git) {
			File keyFile = new File(scParams.getLocationPrivateKey().trim());
			if (!keyFile.exists()) {
				throw new Exception("Private key file is not found " + "["
						+ scParams.getLocationPrivateKey() + "]");
			}
			if (keyFile.isDirectory()) {
				throw new Exception("Private key file freferences folder "
						+ "[" + scParams.getLocationPrivateKey() + "]");
			}

			keyFile = new File(scParams.getLocationPublicKey().trim());
			if (!keyFile.exists()) {
				throw new Exception("Public key file is not found " + "["
						+ scParams.getLocationPrivateKey() + "]");
			}
			if (keyFile.isDirectory()) {
				throw new Exception("Public key file freferences folder " + "["
						+ scParams.getLocationPrivateKey() + "]");
			}
		}
	}

	@Override
	protected String getLogFileLocation() {

		String logFileLocation = commandLineArguments.getOptionValue(PARAM_LOG_FILE.getOpt());
		String projectName = commandLineArguments.getOptionValue(PARAM_PRJ_NAME.getOpt());
		if (projectName!=null) {
			projectName = projectName.replaceAll("/","\\\\");
		}
		// String usrHomeDir = System.getProperty("user.home");
		// CxLogger.getLogger().info("Log user dir: " +
		// System.getProperty("user.dir"));

		String[] parts = projectName.split("\\\\");
		String usrDir = System.getProperty("user.dir") + File.separator + normalizeLogPath(parts[parts.length - 1]) + File.separator;

		// String usrHomeDir = "";
		if (logFileLocation == null) {
			logFileLocation = usrDir + normalizeLogPath(parts[parts.length - 1]) + ".log";
		}
		else {
			File logpath = new File(logFileLocation);
			if (logpath.isAbsolute()) {
				// Path is absolute
				if (logFileLocation.endsWith(File.separator)) {
					// Directory path
					logFileLocation = logFileLocation + parts[parts.length - 1] + ".log";
				}
				else {
					// File path
					if (logFileLocation.contains(File.separator)) {
						String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
						File logDirs = new File(dirPath);
						if (!logDirs.exists()) {
							logDirs.mkdirs();
						}
					}
				}
			}
			else {
				// Path is not absolute
				if (logFileLocation.endsWith(File.separator)) {
					// Directory path
					logFileLocation = usrDir + logFileLocation + parts[parts.length - 1] + ".log";
				}
				else {
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

	@Override
	public boolean commandAbleToRun() {

		boolean locationParamOK = false;
		if (scParams.getLocationType() != null) {
			switch (scParams.getLocationType()) {
			case folder:
			    locationParamOK = commandLineArguments.hasOption(PARAM_LOCATION_PATH.getOpt());
				break;
			case shared:

                locationParamOK = commandLineArguments.hasOption(PARAM_LOCATION_PATH.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_USER.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_PWD.getOpt());

				break;
			case tfs:
			case svn:

                locationParamOK = commandLineArguments.hasOption(PARAM_LOCATION_URL.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_USER.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_PWD.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_PATH.getOpt());

				break;
			case git:

                locationParamOK = commandLineArguments.hasOption(PARAM_LOCATION_URL.getOpt()) &&
                                  commandLineArguments.hasOption(PARAM_LOCATION_BRANCH.getOpt());

				break;
			}
		} else {
			locationParamOK = true;
		}

		return super.commandAbleToRun() && commandLineArguments.hasOption(PARAM_PRJ_NAME.getOpt()) && locationParamOK;
	}

	@Override
	public String getMandatoryParams() {
		return super.getMandatoryParams() + PARAM_PRJ_NAME
				+ " fullProjectName "/* + PARAM_LOCATION_TYPE + " ltype" */;
	}

	@Override
	public String getOptionalParams() {
		return "[ " + PARAM_LOCATION_TYPE + " ltype ] " + "[ "
				+ PARAM_LOCATION_PATH + " locationPath ] " + "[ "
				+ PARAM_LOCATION_USER + " locationUser ] " + "[ "
				+ PARAM_LOCATION_PWD + " locationPassword ] " + "[ "
				+ PARAM_LOCATION_URL + " locationURL ] " + "[ "
				+ PARAM_LOCATION_PORT + " locationPort ] " + "[ "
				+ PARAM_LOCATION_BRANCH + " locationBranch ] " + "[ "
				+ PARAM_LOCATION_PRIVATE_KEY + " private.key ] " + "[ "
				+ PARAM_LOCATION_PUBLIC_KEY + " public.key ] " + "[ "
				+ PARAM_PRESET + " preset ] " + "[ "
				+ PARAM_SCAN_COMMENT + " text ] [ " 
				+ PARAM_CONFIGURATION + " configSet ] " + super.getOptionalParams() + " [ "
				+ PARAM_INCREMENTAL + " ] " + "[ " + PARAM_PRIVATE + " ] ";
	}

	@Override
	public String getOptionalKeyDescriptions() {
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);

		// keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_TYPE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Source location type ["
				+ LocationType.folder.getLocationType() + "/"
				+ LocationType.shared.getLocationType() + "/"
				+ LocationType.tfs.getLocationType() + "/"
				+ LocationType.svn.getLocationType() + "/"
				+ LocationType.git.getLocationType() + "]. Optional\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_PATH);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Local or shared path to sources. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.folder.getLocationType() + "/"
				+ LocationType.shared.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_USER);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Source control system username. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.tfs.getLocationType() + "/"
				+ LocationType.svn.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_PWD);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Source control system password. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.tfs.getLocationType() + "/"
				+ LocationType.svn.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_URL);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Source control system URL. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.tfs.getLocationType() + "/"
				+ LocationType.svn.getLocationType() + "/"
				+ LocationType.git.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_PORT);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Source control system port. Default 8080/80 (TFS/SVN). Optional.\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_BRANCH);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Sources GIT branch path. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.git.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_PRIVATE_KEY);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- GIT private key location. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.git.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_LOCATION_PUBLIC_KEY);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- GIT public key location. Optional. Required if "
				+ PARAM_LOCATION_TYPE + " is "
				+ LocationType.git.getLocationType() + "\n");

		keys.append(leftSpacing);
		keys.append(PARAM_PRESET);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Preset. Default preset: \"Default\". If project alredy exists and parameter is not specified, project preset will be used. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_SCAN_COMMENT);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- The comment will be added to scan. Optional. Example: "+PARAM_SCAN_COMMENT+" 'important scan1'\n");

		keys.append(leftSpacing);
		keys.append(PARAM_CONFIGURATION);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Configuration. Default configuration: \"Default Configuration\". If project alredy exists and parameter is not specified, project confoguration will be used. Optional.\n");

		keys.append(leftSpacing);
		keys.append(PARAM_INCREMENTAL);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Flag indicating incremental scan.\n");

		keys.append(leftSpacing);
		keys.append(PARAM_PRIVATE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Flag indicating private scan.\n");

		keys.append(super.getOptionalKeyDescriptions());

		return keys.toString();
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
