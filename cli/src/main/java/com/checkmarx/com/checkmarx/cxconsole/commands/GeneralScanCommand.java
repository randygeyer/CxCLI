package com.checkmarx.cxconsole.commands;

import java.io.IOException;
import java.net.MalformedURLException;

import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.ParseException;

public abstract class GeneralScanCommand extends VerboseCommand {

    public static final Option PARAM_HOST = OptionBuilder.isRequired().hasArg().withArgName("server").withDescription("IP address or resolvable name of CxSuite web server").create("CxServer");
    public static final Option PARAM_USER = OptionBuilder.isRequired().hasArg().withArgName("username").withDescription("Login username").create("CxUser");
    public static final Option PARAM_PASSWORD = OptionBuilder.isRequired().hasArg().withArgName("password").withDescription("Login password").create("CxPassword");
    public static final Option PARAM_LOG_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Log file. Optional.").create("log");
    public static final Option PARAM_XML_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results XML file. Optional.").create("ReportXML");
    public static final Option PARAM_PDF_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results PDF file. Optional.").create("ReportPDF");
    public static final Option PARAM_CSV_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results CSV file. Optional.").create("ReportCSV");
    public static final Option PARAM_RTF_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results RTF file. Optional.").create("ReportRTF");
    public static final Option PARAM_EXCLUDE = OptionBuilder.hasArgs().withArgName("file list").withDescription("List of ignored folders. Relative paths are resolved retalive to -LocationPath. Example: -LocationPathExclude test* log_*. Optional.").create("LocationPathExclude");

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

    @Override
    public void parseArguments(String[] args) throws ParseException
    {
        super.parseArguments(args);  //  parseArguments initializes commandLineArguments
        scParams = new ScanParams(commandLineArguments);
    }

    private void initCommandLineOptions()
    {
        this.commandLineOptions.addOption(PARAM_HOST);
        this.commandLineOptions.addOption(PARAM_USER);
        this.commandLineOptions.addOption(PARAM_PASSWORD);
        this.commandLineOptions.addOption(PARAM_LOG_FILE);

        this.commandLineOptions.addOption(PARAM_XML_FILE);
        OptionGroup reportGroup = new OptionGroup();
        reportGroup.setRequired(false);
        reportGroup.addOption(PARAM_PDF_FILE);
        reportGroup.addOption(PARAM_CSV_FILE);
        reportGroup.addOption(PARAM_RTF_FILE);
        this.commandLineOptions.addOptionGroup(reportGroup);
        this.commandLineOptions.addOption(PARAM_EXCLUDE);
    }


    @Override
	public void resolveServerUrl() throws Exception {
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
	public void checkParameters() throws CommandLineArgumentException {
		if (scParams.hasExcludedParam()) {
			String[] excludedFolders = scParams.getExcludedFolders();
			if (excludedFolders == null || excludedFolders.length==0) {
				throw new CommandLineArgumentException(MSG_ERR_EXCLUDED_DIR);
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
