package com.checkmarx.cxconsole.utils;

import org.apache.commons.cli.CommandLine;

import java.io.File;

import static com.checkmarx.cxconsole.commands.OsaScanCommand.*;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_CONFIGURATION;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_CSV_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_ENABLE_OSA;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_EXCLUDE_FILES;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_EXCLUDE_FOLDERS;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_FORCE_SCAN;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_HOST;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_INCREMENTAL;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_BRANCH;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_PATH;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_PORT;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_PRIVATE_KEY;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_PWD;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_TYPE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_URL;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOCATION_USER;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_LOG_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_EXCLUDE_FILES;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_EXCLUDE_FOLDERS;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_HTML_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_INCLUDE_FILES;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_JSON;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_LOCATION_PATH;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_OSA_PDF_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PASSWORD;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PDF_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PRESET;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PRIVATE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_PRJ_NAME;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_RTF_FILE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_SAST_HIGH_THRESHOLD;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_SAST_LOW_THRESHOLD;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_SAST_MEDIUM_THRESHOLD;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_SCAN_COMMENT;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_USER;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_USE_SSO;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_VERBOSE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_WORKSPACE;
import static com.checkmarx.cxconsole.commands.ScanCommand.PARAM_XML_FILE;


/**
 * Parameter container for
 *
 * @author Oleksiy Mysnyk
 */
public class ScanParams {

    private String host;
    private String originHost;
    private String user;
    private String password;
    private String srcPath;
    private String projName;
    private String fullProjName;
    private String folderProjName;
    private String presetName;
    private String xmlFile;
    private String reportFile;
    private String reportType;
    private String logFile = "cx_scan.log";
    private boolean isVerbose = false;
    private boolean isVisibleOthers = true;
    private boolean isValidateFix = false;
    private boolean ignoreScanWithUnchangedSource = true;
    private String spFolderName;
    private LocationType locationType;
    private String locationPath;
    private String scanComment;
    private String locationURL;
    private Integer locationPort;
    //private String locationRepository;
    private String locationBranch;
    private String locationUser;
    private String locationPassword;
    private String locationPrivateKey;
    private String privateKey;
    private String configuration;
    private String[] excludedFolders = new String[]{};
    private boolean hasExcludedFoldersParam = false;
    private String[] excludedFiles = new String[]{};
    private boolean hasExcludedFilesParam = false;
    private boolean isSsoLoginUsed = false;
    private boolean hasPasswordParam = false;
    private boolean hasUserParam = false;
    private boolean isPerforceWorkspaceMode = false;
    private boolean isOsaEnabled = false;
    private String[] osaLocationPath = new String[]{};
    private String[] osaExcludedFolders = new String[]{};
    private String[] osaExcludedFiles = new String[]{};
    private String[] osaIncludedFiles = new String[]{};
    private String osaReportPDF;
    private String osaReportHTML;
    private String osaJson;
    private boolean isSastThresholdEnabled = false;
    private int sastLowThreshold = -1;
    private int sastMediumThreshold = -1;
    private int sastHighThreshold = -1;
    private boolean isOsaThresholdEnabled = false;
    private int osaLowThreshold = -1;
    private int osaMediumThreshold = -1;
    private int osaHighThreshold = -1;

