package com.checkmarx.parameters;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.parameters.exceptions.CLIParameterParsingException;
import com.checkmarx.parameters.utils.ParametersUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import static com.checkmarx.cxconsole.utils.ConfigMgr.*;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLIOSAParameters extends AbstractCLIScanParameters {

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private CLIMandatoryParameters cliMandatoryParameters;

    private boolean isOsaThresholdEnabled = false;
    private int osaLowThresholdValue = Integer.MAX_VALUE;
    private int osaMediumThresholdValue = Integer.MAX_VALUE;
    private int osaHighThresholdValue = Integer.MAX_VALUE;
    private static final String SPLIT_REGEX = "\\s*,\\s*";


    private String[] osaLocationPath = new String[]{};
    private String[] osaExcludedFolders = new String[]{};
    private boolean hasOsaExcludedFoldersParam = false;
    private String[] osaExcludedFiles = new String[]{};
    private boolean hasOsaExcludedFilesParam = false;
    private String[] osaIncludedFiles = new String[]{};
    private boolean hasOsaIncludedFilesParam = false;
    private String[] osaExtractableIncludeFiles = new String[]{};
    private boolean hasOsaExtractableIncludeFilesParam = false;
    private String osaScanDepth;
    private String osaReportPDF;
    private String osaReportHTML;
    private String osaJson;

    private static final Option PARAM_OSA_LOCATION_PATH = Option.builder("osalocationpath").hasArgs().argName("folders list").desc("Comma separated list of folder path patterns(Local or shared path ) to OSA sources.")
            .valueSeparator(',').build();

    private static final Option PARAM_OSA_PDF_FILE = Option.builder("osareportpdf").hasArg(true).optionalArg(true).argName("file").desc("Name or path to OSA PDF report . Optional.").build();
    private static final Option PARAM_OSA_HTML_FILE = Option.builder("osareporthtml").hasArg(true).optionalArg(true).argName("file").desc("Name or path to OSA HTML report. Optional.").build();
    private static final Option PARAM_OSA_JSON = Option.builder("osajson").hasArg(true).optionalArg(true).argName("file").desc("Name or path to OSA scan results (libraries and vulnerabilities) in Json format. Optional.").build();

    private static final Option PARAM_OSA_EXCLUDE_FILES = Option.builder("osafilesexclude").hasArgs().argName("files list").desc("Comma separated list of file name patterns to exclude from OSA scan. Example: '-OsaFilesExclude *.class' excludes all files with '.class' extension. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_OSA_INCLUDE_FILES = Option.builder("osafilesinclude").hasArgs().argName("folders list").desc("Comma separated list of files extension to include in OSA scan. Example: '-OsaFilesInclude *.bin' include only files with .bin extension. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_OSA_EXCLUDE_FOLDERS = Option.builder("osapathexclude").hasArgs().argName("folders list").desc("Comma separated list of folder path patterns to exclude from OSA scan. Example: '-OsaPathExclude test' excludes all folders which start with 'test' prefix. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_OSA_EXTRACTABLE_INCLUDE_FILES = Option.builder("osaarchivetoextract").hasArgs().argName("folders list").desc("Comma separated list of files extension to be extracted for OSA scan. Example: '-OSAArchiveIncludes *.zip' extracts only files with .zip extension. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_OSA_SCAN_DEPTH = Option.builder("osascandepth").hasArg(true).argName("OSA analysis unzip depth").desc("Extraction depth for files to send for OSA analysis. Optional.").build();


    private static final Option PARAM_OSA_LOW_THRESHOLD = Option.builder("osalow").hasArg(true).argName("number of low OSA vulnerabilities").desc("OSA low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error. Optional.").build();
    private static final Option PARAM_OSA_MEDIUM_THRESHOLD = Option.builder("osamedium").hasArg(true).argName("number of medium OSA vulnerabilities").desc("OSA medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error. Optional.").build();
    private static final Option PARAM_OSA_HIGH_THRESHOLD = Option.builder("osahigh").hasArg(true).argName("number of high OSA vulnerabilities").desc("OSA high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error. Optional.").build();


    CLIOSAParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initOsaParams(CommandLine parsedCommandLineArguments) {
        osaLocationPath = parsedCommandLineArguments.getOptionValues(PARAM_OSA_LOCATION_PATH.getOpt());

        hasOsaExcludedFoldersParam = parsedCommandLineArguments.hasOption(PARAM_OSA_EXCLUDE_FOLDERS.getOpt());
        hasOsaExcludedFilesParam = parsedCommandLineArguments.hasOption(PARAM_OSA_EXCLUDE_FILES.getOpt());
        osaExcludedFiles = parsedCommandLineArguments.getOptionValues(PARAM_OSA_EXCLUDE_FILES.getOpt());
        hasOsaIncludedFilesParam = parsedCommandLineArguments.hasOption(PARAM_OSA_INCLUDE_FILES.getOpt());
        osaIncludedFiles = parsedCommandLineArguments.getOptionValues(PARAM_OSA_INCLUDE_FILES.getOpt());
        hasOsaExtractableIncludeFilesParam = parsedCommandLineArguments.hasOption(PARAM_OSA_EXTRACTABLE_INCLUDE_FILES.getOpt());
        osaExtractableIncludeFiles = parsedCommandLineArguments.getOptionValues(PARAM_OSA_EXTRACTABLE_INCLUDE_FILES.getOpt());
        osaReportHTML = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_OSA_HTML_FILE.getOpt());
        osaReportPDF = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_OSA_PDF_FILE.getOpt());
        osaJson = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_OSA_JSON.getOpt());
        osaScanDepth = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_OSA_SCAN_DEPTH.getOpt());
        String osaLowThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_OSA_LOW_THRESHOLD.getOpt());
        String osaMediumThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_OSA_MEDIUM_THRESHOLD.getOpt());
        String osaHighThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_OSA_HIGH_THRESHOLD.getOpt());

        if (osaScanDepth == null) {
            osaScanDepth = ConfigMgr.getCfgMgr().getProperty(KEY_OSA_SCAN_DEPTH);
        }

        if (osaExcludedFiles == null) {
            osaExcludedFiles = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_EXCLUDED_FILES)).split(SPLIT_REGEX);
            cleanExtensionList(osaExcludedFiles);
        }
        if (osaIncludedFiles == null) {
            osaIncludedFiles = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_INCLUDED_FILES)).split(SPLIT_REGEX);
            cleanExtensionList(osaIncludedFiles);
        }
        if (osaExtractableIncludeFiles == null) {
            osaExtractableIncludeFiles = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_EXTRACTABLE_INCLUDE_FILES)).split(SPLIT_REGEX);
            cleanExtensionList(osaExtractableIncludeFiles);
        }
        if (hasOsaExcludedFoldersParam) {
            osaExcludedFolders = parsedCommandLineArguments.getOptionValues(PARAM_OSA_EXCLUDE_FOLDERS.getOpt());
        }

        if (osaLowThresholdStr != null || osaMediumThresholdStr != null || osaHighThresholdStr != null) {
            isOsaThresholdEnabled = true;
            if (osaLowThresholdStr != null) {
                osaLowThresholdValue = Integer.parseInt(osaLowThresholdStr);
            }

            if (osaMediumThresholdStr != null) {
                osaMediumThresholdValue = Integer.parseInt(osaMediumThresholdStr);
            }

            if (osaHighThresholdStr != null) {
                osaHighThresholdValue = Integer.parseInt(osaHighThresholdStr);
            }
        }
    }

    private void cleanExtensionList(String[] osaScanDepth) {
        for (int i = 0; i < osaScanDepth.length; i++) {
            osaScanDepth[i] = osaScanDepth[i].replace("*.", "");
        }
    }

    public boolean isOsaThresholdEnabled() {
        return isOsaThresholdEnabled;
    }

    public int getOsaLowThresholdValue() {
        return osaLowThresholdValue;
    }

    public int getOsaMediumThresholdValue() {
        return osaMediumThresholdValue;
    }

    public int getOsaHighThresholdValue() {
        return osaHighThresholdValue;
    }

    public String[] getOsaLocationPath() {
        return osaLocationPath;
    }

    public String[] getOsaExcludedFolders() {
        return osaExcludedFolders;
    }

    public String[] getOsaExcludedFiles() {
        return osaExcludedFiles;
    }

    public String[] getOsaIncludedFiles() {
        return osaIncludedFiles;
    }

    public String getOsaReportPDF() {
        return osaReportPDF;
    }

    public String getOsaReportHTML() {
        return osaReportHTML;
    }

    public String getOsaJson() {
        return osaJson;
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }

    public boolean isHasOsaExcludedFoldersParam() {
        return hasOsaExcludedFoldersParam;
    }

    public boolean isHasOsaExcludedFilesParam() {
        return hasOsaExcludedFilesParam;
    }

    public boolean isHasOsaExtractableIncludeFilesParam() {
        return hasOsaExtractableIncludeFilesParam;
    }

    public String[] getOsaExtractableIncludeFiles() {
        return osaExtractableIncludeFiles;
    }

    public boolean isHasOsaIncludedFilesParam() {
        return hasOsaIncludedFilesParam;
    }

    public String getOsaScanDepth() {
        return osaScanDepth;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_OSA_PDF_FILE);
        commandLineOptions.addOption(PARAM_OSA_HTML_FILE);
        commandLineOptions.addOption(PARAM_OSA_JSON);
        commandLineOptions.addOption(PARAM_OSA_EXCLUDE_FOLDERS);
        commandLineOptions.addOption(PARAM_OSA_EXCLUDE_FILES);
        commandLineOptions.addOption(PARAM_OSA_INCLUDE_FILES);
        commandLineOptions.addOption(PARAM_OSA_LOCATION_PATH);
        commandLineOptions.addOption(PARAM_OSA_EXTRACTABLE_INCLUDE_FILES);

        commandLineOptions.addOption(PARAM_OSA_LOW_THRESHOLD);
        commandLineOptions.addOption(PARAM_OSA_MEDIUM_THRESHOLD);
        commandLineOptions.addOption(PARAM_OSA_HIGH_THRESHOLD);
        commandLineOptions.addOption(PARAM_OSA_SCAN_DEPTH);

    }

    @Override
    public String getMandatoryParams() {
        return cliMandatoryParameters.getMandatoryParams();
    }

    @Override
    public String getKeyDescriptions() {
        return null;
    }

    OptionGroup getOSAScanParamsOptionGroup() {
        OptionGroup osaParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            osaParamsOptionGroup.addOption(opt);
        }

        return osaParamsOptionGroup;
    }
}