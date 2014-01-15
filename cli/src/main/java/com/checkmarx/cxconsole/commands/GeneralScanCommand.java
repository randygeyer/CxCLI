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
import org.apache.log4j.Level;

public abstract class GeneralScanCommand extends VerboseCommand {

    public static final Option PARAM_HOST = OptionBuilder.isRequired().hasArg().withArgName("server").withDescription("IP address or resolvable name of CxSuite web server").create("CxServer");
    public static final Option PARAM_USER = OptionBuilder.hasArg().withArgName("username").withDescription("Login username. Mandatory, Unless SSO login is used on Windows ('-useSSO' flag)").create("CxUser");
    public static final Option PARAM_PASSWORD = OptionBuilder.hasArg().withArgName("password").withDescription("Login password. Mandatory, Unless SSO login is used on Windows ('-useSSO' flag)").create("CxPassword");
    public static final Option PARAM_LOG_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Log file. Optional.").create("log");
    public static final Option PARAM_XML_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results XML file. Optional.").create("ReportXML");
    public static final Option PARAM_PDF_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results PDF file. Optional.").create("ReportPDF");
    public static final Option PARAM_CSV_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results CSV file. Optional.").create("ReportCSV");
    public static final Option PARAM_RTF_FILE = OptionBuilder.hasArg().withArgName("file").withDescription("Name or path to results RTF file. Optional.").create("ReportRTF");
    public static final Option PARAM_EXCLUDE_FOLDERS = OptionBuilder.hasArgs().withArgName("folders list").withDescription("Comma separated list of folder path patterns to exclude from scan. Example: '-LocationFoldersExclude test*' excludes all folders which start with 'test' prefix. Optional.").withValueSeparator(',').create("LocationFoldersExclude");
    public static final Option PARAM_EXCLUDE_FILES = OptionBuilder.hasArgs().withArgName("files list").withDescription("Comma separated list of file name patterns to exclude from scan. Example: '-LocationFilesExclude *.class' excludes all files with '.class' extension. Optional.").withValueSeparator(',').create("LocationFilesExclude");



	protected Integer timeout;
	protected ScanParams scParams;
	
	/*
	 * Error messages
	 */
	public static String MSG_ERR_SRV_NAME_OR_NETWORK = "Server Name is invalid or network is unavailable.";
	public static String MSG_ERR_PRJ_DIR_NOT_EXIST = "Project directory does not exist.";
	public static String MSG_ERR_PRJ_PATH_NOT_DIR = "Project path does not reference a directory.";
	public static String MSG_ERR_EXCLUDED_DIR = "Excluded folders list is invalid.";
    public static String MSG_ERR_EXCLUDED_FILES = "Excluded files list is invalid.";

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
        this.commandLineOptions.addOption(PARAM_EXCLUDE_FOLDERS);
        this.commandLineOptions.addOption(PARAM_EXCLUDE_FILES);
        // Excluded extension is hidden from the user
        // It was added to support Jenkins functionality, and since Jenking
        // do not use CLI any more, this option was disabled.
        //this.commandLineOptions.addOption(PARAM_EXCLUDE_FILES);
    }


    @Override
	public void resolveServerUrl() throws Exception {
		String generatedHost = null;
		try {
			generatedHost = ConfigMgr.getWSMgr().resolveServiceLocation(scParams.getHost());
		}
		catch (javax.xml.ws.WebServiceException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (IllegalArgumentException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (java.net.UnknownHostException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (MalformedURLException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (IOException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		catch (Throwable e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("",e);
            }
            throw new Exception(MSG_ERR_SRV_NAME_OR_NETWORK, e);
		}
		
		scParams.setHost(generatedHost);
	}
	
	@Override
	public void checkParameters() throws CommandLineArgumentException {


        if (scParams.hasExcludedFoldersParam()) {
			String[] excludedFolders = scParams.getExcludedFolders();
			if (excludedFolders == null || excludedFolders.length==0) {
				throw new CommandLineArgumentException(MSG_ERR_EXCLUDED_DIR);
			}
		}

        if (scParams.hasExcludedFilesParam()) {
            String[] excludedFiles = scParams.getExcludedFiles();
            if (excludedFiles == null || excludedFiles.length==0) {
                throw new CommandLineArgumentException(MSG_ERR_EXCLUDED_FILES);
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
		keys.append(PARAM_EXCLUDE_FOLDERS);
		keys.append(KEY_DESCR_INTEND_SINGLE);
		keys.append("- Comma separated list of folder path patterns to exclude from scan. Example: -LocationFoldersExclude “**\\test*” excludes all folders which start with “test” prefix. Optional.\n");

        keys.append(leftSpacing);
        keys.append(PARAM_EXCLUDE_FILES);
        keys.append(KEY_DESCR_INTEND_SINGLE);
        keys.append("- Comma separated list of file name patterns to exclude from scan. Example: -LocationFilesExclude “*.class” excludes all .class files. Optional.\n");


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
			+ "[ " + PARAM_EXCLUDE_FOLDERS + " \"DirName1,DirName2,DirName3\" ] "
            + "[ " + PARAM_EXCLUDE_FILES + " \"FileName1,FileName2,FileName3\" ] "
			 /*+ super.getOptionalParams()*/;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
