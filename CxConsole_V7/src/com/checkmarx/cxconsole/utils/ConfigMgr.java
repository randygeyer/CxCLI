package com.checkmarx.cxconsole.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.checkmarx.cxviewer.ws.WSMgr;
import com.checkmarx.cxviewer.ws.generated.CxClientType;

/**
 * Class responsible for loading CxConsole properties from corresponding 
 * config folder <br>
 * Properties should be stored in file {user.dir}/config/cx_console.properties
 * 
 * @author Oleksiy Mysnyk
 *
 */
public class ConfigMgr {	
	
	/*
	 * Property keys
	 */
	public static String KEY_PROGRESS_INTERVAL 		= 	"scan.job.progress.interval";
	public static String KEY_RETIRES 				= 	"scan.job.connection.retries";
	public static String KEY_IGNORED_FOLDERS 		=	"scan.zip.ignored.folders";
	public static String KEY_IGNORED_EXTENSIONS 	=	"scan.zip.ignored.extensions";
	public static String KEY_MAX_ZIP_SIZE 			=	"scan.zip.max_size";
	public static String KEY_DEF_LOG_NAME 			=	"scan.log.default.filename";
	public static String KEY_DEF_PROJECT_NAME 		=	"scan.default.projectname";
	public static String KEY_FILE_APP_PATTERN 		=	"scan.log.appender.file.pattern";
	public static String KEY_FILE_APP_MAX_SIZE 		=	"scan.log.appender.file.max_size";
	public static String KEY_FILE_APP_MAX_ROLLS 	=	"scan.log.appender.file.max_rolls";
	public static String KEY_CLI_APP_PATTERN 		=	"scan.log.appender.console.pattern";
	public static String KEY_PDF_GEN_TIMEOUT 		=	"scan.job.pdf.generate.timeout";
	public static String KEY_VERSION 				=	"cxconsole.version";
	
	private String CONFIG_DIR_RELATIVE_PATH = "/config";
	private String CONFIG_FILE = "/cx_console.properties";
	
	private Properties applicationProperties;
	protected static WSMgr wsMgr;
	
	public static ConfigMgr mgr;

	private ConfigMgr () {
		applicationProperties = new Properties();
		loadProperties();
	}
	
	protected void loadProperties() {
		
		String userDir = System.getProperty("user.dir");
		try {
			FileInputStream in = new FileInputStream(userDir
					+ CONFIG_DIR_RELATIVE_PATH + CONFIG_FILE);
			applicationProperties.load(in);
			in.close();
		} catch (Exception e) {
			Logger.getRootLogger().error("Error occurred during loading CxConsole "
					+ "properties. Default configuration values will be loaded.", e);
			loadDefaults();
		}
		if (applicationProperties.isEmpty()) {
			loadDefaults();
		}
	}
	
	protected void loadDefaults() {
		applicationProperties.put(KEY_PROGRESS_INTERVAL, "5");
		applicationProperties.put(KEY_RETIRES, "3");
		applicationProperties.put(KEY_IGNORED_FOLDERS, "_cvs, .svn, .hg, .git, .bzr, bin");
		applicationProperties.put(KEY_IGNORED_EXTENSIONS, "bak, tmp");
		applicationProperties.put(KEY_MAX_ZIP_SIZE, "15728640");
		applicationProperties.put(KEY_DEF_LOG_NAME, "cx_scan.log");
		applicationProperties.put(KEY_DEF_PROJECT_NAME, "console.project");
		applicationProperties.put(KEY_FILE_APP_PATTERN, "%d{ISO8601} [%t] (%F:%L) %-5p - %m%n");
		applicationProperties.put(KEY_CLI_APP_PATTERN, "[%d{ISO8601} %-5p] %m%n");
		applicationProperties.put(KEY_FILE_APP_MAX_SIZE, "5000KB");
		applicationProperties.put(KEY_FILE_APP_MAX_ROLLS, "10");
		applicationProperties.put(KEY_VERSION, "7.1");
		
		
		String userDir = System.getProperty("user.dir");
		File propsFile = new File(userDir + CONFIG_DIR_RELATIVE_PATH + CONFIG_FILE);
		if (!propsFile.exists()) {
			File configDir = new File(userDir + CONFIG_DIR_RELATIVE_PATH);
			if (!configDir.exists()) {
				configDir.mkdir();
			}
		}
		
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(propsFile);
			applicationProperties.store(fOut, "");
		} catch (Exception e) {
			// ignore
		} finally {
			if (fOut != null) {
				try {
					fOut.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}
	
	public String getProperty(String key) {
		Object value = applicationProperties.get(key);
		return value == null ? null : value.toString();
	}
	
	public Integer getIntProperty(String key) {
		Object value = applicationProperties.get(key);
		Integer intValue = null;
		if (value != null) {
			try {
			intValue = Integer.parseInt(value.toString());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return intValue;
	}
	
	public Long getLongProperty(String key) {
		Object value = applicationProperties.get(key);
		Long longValue = null;
		if (value != null) {
			try {
			longValue = Long.parseLong(value.toString());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return longValue;
	}
	
	public static ConfigMgr getCfgMgr() {
		if (mgr == null) {
			mgr = new ConfigMgr();
		}
		
		return mgr;
	}
	
	public static WSMgr getWSMgr() {
		if (wsMgr == null) {
			wsMgr = new WSMgr(CxClientType.CLI, getCfgMgr().getProperty(ConfigMgr.KEY_VERSION));
		}
		
		return wsMgr;
	}
}
