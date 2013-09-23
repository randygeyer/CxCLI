package com.checkmarx.cxconsole.commands;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public abstract class GeneralScanCommand extends VerboseCommand {

	public static String PARAM_HOST = "-CxServer";
	public static String PARAM_USER = "-CxUser";
	public static String PARAM_PASSWORD = "-CxPassword";	
	public static String PARAM_LOG_FILE = "-log";
	public static String PARAM_XML_FILE = "-reportxml";
	public static String PARAM_PDF_FILE = "-reportpdf";
	public static String PARAM_CSV_FILE = "-reportcsv";
	public static String PARAM_RTF_FILE = "-reportrtf";
	public static String PARAM_EXCLUDE = "-locationpathexclude";

    public static final Option PARAM_HOST_2 = OptionBuilder.isRequired().hasArg().withArgName("server").withDescription("Host name of web-service").create("CxServer");
    public static final Option PARAM_USER_2 = OptionBuilder.isRequired().hasArg().withArgName("username").withDescription("User login name").create("CxUser");



	protected Integer timeout;
	protected ScanParams scParams;
	
	/*
	 * Error messages
	 */
	public static String MSG_ERR_SRV_NAME_OR_NETWORK = "Server Name is not valid or network unavailable.";
	public static String MSG_ERR_SRV_NAME_INCORRECT = "Not correct server name.";
	public static String MSG_ERR_PRJ_DIR_NOT_EXIST = "Project directory does not exist.";
	public static String MSG_ERR_PRJ_PATH_NOT_DIR = "Project path does not reference a directory.";
	public static String MSG_ERR_EXCLUDED_DIR = "Ignored folders list is invalid.";
	
	public GeneralScanCommand() {
		super();  // cli mode
		initCommandLineOptions();
	}

    private void initCommandLineOptions()
    {
        this.commandLineOptions.addOption(PARAM_HOST_2);
        this.commandLineOptions.addOption(PARAM_USER_2);
    }

	@Override
	public boolean commandAbleToRun() {
		return parameters.containsKey(PARAM_HOST.toUpperCase())
				&& parameters.containsKey(PARAM_USER.toUpperCase())
				&& parameters.containsKey(PARAM_PASSWORD.toUpperCase());
	}

	protected void checkHost() throws Exception {
		String generatedHost = null;
		try {
			generatedHost = ConfigMgr.getWSMgr().resolveServiceLocation(scParams.getHost());
		}
		catch (javax.xml.ws.WebServiceException e) {
			throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (IllegalArgumentException e) {
			throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (java.net.UnknownHostException e) {
			throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (MalformedURLException e) {
			throw new Exception(MSG_ERR_SRV_NAME_INCORRECT, e);
		}
		catch (IOException e) {
			throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (Throwable e) {
			throw new Exception(MSG_ERR_SRV_NAME_INCORRECT, e);
		}
		
		scParams.setHost(generatedHost);
	}
	
	@Override
	public void checkParameters() throws Exception {
		checkHost();
		
		if (scParams.hasExcludedParam()) {
			String excludedFolders = scParams.getExcludedFolders();
			if (excludedFolders == null || excludedFolders.isEmpty()) {
				throw new Exception(MSG_ERR_EXCLUDED_DIR);
			}
		}
	}
	

	
	@Override
	public String getDescriptionString() {
		return "All result file paths (xml, pdf, log) can be provided in absolute or \n" +
				"relative form. In case of relative path, all files/directories will be created\n" +
				"in scanned project root (in console root if existing project scan is performed).\n" +
				"If some path directories don't exist, they will be created.\n\n" + 
				"Directory path - if specified path ends with path separator character (OS dependent)\n" +
				"path is threaded as directory path. In this case file name will be generated\n" +
				"from scanned project name.\n" +
				"File path - in other cases path is treated as file path. Provided\n" +
				"file name will override project name. Examples:\n\n" +
				"Absolute directory paths:\n" + 		 
				"[Windows] C:\\ScanLogs\\ProjectX_log\\\n" +
				"[Linux] /user/home/project_logs/prj_one/\n\n" +
				"Absolute file paths:\n" +
				"[Windows] C:\\ScanLogs\\ProjectX_log\\project_log.log\n" +
				"[Windows] C:\\ScanLogs\\ProjectX_log\\justlogfile\n" +
				"[Linux] /user/home/project_logs/prj_one/project_scan_info.log\n\n" +
				"Relative directory paths:\n" +
				"[Windows] XMLResuls\\Scan1\\\n" +
				"[Linux] PDFStorage/\n\n" +
				"Relative file paths:\n" +
				"[Windows] scan_resuls\\project_summary.pdf\n" +
				"[Linux] cxscan/results/xml/xml_results_without_extention";
	}
	
	@Override
	public String getKeyDescriptions() {
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);
		
		keys.append(PARAM_HOST);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Host name of web-service. Mandatory\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_USER);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- User login name. Mandatory\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_PASSWORD);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Login password. Mandatory\n");
		
		return keys.toString();
	}
	
	@Override
	public String getOptionalKeyDescriptions() {
		
		String leftSpacing = "  ";
		StringBuilder keys = new StringBuilder(leftSpacing);
		
		keys.append(PARAM_LOG_FILE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Name or path to log file. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_XML_FILE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Name or path to results XML file. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_PDF_FILE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Name or path to results PDF file. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_CSV_FILE);
		keys.append(KEY_DESCR_INTEND_SMALL);
		keys.append("- Name or path to results CSV file. Optional.\n");
		
		keys.append(leftSpacing);
		keys.append(PARAM_EXCLUDE);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Semicolon separated list of ignored folders. Optional. Example: "+PARAM_EXCLUDE+" 'test*;log_*'\n");
		
		//keys.append(super.getOptionalKeyDescriptions());
		
		return keys.toString();
	}

	@Override
	public String getMandatoryParams() {
		return PARAM_HOST + " hostName " + PARAM_USER + " login "
				+ PARAM_PASSWORD + " password ";
	}
	
	@Override
	public String getOptionalParams() {
		return "[ " + PARAM_XML_FILE + " results.xml ] "
			+ "[ " + PARAM_PDF_FILE + " results.pdf ] "
			+ "[ " + PARAM_CSV_FILE + " results.csv ] "
			+ "[ " + PARAM_LOG_FILE + " logFile.log ] "
			+ "[ " + PARAM_EXCLUDE + " \"DirName1;DirName2;DirName3\" ] "
			 /*+ super.getOptionalParams()*/;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