    public ScanParams(CommandLine commandLine) {
        this.host = commandLine.getOptionValue(PARAM_HOST.getOpt());
        this.originHost = commandLine.getOptionValue(PARAM_HOST.getOpt());
        this.user = commandLine.getOptionValue(PARAM_USER.getOpt());
        this.password = commandLine.getOptionValue(PARAM_PASSWORD.getOpt());

        this.presetName = commandLine.getOptionValue(PARAM_PRESET.getOpt());

        this.xmlFile = commandLine.getOptionValue(PARAM_XML_FILE.getOpt());

        if (commandLine.hasOption(PARAM_PDF_FILE.getOpt())) {
            this.reportType = "PDF";
            this.reportFile = commandLine.getOptionValue(PARAM_PDF_FILE.getOpt());
        }

        if (commandLine.hasOption(PARAM_CSV_FILE.getOpt())) {
            this.reportType = "CSV";
            this.reportFile = commandLine.getOptionValue(PARAM_CSV_FILE.getOpt());
        }

        if (commandLine.hasOption(PARAM_RTF_FILE.getOpt())) {
            this.reportType = "RTF";
            this.reportFile = commandLine.getOptionValue(PARAM_RTF_FILE.getOpt());
        }

        this.logFile = commandLine.getOptionValue(PARAM_LOG_FILE.getOpt());
        isVerbose = commandLine.hasOption(PARAM_VERBOSE.getOpt());


        //Scan command
        fullProjName = commandLine.getOptionValue(PARAM_PRJ_NAME.getOpt()); //params.get(ScanCommand.PARAM_PRJ_NAME.toUpperCase());
        if (fullProjName != null) {
            fullProjName = fullProjName.replaceAll("/", "\\\\");
            String parts[] = fullProjName.split("\\\\");
            if (parts == null || !(parts.length > 0)) {
                projName = fullProjName;
            } else {
                projName = parts[parts.length - 1];
            }
        }

        if (commandLine.hasOption(PARAM_LOCATION_TYPE.getOpt())) {
            locationType = LocationType.byName(commandLine.getOptionValue(PARAM_LOCATION_TYPE.getOpt()));
        }

        scanComment = commandLine.getOptionValue(PARAM_SCAN_COMMENT.getOpt());

        locationPath = commandLine.getOptionValue(PARAM_LOCATION_PATH.getOpt());
        if (locationType == LocationType.folder && locationPath != null) {
            File resultFile = new File(locationPath);
            if (!resultFile.isAbsolute()) {
                String path = System.getProperty("user.dir");
                locationPath = path + File.separator + locationPath;
            }
        }
        locationUser = commandLine.getOptionValue(PARAM_LOCATION_USER.getOpt());
        locationPassword = commandLine.getOptionValue(PARAM_LOCATION_PWD.getOpt());

        if (locationType == LocationType.perforce && !commandLine.hasOption(PARAM_LOCATION_PWD.getOpt())) {
            // In Perforce the password is not mandatory in case of a new user
            locationPassword = "";
        }

        locationURL = commandLine.getOptionValue(PARAM_LOCATION_URL.getOpt());
        locationBranch = commandLine.getOptionValue(PARAM_LOCATION_BRANCH.getOpt());
        locationPrivateKey = commandLine.getOptionValue(PARAM_LOCATION_PRIVATE_KEY.getOpt());
        if (locationPrivateKey != null) {
            File resultFile = new File(locationPrivateKey);
            if (!resultFile.isAbsolute()) {
                String path = System.getProperty("user.dir");
                locationPrivateKey = path + File.separator + locationPrivateKey;
            }
        }

        if (commandLine.hasOption(PARAM_LOCATION_PORT.getOpt())) {
            String portStr = commandLine.getOptionValue(PARAM_LOCATION_PORT.getOpt());
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
        presetName = commandLine.getOptionValue(PARAM_PRESET.getOpt());
        configuration = commandLine.getOptionValue(PARAM_CONFIGURATION.getOpt());
        isValidateFix = commandLine.hasOption(PARAM_INCREMENTAL.getOpt());
        isVisibleOthers = !commandLine.hasOption(PARAM_PRIVATE.getOpt());
        isSsoLoginUsed = commandLine.hasOption(PARAM_USE_SSO.getOpt());
        ignoreScanWithUnchangedSource = !commandLine.hasOption(PARAM_FORCE_SCAN.getOpt());
        hasUserParam = commandLine.hasOption(PARAM_USER.getOpt());
        hasPasswordParam = commandLine.hasOption(PARAM_PASSWORD.getOpt());
        isPerforceWorkspaceMode = commandLine.hasOption(PARAM_WORKSPACE.getOpt());

        if (commandLine.hasOption(PARAM_EXCLUDE_FOLDERS.getOpt())) {
            hasExcludedFoldersParam = true;
            excludedFolders = commandLine.getOptionValues(PARAM_EXCLUDE_FOLDERS.getOpt());
        }

        if (commandLine.hasOption(PARAM_EXCLUDE_FILES.getOpt())) {
            hasExcludedFilesParam = true;
            excludedFiles = commandLine.getOptionValues(PARAM_EXCLUDE_FILES.getOpt());
        }

        isOsaEnabled = commandLine.hasOption(PARAM_ENABLE_OSA.getOpt());
        osaLocationPath = commandLine.getOptionValues(PARAM_OSA_LOCATION_PATH.getOpt());
        osaExcludedFolders = commandLine.getOptionValues(PARAM_OSA_EXCLUDE_FOLDERS.getOpt());
        osaExcludedFiles = commandLine.getOptionValues(PARAM_OSA_EXCLUDE_FILES.getOpt());
        osaIncludedFiles = commandLine.getOptionValues(PARAM_OSA_INCLUDE_FILES.getOpt());
        osaReportHTML = getOptionalValue(commandLine, PARAM_OSA_HTML_FILE.getOpt());
        osaReportPDF = getOptionalValue(commandLine, PARAM_OSA_PDF_FILE.getOpt());
        osaJson = getOptionalValue(commandLine, PARAM_OSA_JSON.getOpt());

        if (commandLine.hasOption(PARAM_SAST_LOW_THRESHOLD.getOpt()) || commandLine.hasOption(PARAM_SAST_MEDIUM_THRESHOLD.getOpt()) || commandLine.hasOption(PARAM_SAST_HIGH_THRESHOLD.getOpt())) {
            isSastThresholdEnabled = true;

            sastLowThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_SAST_LOW_THRESHOLD.getOpt()));
            sastMediumThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_SAST_MEDIUM_THRESHOLD.getOpt()));
            sastHighThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_SAST_HIGH_THRESHOLD.getOpt()));
        }

        if (commandLine.hasOption(PARAM_OSA_LOW_THRESHOLD.getOpt()) || commandLine.hasOption(PARAM_OSA_MEDIUM_THRESHOLD.getOpt()) || commandLine.hasOption(PARAM_OSA_HIGH_THRESHOLD.getOpt())) {
            isOsaThresholdEnabled = true;

            osaLowThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_OSA_LOW_THRESHOLD.getOpt()));
            osaMediumThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_OSA_MEDIUM_THRESHOLD.getOpt()));
            osaHighThreshold = Integer.parseInt(getOptionalValue(commandLine, PARAM_OSA_HIGH_THRESHOLD.getOpt()));
        }
    }


    private String getOptionalValue(CommandLine commandLine, String opt) {
        String ret = commandLine.getOptionValue(opt);
        if (ret == null && commandLine.hasOption(opt)) {
            ret = "";
        }
        return ret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOriginHost() {
        return originHost;
    }

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getReportType() {
        return reportType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String file) {
        this.reportFile = file;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public void setVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }

    public String getProjName() {
        return projName;
    }

    public String getFullProjName() {
        return fullProjName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public String getFolderProjName() {
        return folderProjName;
    }

    public void setProjFolderName(String projFolderName) {
        this.folderProjName = projFolderName;
    }

    public boolean isVisibleOthers() {
        return isVisibleOthers;
    }

    public boolean isValidateFix() {
        return isValidateFix;
    }

    public String getSpFolderName() {
        return spFolderName;
    }

    public String getScanComment() {
        return scanComment;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType type) {
        locationType = type;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }

    public String getLocationURL() {
        return locationURL;
    }

//	public String getLocationRepository() {
//		return locationRepository;
//	}

    public String getLocationBranch() {
        return locationBranch;
    }

    public Integer getLocationPort() {
        return locationPort;
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

    public String getConfiguration() {
        return configuration;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String[] getExcludedFolders() {
        return excludedFolders;
    }

    public void setExcludedFolders(String[] excludedFolders) {
        this.excludedFolders = excludedFolders;
    }

    public boolean hasExcludedFoldersParam() {
        return hasExcludedFoldersParam;
    }

    public void setHasExcludedFoldersParam(boolean hasExcludedFoldersParam) {
        this.hasExcludedFoldersParam = hasExcludedFoldersParam;
    }

    public String[] getExcludedFiles() {
        return excludedFiles;
    }

    public boolean hasExcludedFilesParam() {
        return hasExcludedFilesParam;
    }

    public boolean isSsoLoginUsed() {
        return isSsoLoginUsed;
    }

    public boolean hasUserParam() {
        return hasUserParam;
    }

    public boolean hasPasswordParam() {
        return hasPasswordParam;
    }

    public boolean isIgnoreScanWithUnchangedSource() {
        return ignoreScanWithUnchangedSource;
    }

    public void setIgnoreScanWithUnchangedSource(boolean ignoreScanWithUnchangedSource) {
        this.ignoreScanWithUnchangedSource = ignoreScanWithUnchangedSource;
    }

    public boolean getIsPerforceWorkspaceMode() {
        return isPerforceWorkspaceMode;
    }

    public void setIsPerforceWorkspaceMode(boolean isPerforceWorkspaceMode) {
        this.isPerforceWorkspaceMode = isPerforceWorkspaceMode;
    }

    public boolean isOsaEnabled() {
        return isOsaEnabled;
    }

    public void setOsaEnabled(boolean osaEnabled) {
        isOsaEnabled = osaEnabled;
    }

    public String[] getOsaLocationPath() {
        return osaLocationPath;
    }

    public void setOsaLocationPath(String[] osaLocationPath) {
        this.osaLocationPath = osaLocationPath;
    }

    public String[] getOsaExcludedFolders() {
        return osaExcludedFolders;
    }

    public void setOsaExcludedFolders(String[] osaExcludedFolders) {
        this.osaExcludedFolders = osaExcludedFolders;
    }

    public String[] getOsaExcludedFiles() {
        return osaExcludedFiles;
    }

    public void setOsaExcludedFiles(String[] osaExcludedFiles) {
        this.osaExcludedFiles = osaExcludedFiles;
    }

    public String[] getOsaIncludedFiles() {
        return osaIncludedFiles;
    }

    public void setOsaIncludedFiles(String[] osaIncludedFiles) {
        this.osaIncludedFiles = osaIncludedFiles;
    }

    public String getOsaReportPDF() {
        return osaReportPDF;
    }

    public void setOsaReportPDF(String osaReportPDF) {
        this.osaReportPDF = osaReportPDF;
    }

    public String getOsaReportHTML() {
        return osaReportHTML;
    }

    public void setOsaReportHTML(String osaReportHTML) {
        this.osaReportHTML = osaReportHTML;
    }

    public String getOsaJson() {
        return osaJson;
    }

    public void setOsaJson(String osaJson) {
        this.osaJson = osaJson;
    }

    public boolean isSastThresholdEnabled() {
        return isSastThresholdEnabled;
    }

    public void setSastThresholdEnabled(boolean sastThresholdEnabled) {
        this.isSastThresholdEnabled = sastThresholdEnabled;
    }

    public int getSastLowThreshold() {
        return sastLowThreshold;
    }

    public void setSastLowThreshold(int sastLowThreshold) {
        this.sastLowThreshold = sastLowThreshold;
    }

    public int getSastMediumThreshold() {
        return sastMediumThreshold;
    }

    public void setSastMediumThreshold(int sastMediumThreshold) {
        this.sastMediumThreshold = sastMediumThreshold;
    }

    public int getSastHighThreshold() {
        return sastHighThreshold;
    }

    public void setSastHighThreshold(int sastHighThreshold) {
        this.sastHighThreshold = sastHighThreshold;
    }

    public boolean isOsaThresholdEnabled() {
        return isOsaThresholdEnabled;
    }

    public void setOsaThresholdEnabled(boolean osaThresholdEnabled) {
        this.isOsaThresholdEnabled = osaThresholdEnabled;
    }

    public int getOsaLowThreshold() {
        return osaLowThreshold;
    }

    public void setOsaLowThreshold(int osaLowThreshold) {
        this.osaLowThreshold = osaLowThreshold;
    }

    public int getOsaMediumThreshold() {
        return osaMediumThreshold;
    }

    public void setOsaMediumThreshold(int osaMediumThreshold) {
        this.osaMediumThreshold = osaMediumThreshold;
    }

    public int getOsaHighThreshold() {
        return osaHighThreshold;
    }

    public void setOsaHighThreshold(int osaHighThreshold) {
        this.osaHighThreshold = osaHighThreshold;
    }
}
