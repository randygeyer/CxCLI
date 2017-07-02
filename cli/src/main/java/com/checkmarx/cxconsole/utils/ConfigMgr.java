package com.checkmarx.cxconsole.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.checkmarx.cxviewer.ws.WSMgr;
import com.checkmarx.cxviewer.ws.resolver.CxClientType;

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
	public static String KEY_OSA_PROGRESS_INTERVAL 	= 	"scan.osa.job.progress.interval";
	public static String KEY_RETIRES 				= 	"scan.job.connection.retries";
    public static String REPORT_TIMEOUT             =   "scan.job.report.timeout";
	public static String KEY_EXCLUDED_FOLDERS       =	"scan.zip.ignored.folders";
	public static String KEY_EXCLUDED_FILES 	    =   "scan.zip.ignored.files";
	public static String KEY_MAX_ZIP_SIZE 			=	"scan.zip.max_size";
	public static String KEY_OSA_MAX_ZIP_SIZE 		=	"scan.osa.zip.max_size";
	public static String KEY_DEF_LOG_NAME 			=	"scan.log.default.filename";
	public static String KEY_DEF_PROJECT_NAME 		=	"scan.default.projectname";
	public static String KEY_FILE_APP_PATTERN 		=	"scan.log.appender.file.pattern";
	public static String KEY_FILE_APP_MAX_SIZE 		=	"scan.log.appender.file.max_size";
	public static String KEY_FILE_APP_MAX_ROLLS 	=	"scan.log.appender.file.max_rolls";
	public static String KEY_CLI_APP_PATTERN 		=	"scan.log.appender.console.pattern";
	public static String KEY_VERSION 				=	"cxconsole.version";
	public static String KEY_USE_KERBEROS_AUTH		=	"use_kerberos_authentication";
	public static String KEY_KERBEROS_USERNAME		=	"kerberos.username";
	public static String KEY_KERBEROS_PASSWORD		=	"kerberos.password";


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
        applicationProperties.put(REPORT_TIMEOUT, "30");
        applicationProperties.put(KEY_PROGRESS_INTERVAL, "15");
        applicationProperties.put(KEY_OSA_PROGRESS_INTERVAL, "5");
		applicationProperties.put(KEY_RETIRES, "3");
		applicationProperties.put(KEY_EXCLUDED_FOLDERS, "_cvs, .svn, .hg, .git, .bzr, bin, obj, backup");
		applicationProperties.put(KEY_EXCLUDED_FILES, "*.DS_Store, *.ipr, *.iws, *.bak, *.tmp, *.aac, *.aif, *.iff, *.m3u, *.mid, *.mp3, *.mpa, *.ra, *.wav, *.wma, *.3g2, *.3gp, *.asf, *.asx, *.avi, *.flv, *.mov, *.mp4, *.mpg, *.rm, *.swf, *.vob, *.wmv, *.bmp, *.gif, *.jpg, *.png, *.psd, *.tif, *.jar, *.zip, *.rar, *.exe, *.dll, *.pdb, *.7z, *.gz, *.tar.gz, *.tar, *.ahtm, *.ahtml, *.fhtml, *.hdm, *.hdml, *.hsql, *.ht, *.hta, *.htc, *.htd, *.htmls, *.ihtml, *.mht, *.mhtm, *.mhtml, *.ssi, *.stm, *.stml, *.ttml, *.txn, *.xhtm, *.xhtml, *.class, *.iml");
        applicationProperties.put(KEY_MAX_ZIP_SIZE, "200");
        applicationProperties.put(KEY_OSA_MAX_ZIP_SIZE, "2000");
		applicationProperties.put(KEY_DEF_LOG_NAME, "cx_scan.log");
		applicationProperties.put(KEY_DEF_PROJECT_NAME, "console.project");
		applicationProperties.put(KEY_VERSION, "7.1");
		applicationProperties.put(KEY_USE_KERBEROS_AUTH, "false");
		applicationProperties.put(KEY_KERBEROS_USERNAME, "");
		applicationProperties.put(KEY_KERBEROS_PASSWORD, "");

		
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
			wsMgr = new WSMgr();
		}
		
		return wsMgr;
	}
}
