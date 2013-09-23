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

    public static String PARAM_LOCATION_TYPE = "-Locationtype";
	public static String PARAM_LOCATION_PATH = "-locationpath";
	public static String PARAM_LOCATION_USER = "-locationuser";
	public static String PARAM_LOCATION_PWD = "-locationpassword";
	public static String PARAM_LOCATION_URL = "-locationURL";
	public static String PARAM_LOCATION_PORT = "-locationPort";
	// public static String PARAM_LOCATION_REPOSITORY = "-locationRepository";
	public static String PARAM_LOCATION_BRANCH = "-locationBranch";
	public static String PARAM_LOCATION_PRIVATE_KEY = "-locationprivatekey";
	public static String PARAM_LOCATION_PUBLIC_KEY = "-locationpublickey";
	public static String PARAM_PRESET = "-Preset";
	public static String PARAM_CONFIGURATION = "-Configuration";
	public static String PARAM_INCREMENTAL = "-incremental";
	public static String PARAM_PRIVATE = "-private";
	public static String PARAM_SCAN_COMMENT = "-Comment";
	
	public static String PARAM_PRJ = "-project";
	public static String PARAM_FOLDER_NAME = "-folderName";

	public static String MSG_ERR_FOLDER_NOT_EXIST = "Specified source folder does not exist.";

    public static final Option PARAM_LOCATION_TYPE_2 = OptionBuilder.withArgName("type").hasArgs().withDescription("Source location type [folder/shared/TFS/SVN/GIT]").create("Locationtype");


    public ScanCommand() {
		super(); // cli mode
        initCommandLineOptions();
	}

    private void initCommandLineOptions()
    {
        this.commandLineOptions.addOption(PARAM_PRJ_NAME);
        this.commandLineOptions.addOption(PARAM_LOCATION_TYPE_2);
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
					log.trace("Error reding public key file.", ex);
				}
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Public key file not found [ "
							+ scParams.getLocationPublicKey() + "]");
				}
				errorCode = CODE_ERRROR;
				return;
			} catch (IOException ex) {
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error reding public key file.", ex);
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
					log.trace("Error reding private key file.", ex);
				}
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Private key file not found [ " + scParams.getLocationPublicKey() + "]");
				}
				errorCode = CODE_ERRROR;
				return;
			} catch (IOException ex) {
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error reding private key file.", ex);
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
		return /*super.isKeyFlag(key) || */PARAM_INCREMENTAL.equalsIgnoreCase(key)
				|| PARAM_PRIVATE.equalsIgnoreCase(key);
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
					+ parameters.get(PARAM_LOCATION_PORT.toUpperCase()) + "]");
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
				if (parameters.containsKey(PARAM_LOCATION_PATH.toUpperCase())) {
					locationParamOK = true;
				}
				break;
			case shared:
				if (parameters.containsKey(PARAM_LOCATION_PATH.toUpperCase())
						&& parameters.containsKey(PARAM_LOCATION_USER
								.toUpperCase())
						&& parameters.containsKey(PARAM_LOCATION_PWD
								.toUpperCase())) {
					locationParamOK = true;
				}
				break;
			case tfs:
			case svn:
				if (parameters.containsKey(PARAM_LOCATION_URL.toUpperCase())
						&& parameters.containsKey(PARAM_LOCATION_USER
								.toUpperCase())
						&& parameters.containsKey(PARAM_LOCATION_PWD
								.toUpperCase())
						&& parameters.containsKey(PARAM_LOCATION_PATH
								.toUpperCase())) {
					locationParamOK = true;
				}
				break;
			case git:
				if (parameters.containsKey(PARAM_LOCATION_URL.toUpperCase())
				/*
				 * &&
				 * parameters.containsKey(PARAM_LOCATION_PRIVATE_KEY.toUpperCase
				 * ()) &&
				 * parameters.containsKey(PARAM_LOCATION_PUBLIC_KEY.toUpperCase
				 * ())
				 */
				&& parameters.containsKey(PARAM_LOCATION_BRANCH.toUpperCase())) {
					locationParamOK = true;
				}
				break;
			}
		} else {
			locationParamOK = true;
		}

		return super.commandAbleToRun()
				&& parameters.containsKey(PARAM_PRJ_NAME.getOpt().toUpperCase())
				/* && parameters.containsKey(PARAM_LOCATION_TYPE.toUpperCase()) */
				&& locationParamOK;
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
