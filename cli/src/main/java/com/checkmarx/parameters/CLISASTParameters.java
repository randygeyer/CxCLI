package com.checkmarx.parameters;

import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.parameters.exceptions.CLIParameterParsingException;
import com.checkmarx.parameters.utils.ParametersUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLISASTParameters extends AbstractCLIScanParameters {

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private CLIMandatoryParameters cliMandatoryParameters;
    private CLISharedParameters cliSharedParameters;

    private String presetName;
    private String configuration;
    private boolean isIncrementalScan = false;
    private boolean forceScan = true;
    private String reportType;
    private String reportFile;
    private String xmlFile;
    private boolean isOsaEnabled = false;

    private String[] excludedFolders = new String[]{};
    private boolean hasExcludedFoldersParam = false;
    private String[] excludedFiles = new String[]{};
    private boolean hasExcludedFilesParam = false;

    private String locationURL;
    private String locationBranch;
    private String locationUser;
    private String locationPassword;
    private String locationPrivateKey;
    private String privateKey;
    private Integer locationPort;
    private LocationType locationType;
    private boolean isPerforceWorkspaceMode = false;

    private boolean isSastThresholdEnabled = false;
    private int sastLowThresholdValue = Integer.MAX_VALUE;
    private int sastMediumThresholdValue = Integer.MAX_VALUE;
    private int sastHighThresholdValue = Integer.MAX_VALUE;

    private static final Option PARAM_XML_FILE = Option.builder("reportxml").hasArg(true).argName("file").desc("Name or path to results XML file. Optional.").build();
    private static final Option PARAM_PDF_FILE = Option.builder("reportpdf").hasArg(true).argName("file").desc("Name or path to results PDF file. Optional.").build();
    private static final Option PARAM_CSV_FILE = Option.builder("reportcsv").hasArg(true).argName("file").desc("Name or path to results CSV file. Optional.").build();
    private static final Option PARAM_RTF_FILE = Option.builder("reportrtf").hasArg(true).argName("file").desc("Name or path to results RTF file. Optional.").build();

    private static final Option PARAM_LOCATION_USER = Option.builder("locationuser").argName("username").hasArg(true)
            .desc("Source control or network username. Required if -LocationType is TFS/Perforce/shared.").build();
    private static final Option PARAM_LOCATION_PWD = Option.builder("locationpassword").argName("password").hasArg(true)
            .desc("Source control or network password. Required if -LocationType is TFS/Perforce/shared.").build();
    private static final Option PARAM_LOCATION_URL = Option.builder("locationurl").argName("url").hasArg(true)
            .desc("Source control URL. Required if -LocationType is TFS/SVN/GIT/Perforce. For Perforce SSL, set ssl:<URL> .").build();
    private static final Option PARAM_LOCATION_PORT = Option.builder("locationport").argName("url").hasArg(true)
            .desc("Source control system port. Default 8080/80/1666 (TFS/SVN/Perforce). Optional.").build();
    private static final Option PARAM_LOCATION_BRANCH = Option.builder("locationbranch").argName("branch").hasArg(true)
            .desc("Sources GIT branch. Required if -LocationType is GIT. Optional.").build();
    private static final Option PARAM_LOCATION_PRIVATE_KEY = Option.builder("locationprivatekey").argName("file").hasArg(true)
            .desc("GIT/SVN private key location. Required  if -LocationType is GIT/SVN in SSH mode.").build();
    private static final Option PARAM_PRESET = Option.builder("preset").argName("preset").hasArg(true)
            .desc("If preset is not specified, will use the predefined preset for an existing project, and Default preset for a new project. Optional.").build();
    private static final Option PARAM_CONFIGURATION = Option.builder("configuration").argName("configuration").hasArg(true)
            .desc("If configuration is not set, \"Default Configuration\" will be used for a new project. Possible values: [ \"Default Configuration\" | \"Japanese (Shift-JIS)\" ] Optional.").build();
    private static final Option PARAM_INCREMENTAL = Option.builder("incremental").hasArg(false).desc("Run incremental scan instead of full scan. Optional.").build();
    private static final Option PARAM_FORCE_SCAN = Option.builder("forcescan").hasArg(false).desc("Force scan on source code, which has not been changed since the last scan of the same project. Optional.").build();
    private static final Option PARAM_WORKSPACE = Option.builder("workspacemode").desc("Use location path to specify Perforce workspace name. Optional.").build();
    private static final Option PARAM_ENABLE_OSA = Option.builder("enableosa").hasArg(false).desc("Enable Open Source Analysis (OSA). It requires the -LocationType to be folder/shared.  Optional.)").build();

    private static final Option PARAM_EXCLUDE_FOLDERS = Option.builder("locationpathexclude").hasArgs().argName("folders list").desc("Comma separated list of folder path patterns to exclude from scan. Example: '-LocationPathExclude test*' excludes all folders which start with 'test' prefix. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_EXCLUDE_FILES = Option.builder("locationfilesexclude").hasArgs().argName("files list").desc("Comma separated list of file name patterns to exclude from scan. Example: '-LocationFilesExclude *.class' excludes all files with '.class' extension. Optional.")
            .valueSeparator(',').build();

    private static final Option PARAM_SAST_LOW_THRESHOLD = Option.builder("sastlow").hasArg(true).argName("number of low SAST vulnerabilities")
            .desc("SAST low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();
    private static final Option PARAM_SAST_MEDIUM_THRESHOLD = Option.builder("sastmedium").hasArg(true).argName("number of medium SAST vulnerabilities")
            .desc("SAST medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();
    private static final Option PARAM_SAST_HIGH_THRESHOLD = Option.builder("sasthigh").hasArg(true).argName("number of high SAST vulnerabilities")
            .desc("SAST high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();


    CLISASTParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initSastParams(CommandLine parsedCommandLineArguments, LocationType locationType) throws CLIParameterParsingException {
        presetName = parsedCommandLineArguments.getOptionValue(PARAM_PRESET.getOpt());
        configuration = parsedCommandLineArguments.getOptionValue(PARAM_CONFIGURATION.getOpt());
        isIncrementalScan = parsedCommandLineArguments.hasOption(PARAM_INCREMENTAL.getOpt());
        forceScan = !parsedCommandLineArguments.hasOption(PARAM_FORCE_SCAN.getOpt());
        isOsaEnabled = parsedCommandLineArguments.hasOption(PARAM_ENABLE_OSA.getOpt());
        this.locationType = locationType;

        this.xmlFile = parsedCommandLineArguments.getOptionValue(PARAM_XML_FILE.getOpt());

        if (parsedCommandLineArguments.hasOption(PARAM_PDF_FILE.getOpt())) {
            this.reportType = "PDF";
            this.reportFile = parsedCommandLineArguments.getOptionValue(PARAM_PDF_FILE.getOpt());
        }

        if (parsedCommandLineArguments.hasOption(PARAM_CSV_FILE.getOpt())) {
            this.reportType = "CSV";
            this.reportFile = parsedCommandLineArguments.getOptionValue(PARAM_CSV_FILE.getOpt());
        }

        if (parsedCommandLineArguments.hasOption(PARAM_RTF_FILE.getOpt())) {
            this.reportType = "RTF";
            this.reportFile = parsedCommandLineArguments.getOptionValue(PARAM_RTF_FILE.getOpt());
        }

        if (parsedCommandLineArguments.hasOption(PARAM_EXCLUDE_FOLDERS.getOpt())) {
            hasExcludedFoldersParam = true;
            excludedFolders = parsedCommandLineArguments.getOptionValues(PARAM_EXCLUDE_FOLDERS.getOpt());
        }

        if (parsedCommandLineArguments.hasOption(PARAM_EXCLUDE_FILES.getOpt())) {
            hasExcludedFilesParam = true;
            excludedFiles = parsedCommandLineArguments.getOptionValues(PARAM_EXCLUDE_FILES.getOpt());
        }

        locationUser = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_USER.getOpt());
        locationPassword = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PWD.getOpt());

        if (locationType == LocationType.perforce && !parsedCommandLineArguments.hasOption(PARAM_LOCATION_PWD.getOpt())) {
            // In Perforce the password is not mandatory in case of a new user
            locationPassword = "";
        }

        locationURL = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_URL.getOpt());
        locationBranch = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_BRANCH.getOpt());
        locationPrivateKey = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PRIVATE_KEY.getOpt());
        if (locationPrivateKey != null) {
            File resultFile = new File(locationPrivateKey);
            if (!resultFile.isAbsolute()) {
                String path = System.getProperty("user.dir");
                locationPrivateKey = path + File.separator + locationPrivateKey;
            }
            privateKey = ParametersUtils.setPrivateKeyFromLocation(locationPrivateKey);
        }

        if (parsedCommandLineArguments.hasOption(PARAM_LOCATION_PORT.getOpt())) {
            String portStr = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PORT.getOpt());
            try {
                locationPort = Integer.parseInt(portStr);
            } catch (Exception e) {
                // ignore
            }
        } else {
            if (locationType == LocationType.svn) {
                if (locationURL.toLowerCase().startsWith("svn://")) {
                    locationPort = 3690;
                } else {
                    locationPort = 80;
                }
            } else if (locationType == LocationType.tfs) {
                locationPort = 8080;
            } else if (locationType == LocationType.perforce) {
                locationPort = 1666;
            }
        }
        isPerforceWorkspaceMode = parsedCommandLineArguments.hasOption(PARAM_WORKSPACE.getOpt());


        String sastLowThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_SAST_LOW_THRESHOLD.getOpt());
        String sastMediumThresholdStr = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_SAST_MEDIUM_THRESHOLD.getOpt());
        String sastHighThresholdStr = ParametersUtils.getOptionalValue(parsedCommandLineArguments, PARAM_SAST_HIGH_THRESHOLD.getOpt());
        if (sastLowThresholdStr != null || sastMediumThresholdStr != null || sastHighThresholdStr != null) {
            isSastThresholdEnabled = true;
            if (sastLowThresholdStr != null) {
                sastLowThresholdValue = Integer.parseInt(sastLowThresholdStr);
            }

            if (sastMediumThresholdStr != null) {
                sastMediumThresholdValue = Integer.parseInt(sastMediumThresholdStr);
            }

            if (sastHighThresholdStr != null) {
                sastHighThresholdValue = Integer.parseInt(sastHighThresholdStr);
            }
        }
    }


    public boolean isSastThresholdEnabled() {
        return isSastThresholdEnabled;
    }

    public int getSastLowThresholdValue() {
        return sastLowThresholdValue;
    }

    public int getSastMediumThresholdValue() {
        return sastMediumThresholdValue;
    }

    public int getSastHighThresholdValue() {
        return sastHighThresholdValue;
    }

    public String getPresetName() {
        return presetName;
    }

    public String getConfiguration() {
        return configuration;
    }

    public boolean isIncrementalScan() {
        return isIncrementalScan;
    }

    public boolean isForceScan() {
        return forceScan;
    }

    public boolean isOsaEnabled() {
        return isOsaEnabled;
    }

    public void setOsaEnabled(boolean osaEnabled) {
        isOsaEnabled = osaEnabled;
    }

    public String getReportType() {
        return reportType;
    }

    public String getReportFile() {
        return reportFile;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }

    public String getLocationURL() {
        return locationURL;
    }

    public String getLocationBranch() {
        return locationBranch;
    }

    public String getLocationUser() {
        return locationUser;
    }

    public String getLocationPassword() {
        return locationPassword;
    }

    public String getLocationPrivateKey() {
        return locationPrivateKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public Integer getLocationPort() {
        return locationPort;
    }

    public boolean isPerforceWorkspaceMode() {
        return isPerforceWorkspaceMode;
    }

    public String[] getExcludedFolders() {
        return excludedFolders;
    }

    public boolean isHasExcludedFoldersParam() {
        return hasExcludedFoldersParam;
    }

    public String[] getExcludedFiles() {
        return excludedFiles;
    }

    public boolean isHasExcludedFilesParam() {
        return hasExcludedFilesParam;
    }

    public void setPerforceWorkspaceMode(boolean perforceWorkspaceMode) {
        isPerforceWorkspaceMode = perforceWorkspaceMode;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_LOCATION_USER);
        commandLineOptions.addOption(PARAM_LOCATION_PWD);
        commandLineOptions.addOption(PARAM_LOCATION_URL);
        commandLineOptions.addOption(PARAM_LOCATION_PORT);
        commandLineOptions.addOption(PARAM_LOCATION_BRANCH);
        commandLineOptions.addOption(PARAM_LOCATION_PRIVATE_KEY);
        commandLineOptions.addOption(PARAM_PRESET);
        commandLineOptions.addOption(PARAM_CONFIGURATION);
        commandLineOptions.addOption(PARAM_INCREMENTAL);
        commandLineOptions.addOption(PARAM_FORCE_SCAN);
        commandLineOptions.addOption(PARAM_WORKSPACE);
        commandLineOptions.addOption(PARAM_ENABLE_OSA);
        commandLineOptions.addOption(PARAM_SAST_LOW_THRESHOLD);
        commandLineOptions.addOption(PARAM_SAST_MEDIUM_THRESHOLD);
        commandLineOptions.addOption(PARAM_SAST_HIGH_THRESHOLD);

        commandLineOptions.addOption(PARAM_XML_FILE);
        OptionGroup reportGroup = new OptionGroup();
        reportGroup.setRequired(false);
        reportGroup.addOption(PARAM_PDF_FILE);
        reportGroup.addOption(PARAM_CSV_FILE);
        reportGroup.addOption(PARAM_RTF_FILE);
        commandLineOptions.addOptionGroup(reportGroup);
        commandLineOptions.addOption(PARAM_EXCLUDE_FOLDERS);
        commandLineOptions.addOption(PARAM_EXCLUDE_FILES);
    }

    @Override
    public String getMandatoryParams() {
        return cliMandatoryParameters.getMandatoryParams() + cliSharedParameters.getParamLocationType() + " locationType ";
    }

    public String getOptionalParams() {
        return "[ " + PARAM_XML_FILE + " results.xml ] "
                + "[ " + PARAM_PDF_FILE + " results.pdf ] "
                + "[ " + PARAM_CSV_FILE + " results.csv ] "
                + "[ " + PARAM_EXCLUDE_FOLDERS + " \"DirName1,DirName2,DirName3\" ] "
                + "[ " + PARAM_EXCLUDE_FILES + " \"FileName1,FileName2,FileName3\" ] "
                + "[ " + cliSharedParameters.getParamLogFile() + " logFile.log ] "
                + "[ " + cliSharedParameters.getParamConfigFile() + " config ] ";
    }

    @Override
    public String getKeyDescriptions() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(cliMandatoryParameters.getKeyDescriptions());
        keys.append(cliSharedParameters.getParamLocationType());
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Location type of files for scan. Mandatory\n");

        return keys.toString();
    }

    public String getOptionalKeyDescriptions() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(cliSharedParameters.getParamLogFile());
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append(KEY_DESCR_INTEND_SINGLE);
        keys.append("- Name or path to log file. Optional.\n");

        keys.append(cliSharedParameters.getParamConfigFile());
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append(KEY_DESCR_INTEND_SINGLE);
        keys.append("- Name or path to config file. Optional.\n");

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
        keys.append("- Comma separated list of folder path patterns to exclude from scan. Example: -LocationPathExclude “**\\test*” excludes all folders which start with “test” prefix. Optional.\n");

        keys.append(leftSpacing);
        keys.append(PARAM_EXCLUDE_FILES);
        keys.append(KEY_DESCR_INTEND_SINGLE);
        keys.append("- Comma separated list of file name patterns to exclude from scan. Example: -LocationFilesExclude “*.class” excludes all .class files. Optional.\n");

        keys.append(leftSpacing);
        keys.append(PARAM_EXCLUDE_FILES);
        keys.append(KEY_DESCR_INTEND_SINGLE);
        keys.append("- Comma separated list of file name patterns to exclude from scan. Example: -LocationFilesExclude “*.class” excludes all .class files. Optional.\n");

        return keys.toString();
    }

    OptionGroup getSASTScanParamsOptionGroup() {
        OptionGroup sastParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            sastParamsOptionGroup.addOption(opt);
        }

        return sastParamsOptionGroup;
    }
}
