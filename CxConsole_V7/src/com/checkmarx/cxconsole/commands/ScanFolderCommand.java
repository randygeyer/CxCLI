package com.checkmarx.cxconsole.commands;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Level;
import com.checkmarx.cxconsole.commands.job.CxScanJob;

/**
 * Scan project folder command
 * 
 * @author Oleksiy Mysnyk
 */
public class ScanFolderCommand extends GeneralScanCommand {
	
	public static String SCAN_COMMAND = "ScanFolder";
	
	public static String PARAM_PROJ_DIR = "-sourceDir";
	public static String PARAM_PRESET = "-preset";
	public static String PARAM_FOLDER_PRJ_NAME = "-projectName";
	public static String PARAM_VISIBLE_OTHERS = "-visibleToOthers";
		
	public ScanFolderCommand(String[] cliArgs) {
		super(cliArgs);
	}
	
	public ScanFolderCommand(String cliArgs) {
		super(cliArgs);
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#isCommandComplete()
	 */
	@Override
	public boolean commandAbleToRun() {
		return super.commandAbleToRun()
			&& parameters.containsKey(PARAM_PROJ_DIR.toUpperCase())
			&& parameters.containsKey(PARAM_PRESET.toUpperCase());
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#execute()
	 */
	@Override
	public void executeCommand() {
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		CxScanJob scanJob = new CxScanJob(scParams);
		scanJob.setLog(log);
		
		Future<Integer> future = executor.submit(scanJob);
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
					log.error("Error during scan job execution: " + e.getCause().getMessage());
				} else {
					log.error("Error during scan job execution: " + e.getCause());
				}
			}
			if (log.isEnabledFor(Level.TRACE)) {
				log.trace("Error during scan job execution.", e);
			}
			errorCode = CODE_ERRROR;
		} catch (TimeoutException e) {
			if (log.isEnabledFor(Level.INFO)) {
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
	
	/*
	 * No logging inside: logger for command won't be created at the moment
	 */
	@Override
	public void checkParameters() throws Exception {
		
		File projectDir = new File(scParams.getSrcPath().trim());		
		if (!projectDir.exists()) {
			throw new Exception(MSG_ERR_PRJ_DIR_NOT_EXIST + "[" + scParams.getSrcPath() + "]");
		}
		
		if (!projectDir.isDirectory()) {
			throw new Exception(MSG_ERR_PRJ_PATH_NOT_DIR + "[" + scParams.getSrcPath() + "]");
		}
		
		super.checkParameters();
	}
	
	@Override
	protected String getLogFileLocation() {
		String logFileLocation = parameters.get(PARAM_LOG_FILE.toUpperCase());
		String projPath = parameters.get(PARAM_PROJ_DIR.toUpperCase());
		
		String projectName;
		if (parameters.get(PARAM_FOLDER_PRJ_NAME.toUpperCase()) != null && !parameters.get(PARAM_FOLDER_PRJ_NAME.toUpperCase()).isEmpty()) {
			projectName = parameters.get(PARAM_FOLDER_PRJ_NAME.toUpperCase());
		} else {
			projectName = projPath.substring(projPath.lastIndexOf(File.separator) + 1, projPath.length());
		}
		if (projectName!=null) {
			projectName = projectName.replaceAll("/","\\\\");
		}
		
		if (logFileLocation == null) {
			logFileLocation = parameters.get(PARAM_PROJ_DIR.toUpperCase()) + File.separator + projectName + ".log";
		} else {
			File logpath = new File(logFileLocation);
			if (logpath.isAbsolute()) {
				// Path is absolute
				if (logFileLocation.endsWith(File.separator)) {
					//Directory path 
					logFileLocation = logFileLocation + projectName + ".log";
				} else {
					// File path
					if (logFileLocation.contains(File.separator)) {
						String dirPath = logFileLocation.substring(0,
								logFileLocation.lastIndexOf(File.separator));
						File logDirs = new File(dirPath);
						if (!logDirs.exists()) {
							logDirs.mkdirs();
						} else if (logDirs.isFile()) {
							//cannot create directory - file already exists
							String newLogPath = projPath + File.separator + projectName + ".log";
							return newLogPath;
						}
					}
				}
			} else {
				// Path is not absolute
				if (logFileLocation.endsWith(File.separator)) {
					//Directory path 
					logFileLocation = projPath + File.separator + logFileLocation + projectName + ".log";
				} else {
					//File path
					if (logFileLocation.contains(File.separator)) {
						String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
						File logDirs = new File(projPath + File.separator + dirPath);
						if (!logDirs.exists()) {
							logDirs.mkdirs();
						} else if (logDirs.isFile()) {
							//cannot create directory - file already exists
							String newLogPath = projPath + File.separator + projectName + ".log";
							return newLogPath;
						}
					}
					
					logFileLocation = projPath + File.separator + logFileLocation;
				}
			}
		}
		
		return logFileLocation;
	}

	@Override
	protected boolean isKeyFlag(String key) {
		return PARAM_VISIBLE_OTHERS.equals(key) || super.isKeyFlag(key);
	}
	
	/* (non-Javadoc)
	 * @see com.checkmarx.cxconsole.commands.CxConsoleCommand#getCLIKeys()
	 */
	@Override
	public Set<String> initCLIKeys() {
		cliScanKeysSet = super.initCLIKeys();
		cliScanKeysSet.add(PARAM_PROJ_DIR.toUpperCase());
		cliScanKeysSet.add(PARAM_PRESET.toUpperCase());
		cliScanKeysSet.add(PARAM_FOLDER_PRJ_NAME.toUpperCase());
		cliScanKeysSet.add(PARAM_VISIBLE_OTHERS.toUpperCase());
		
		return cliScanKeysSet;
	}

	@Override
	public String getDescriptionString() {
		return "Execute folder scan. Depending on -xml/-pdf keys stores results in xml\n" +
				"or PDF format. If command is running in verbose mode all command \n" +
				"messages, evens and errors will be logged to console and log file.\n" +
				"Log file contains more detailed info, including service soap responses.\n" +
				super.getDescriptionString();
	}

	@Override
	public String getCommandName() {
		return SCAN_COMMAND;
	}

	@Override
	public String getMandatoryParams() {
		return PARAM_PROJ_DIR + " sourceFolder " + PARAM_PRESET + " presetName "
			+ super.getMandatoryParams();
	}

	@Override
	public String getOptionalParams() {
		return "[ " + PARAM_FOLDER_PRJ_NAME + " projectName ] " + " [ " + PARAM_VISIBLE_OTHERS + " ] "
			+ super.getOptionalParams();
	}
	
	@Override
	public String getOptionalKeyDescriptions() {
		
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);
		
		keys.append(PARAM_FOLDER_PRJ_NAME);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Intended scan project name. If project name is not provided\n" +
				KEY_DESCR_INTEND + "  it would be generated automatically. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_VISIBLE_OTHERS);
		keys.append("\t- Flag indicating whether current scan and results are available\n" +
				KEY_DESCR_INTEND + "  to other users. If omitted (default) value is false. Optional.\n");
		
		keys.append(super.getOptionalKeyDescriptions());
		
		return keys.toString();
	}
	
	@Override
	public String getKeyDescriptions() {
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);
		keys.append(PARAM_PROJ_DIR);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Source project folder. Mandatory\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_PRESET);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Scan preset name. During scan available preset list\n" +
				KEY_DESCR_INTEND + "  will be requested from web-service. If specified\n" +
				KEY_DESCR_INTEND + "  preset does not exist, scan will fail. Mandatory\n");
		
		
		keys.append(super.getKeyDescriptions());
		
		return keys.toString();
	}

	@Override
	public String getUsageExamples() {
		return "";
	}
}
