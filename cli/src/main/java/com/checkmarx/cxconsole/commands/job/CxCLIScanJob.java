package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.cxviewer.ws.results.GetProjectConfigResult;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import com.checkmarx.cxviewer.ws.results.RunScanResult;
import com.checkmarx.cxviewer.ws.results.UpdateScanCommentResult;
import com.checkmarx.login.soap.CxSoapSASTClient;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.dto.PresetDTO;
import com.checkmarx.login.soap.providers.ScanPrerequisitesValidator;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import com.checkmarx.parameters.CLIScanParameters;
import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.checkmarx.cxconsole.commands.constants.LocationType.folder;
import static com.checkmarx.exitcodes.Constants.ExitCodes.GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.thresholds.ThresholdResolver.resolveThresholdExitCode;

public class CxCLIScanJob extends CxScanJob {

    private static final int LOW_VULNERABILITY_RESULTS = 0;
    private static final int MEDIUM_VULNERABILITY_RESULTS = 1;
    private static final int HIGH_VULNERABILITY_RESULTS = 2;

    private byte[] zippedSourcesBytes;
    private long projectId = -1;

    private List<PresetDTO> presets;
    private PresetDTO selectedPreset;
    private List<ConfigurationDTO> configs;
    private ConfigurationDTO selectedConfig;
    private GetProjectConfigResult projectConfig;
    private int osaExitCode = SCAN_SUCCEEDED_EXIT_CODE;
    private int sastExitCode = SCAN_SUCCEEDED_EXIT_CODE;
    private boolean isAsyncScan;

    private CxSoapSASTClient cxSoapSASTClient;

    public CxCLIScanJob(CLIScanParameters params, boolean isAsyncScan) {
        super(params);
        this.isAsyncScan = isAsyncScan;
    }

    @Override
    public Integer call() throws Exception {

        log.info("Project name is \"" + params.getCliMandatoryParameters().getProjectName() + "\"");


        // Connect
        cxSoapLoginClient = ConfigMgr.getWSMgr();
//        String hostWithProtocol = resolveServerProtocol(params.getCliMandatoryParameters().getOriginalHost());
//        if (hostWithProtocol != null) {
//            params.getCliMandatoryParameters().setOriginalHost(hostWithProtocol);
//            log.trace("Server found in: " + params.getCliMandatoryParameters().getOriginalHost());
//        } else {
//            throw new Exception("Failed to validate server connectivity");
//        }
        URL wsdlLocation = new URL(SoapClientUtils.buildHostWithWSDL(params.getCliMandatoryParameters().getOriginalHost()));

        // Login
        login(wsdlLocation);

        // locate project and get corresponding projectId
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Read preset and configuration settings");
        }
        locateProjectOnServer(params.getCliMandatoryParameters().getProjectNameWithPath(), sessionId);

        if (params.getCliSharedParameters().getLocationType() == null && this.projectConfig != null) {
            if (!this.projectConfig.getProjectConfig().getSourceCodeSettings().getSourceOrigin().equals(SourceLocationType.LOCAL)) {
                params.getCliSharedParameters().setLocationType(getLocationType(this.projectConfig.getProjectConfig().getSourceCodeSettings()));
                if (params.getCliSharedParameters().getLocationType() == LocationType.perforce) {
                    boolean isworkspace = (this.projectConfig.getProjectConfig().getSourceCodeSettings().getSourceControlSetting().getPerforceBrowsingMode() == CxWSPerforceBrowsingMode.WORKSPACE);
                    params.getCliSastParameters().setPerforceWorkspaceMode(isworkspace);
                }
            }
        }

        if (params.getCliSharedParameters().getLocationType() == folder) {
            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);

            if (!packFolder(maxZipSize)) {
                throw new Exception("Error during packing sources.");
            }

            // check packed sources size
            if (zippedSourcesBytes == null || zippedSourcesBytes.length == 0) {
                // if size is greater that restricted value, stop scan
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Packing sources has failed: empty packed source ");
                }
                throw new Exception("Packing sources has failed: empty packed source ");
            }

            if (zippedSourcesBytes.length > maxZipSize) {
                // if size greater that restricted value, stop scan
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Packed project size is greater than " + maxZipSize);
                }
                throw new Exception("Packed project size is greater than " + maxZipSize);
            }
        }

        // check project src type
//		if (log.isEnabledFor(Level.INFO)) {
//			log.info("Checking project source type");
//		}
        cxSoapSASTClient = new CxSoapSASTClient(cxSoapLoginClient.getCxSoapClient());
        checkProjectType(sessionId, projectId);

        // request scan
        log.info("Request SAST scan");

        requestScan(sessionId);

        log.info("SAST scan created successfully");


        // wait for scan completion
        if (isAsyncScan) {
            log.info("Waiting for SAST scan to queue.");
        } else {
            log.info("Waiting for SAST scan to finish.");
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        WaitScanCompletionJob waiterJob = new WaitScanCompletionJob(cxSoapSASTClient, sessionId, runId, isAsyncScan);
        waiterJob.setLog(log);
        try {
            Future<Boolean> furute = executor.submit(waiterJob);
            // wait for scan completion
            furute.get();

            scanId = waiterJob.getScanId();
            if (isAsyncScan) {
                log.info("SAST scan queued. Job finished");
            } else {
                log.info("SAST scan finished. Retrieving scan results");
            }

        } catch (ExecutionException e) {
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Error occured during scan progress monitoring", e.getCause());
            }
            String causeMessage = e.getCause().getMessage();
            if (causeMessage == null) {
                causeMessage = "";
            }
            throw new Exception(causeMessage);
        } finally {
            executor.shutdownNow();
        }

        if (!isAsyncScan) {
            String scanSummary = cxSoapSASTClient.getScanSummary(params.getCliMandatoryParameters().getOriginalHost(), sessionId, scanId);
            int[] scanResults = parseScanSummary(scanSummary);
            printSASTResultsToConsole(scanResults);

            //SAST threshold calculation
            if (params.getCliSastParameters().isSastThresholdEnabled()) {
                ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.SAST_SCAN, params.getCliSastParameters().getSastHighThresholdValue(), params.getCliSastParameters().getSastMediumThresholdValue(),
                        params.getCliSastParameters().getSastLowThresholdValue(), scanResults[HIGH_VULNERABILITY_RESULTS], scanResults[MEDIUM_VULNERABILITY_RESULTS], scanResults[LOW_VULNERABILITY_RESULTS]);
                sastExitCode = resolveThresholdExitCode(thresholdDto, log);
            }
        }

        if (params.getCliSastParameters().isForceScan() && scanId == -1 && waiterJob.getCurrentStatusEnum() == CurrentStatusEnum.FINISHED) {
            log.info("Scan finished with ScanId = (-1): finish Scan Job");
            return SCAN_SUCCEEDED_EXIT_CODE;
        }

        //update scan comment
        String comment = params.getCliSharedParameters().getScanComment();
        if (comment != null) {
            UpdateScanCommentResult result = cxSoapSASTClient.updateScanComment(sessionId, scanId, comment);
            if (!result.isSuccessfulResponse()) {
                log.warn("Cannot update the scan comment: " + result.getErrorMessage());
            }
        }


        //SAST reports
        if (params.getCliSastParameters().getReportType() != null) {
            log.info("Report type: " + params.getCliSastParameters().getReportType());
            String resultsPath = params.getCliSastParameters().getReportFile();
            if (resultsPath == null) {
                resultsPath = normalizePathString(params.getCliMandatoryParameters().getProjectName()) + "." + params.getCliSastParameters().getReportType().toLowerCase();
            }
            downloadAndStoreReport(resultsPath, params.getCliSastParameters().getReportType());
        }

        // Store to xml anyway
        String resultsFileName = params.getCliSastParameters().getXmlFile();
        if (resultsFileName == null) {
            resultsFileName = normalizePathString(params.getCliMandatoryParameters().getProjectName()) + ".xml";
        }

        if (!isAsyncScan) {
            storeXMLResults(resultsFileName, cxSoapSASTClient.getScanReport(sessionId, scanId, "XML"));
        }

        //Osa Scan
        ExecutorService osaExecutor = null;
        if (params.getCliSastParameters().isOsaEnabled()) {
            try {
                osaExecutor = Executors.newSingleThreadExecutor();
                CxScanJob job = new CxCLIOsaScanJob(params, cxSoapLoginClient, sessionId, projectId, isAsyncScan);
                job.setLog(log);
                Future<Integer> future = osaExecutor.submit(job);
                osaExitCode = future.get();
            } catch (Exception e) {
                throw new Exception("An error has occurred during OSA scan: " + e.getMessage(), e);

            } finally {
                if (osaExecutor != null) {
                    osaExecutor.shutdownNow();
                }
            }
        }

        if (sastExitCode == SCAN_SUCCEEDED_EXIT_CODE && osaExitCode != SCAN_SUCCEEDED_EXIT_CODE) {
            return osaExitCode;
        } else if (sastExitCode != SCAN_SUCCEEDED_EXIT_CODE && osaExitCode == SCAN_SUCCEEDED_EXIT_CODE) {
            return sastExitCode;
        } else if (sastExitCode != SCAN_SUCCEEDED_EXIT_CODE) {
            return GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE;
        } else {
            return SCAN_SUCCEEDED_EXIT_CODE;
        }
    }

    private int[] parseScanSummary(String scanSummary) throws Exception {
        int[] scanResults = new int[3];
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(scanSummary));
        Document xmlDoc = builder.parse(is);
        xmlDoc.getDocumentElement().normalize();
        NodeList nList = xmlDoc.getElementsByTagName("GetScanSummaryResult");
        Node node = nList.item(0);
        node.getNodeType();
        Element eElement = (Element) node;
        String highStr = eElement.getElementsByTagName("High").item(0).getTextContent();
        if (highStr != null) {
            scanResults[HIGH_VULNERABILITY_RESULTS] = Integer.parseInt(highStr);
        }
        String mediumStr = eElement.getElementsByTagName("Medium").item(0).getTextContent();
        if (highStr != null) {
            scanResults[MEDIUM_VULNERABILITY_RESULTS] = Integer.parseInt(mediumStr);
        }
        String lowStr = eElement.getElementsByTagName("Low").item(0).getTextContent();
        if (lowStr != null) {
            scanResults[LOW_VULNERABILITY_RESULTS] = Integer.parseInt(lowStr);
        }

        return scanResults;
    }

    private void printSASTResultsToConsole(int[] scanResults) {
        log.info("----------------------------Checkmarx Scan Results(CxSAST):-------------------------------");
        log.info("");
        log.info("------------------------");
        log.info("SAST vulnerabilities Summary:");
        log.info("------------------------");
        log.info("SAST high severity results: " + scanResults[HIGH_VULNERABILITY_RESULTS]);
        log.info("SAST medium severity results: " + scanResults[MEDIUM_VULNERABILITY_RESULTS]);
        log.info("SAST low severity results: " + scanResults[LOW_VULNERABILITY_RESULTS]);
        log.info("");
        log.info("-----------------------------------------------------------------------------------------");
    }

    private LocationType getLocationType(SourceCodeSettings scSettings) {
        SourceLocationType slType = scSettings.getSourceOrigin();
        if (slType.equals(SourceLocationType.LOCAL)) {
            return folder;
        } else if (slType.equals(SourceLocationType.SHARED)) {
            return LocationType.shared;
        } else if (slType.equals(SourceLocationType.SOURCE_CONTROL)) {
            RepositoryType rType = scSettings.getSourceControlSetting().getRepository();
            if (rType.equals(RepositoryType.TFS)) {
                return LocationType.tfs;
            } else if (rType.equals(RepositoryType.GIT)) {
                return LocationType.git;
            } else if (rType.equals(RepositoryType.SVN)) {
                return LocationType.svn;
            } else if (rType.equals(RepositoryType.PERFORCE)) {
                return LocationType.perforce;
            }
        }

        return null;
    }

    private void locateProjectOnServer(String projectName, String sessionId) throws Exception {

        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        GetProjectDataResult getPrjsResult = null;
        int count = 0;
        String errMsg = "";

        ScanPrerequisitesValidator scanPrerequisitesValidator = new ScanPrerequisitesValidator(cxSoapLoginClient.getCxSoapClient(), sessionId);

        presets = scanPrerequisitesValidator.getPresetList();
        configs = scanPrerequisitesValidator.getConfigurationList();

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Succeeded get Presets from server");
            log.trace("Succeeded get Configurations from server");
        }
        if (params.getCliSharedParameters().getLocationType() == null) {
            int getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(
                    ConfigMgr.KEY_PROGRESS_INTERVAL);

            while ((getPrjsResult == null || !getPrjsResult.isSuccessfulResponse())
                    && count < retriesNum) {
                if (projectName.contains("/")) {
                    projectName = projectName.replace('/', '\\');
                }
                if (!projectName.contains("\\")) {
                    projectName = "CxServer\\" + projectName;
                } else {
                    if (!projectName.startsWith("CxServer")) {
                        projectName = "CxServer\\" + projectName;
                    }
                }
                try {
                    getPrjsResult = cxSoapSASTClient.getProjectsDisplayData(sessionId);
                } catch (Throwable e) {
                    errMsg = e.getMessage();
                    count++;
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Error during fetching existing projects dto.", e);
                    }

                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Error occurred during fetching existing projects dto: " + errMsg + ". Operation retry " + count);
                    }
                }

                if ((getPrjsResult != null) && !getPrjsResult.isSuccessfulResponse()) {
                    errMsg = getPrjsResult.getErrorMessage();
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Existing projects dto fetching was unsuccessful.");
                    }
                    count++;
                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Existing projects dto fetching unsuccessful: " + getPrjsResult.getErrorMessage() + ". Operation retry " + count);
                    }
                }

                if ((getPrjsResult == null || !getPrjsResult.isSuccessfulResponse())
                        && count < retriesNum) {
                    try {
                        Thread.sleep(getStatusInterval * 1000);
                    } catch (InterruptedException ex) {
                        // no-op
                    }
                }
            }

            if ((getPrjsResult != null) && !getPrjsResult.isSuccessfulResponse()) {
                throw new Exception("Existing projects dto fetching was unsuccessful. " + (errMsg == null ? "" : errMsg));
            } else if (getPrjsResult == null) {
                throw new Exception("Error occurred during existing projects dto fetching. " + errMsg);
            } else {
                List<ProjectDisplayData> prjData = getPrjsResult.getProjectData();
                for (ProjectDisplayData projectData : prjData) {
                    String fullProjectName = "";
                    //SP->Company->Users
                    /*fullProjectName = projectData.getServiceProvider()
                            + "\\" + projectData.getCompany()
							+ "\\" + projectData.getGroup() 
							+ "\\" + projectData.getProjectName();*/
                    String[] locationParts = projectData.getGroup().split("\\-\\>");
                    if (locationParts != null && locationParts.length > 0) {
                        fullProjectName = locationParts[0];
                        for (int i = 1; i < locationParts.length; i++) {
                            fullProjectName += ("\\" + locationParts[i]);
                        }
                    } else {
                        fullProjectName = projectData.getGroup();
                    }
                    fullProjectName += "\\" + projectData.getProjectName();
                    if (!fullProjectName.startsWith("CxServer")) {
                        fullProjectName = "CxServer\\" + fullProjectName;
                    }
                    if (fullProjectName.equals(projectName)) {
                        //projectData.
                        projectId = projectData.getProjectID();
                        break;
                    }
                }

                if (projectId == -1) {
                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Project " + projectName + " does not exist.");
                    }
                    log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
                    log.error("Scan command failed since no source location was provided.");
                    throw new Exception("Scan command failed since no source location was provided.");
                }
            }

            projectConfig = cxSoapSASTClient.getProjectConfiguration(sessionId, projectId);
            if ((projectConfig != null) && !projectConfig.isSuccessfulResponse()) {
                throw new Exception("Project configuration fetching was unsuccessful. "
                        + (projectConfig.getErrorMessage() == null ? "" : projectConfig.getErrorMessage()));
            }

            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("Existing projects dto response:" + getPrjsResult);
            }
        }
    }

    private void checkProjectType(String sessionId, long projectId) throws Exception {

        if (params.getCliSastParameters().getPresetName() != null) {
            selectedPreset = null;
            if (presets != null) {
                for (PresetDTO preset : presets) {
                    if (preset.getName().equals(params.getCliSastParameters().getPresetName())) {
                        selectedPreset = preset;
                        break;
                    }
                }

                if (selectedPreset == null) {
                    throw new Exception("Preset [" + params.getCliSastParameters().getPresetName() + "] is not found");
                }
            }
        } else {
            if (presets != null && !presets.isEmpty()) {
                // Zero preset will be send. Server will decide what preset to use.
                selectedPreset = new PresetDTO(0, null);
            }
        }

        if (params.getCliSastParameters().getConfiguration() != null) {
            selectedConfig = null;
            if (configs != null) {
                for (ConfigurationDTO config : configs) {
                    if (config.getName().equals(
                            params.getCliSastParameters().getConfiguration())) {
                        selectedConfig = config;
                        break;
                    }
                }

                if (selectedConfig == null) {
                    throw new Exception("Configuration ["
                            + params.getCliSastParameters().getConfiguration() + "] is not found");
                }
            }
        } else {
            if (configs != null) {
                for (ConfigurationDTO config : configs) {
                    if (config.getName().equals("Default Configuration")) {
                        selectedConfig = config;
                        break;
                    }
                }
            }
        }
    }

    private void requestScan(String sessionId) throws Exception {

        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        RunScanResult runScanResult = null;
        int count = 0;
        String errMsg = "";

        SourceLocationType locationType = null;
        RepositoryType repoType = null;
        if (params.getCliSharedParameters().getLocationType() != null) {
            switch (params.getCliSharedParameters().getLocationType()) {
                case folder:
                    locationType = SourceLocationType.LOCAL;
                    break;
                case shared:
                    locationType = SourceLocationType.SHARED;
                    break;
                case tfs:
                    repoType = RepositoryType.TFS;
                    locationType = SourceLocationType.SOURCE_CONTROL;
                    break;
                case svn:
                    repoType = RepositoryType.SVN;
                    locationType = SourceLocationType.SOURCE_CONTROL;
                    break;
                case perforce:
                    repoType = RepositoryType.PERFORCE;
                    locationType = SourceLocationType.SOURCE_CONTROL;
                    break;
                case git:
                    repoType = RepositoryType.GIT;
                    locationType = SourceLocationType.SOURCE_CONTROL;
                    break;
            }
        } else {
            locationType = projectConfig.getProjectConfig().getSourceCodeSettings().getSourceOrigin();
            if (locationType == SourceLocationType.LOCAL) {
                log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
                log.error("Scan command failed since no source location was provided.");
                throw new Exception("Scan command failed since no source location was provided.");
            }
        }

        // Start scan
        int getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

        while ((runScanResult == null || !runScanResult.isSuccessfulResponse()) && count < retriesNum) {

            try {
                if (params.getCliSharedParameters().getLocationType() == null) {
                    ProjectSettings prjSett = projectConfig.getProjectConfig().getProjectSettings();
                    SourceCodeSettings srcCodeSett = projectConfig.getProjectConfig().getSourceCodeSettings();
                    if (params.getCliSastParameters().getLocationUser() != null && params.getCliSastParameters().getLocationPassword() != null) {
                        Credentials creds = new Credentials();
                        creds.setUser(params.getCliSastParameters().getLocationUser());
                        creds.setPass(params.getCliSastParameters().getLocationPassword());
                        srcCodeSett.setUserCredentials(creds);
                    }

                    if (params.getCliSastParameters().getLocationBranch() != null) {
                        srcCodeSett.getSourceControlSetting().setGITBranch(params.getCliSastParameters().getLocationBranch());
                    }

                    SourceFilterPatterns filterPatterns = new SourceFilterPatterns();
                    filterPatterns.setExcludeFilesPatterns(StringUtils.join(params.getCliSastParameters().getExcludedFiles(), ','));
                    filterPatterns.setExcludeFoldersPatterns(StringUtils.join(params.getCliSastParameters().getExcludedFolders(), ','));
                    srcCodeSett.setSourceFilterLists(filterPatterns);

                    runScanResult = cxSoapSASTClient.cliScan(sessionId, prjSett, srcCodeSett, params.getCliSastParameters().isIncrementalScan(), params.getCliSharedParameters().isVisibleOthers(), params.getCliSastParameters().isForceScan());
                } else {
                    runScanResult = cxSoapSASTClient.cliScan(sessionId, params.getCliMandatoryParameters().getProjectNameWithPath(),
                            (selectedPreset == null ? null : selectedPreset.getId()),
                            (selectedConfig == null ? null : selectedConfig.getId()),
                            locationType, params.getCliSharedParameters().getLocationPath(), zippedSourcesBytes,
                            params.getCliSastParameters().getLocationUser(), params.getCliSastParameters().getLocationPassword(),
                            repoType, params.getCliSastParameters().getLocationURL(),
                            params.getCliSastParameters().getLocationPort(), params.getCliSastParameters().getLocationBranch(),
                            params.getCliSastParameters().getPrivateKey(),
                            params.getCliSastParameters().isIncrementalScan(), params.getCliSharedParameters().isVisibleOthers(),
                            params.getCliSastParameters().getExcludedFiles(), params.getCliSastParameters().getExcludedFolders(), params.getCliSastParameters().isForceScan(),
                            params.getCliSastParameters().isPerforceWorkspaceMode());
                }
            } catch (Throwable e) {
                errMsg = e.getMessage();
                count++;
                if (log.isEnabledFor(Level.TRACE)) {
                    log.trace("Error during quering existing project scan run.", e);
                }

                if (log.isEnabledFor(Level.INFO)) {
                    log.info("Error occurred during existing project scan request: " + errMsg + ". Operation retry " + count);
                }
            }

            if ((runScanResult != null) && !runScanResult.isSuccessfulResponse()) {
                errMsg = runScanResult.getErrorMessage();
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Existing project scan request was unsuccessful.");
                }
                count++;
                if (log.isEnabledFor(Level.INFO)) {
                    log.info("Existing project scan run request unsuccessful: " + runScanResult.getErrorMessage() + ". Operation retry " + count);
                }
            }

            if ((runScanResult == null || !runScanResult.isSuccessfulResponse()) && count < retriesNum) {
                try {
                    Thread.sleep(getStatusInterval * 1000);
                } catch (InterruptedException ex) {
                    // no-op
                }
            }
        }

        if ((runScanResult != null) && !runScanResult.isSuccessfulResponse()) {
            throw new Exception("Existing project scan request was unsuccessful. " + (errMsg == null ? "" : errMsg));
        } else if (runScanResult == null) {
            throw new Exception("Error occurred during existing project scan. " + errMsg);
        }

        if (log.isEnabledFor(Level.TRACE)) {
            log.trace("Existing project scan request response:" + runScanResult);
        }
        runId = runScanResult.getRunId();

        if (projectId == -1) {
            projectId = runScanResult.getProjectId();
        }
    }

    private boolean packFolder(long maxZipSize) {

        if (!isProjectDirectoryValid()) {
            return false;
        }
        ZipListener listener = new ZipListener() {
            @Override
            public void updateProgress(String fileName, long size) {
            }
        };
        try {
            Zipper zipper = new Zipper();
            String[] excludePatterns = createExcludePatternsArray();
            String[] includeAllPatterns = new String[]{"**/*"};//the default is to include all files
            zippedSourcesBytes = zipper.zip(new File(params.getCliSharedParameters().getLocationPath()), excludePatterns, includeAllPatterns, maxZipSize, listener);

        } catch (Exception e) {
            log.trace(e);
            log.error("Error occurred during zipping source files. Error message: " + e.getMessage());

            return false;
        }
        return true;
    }

    private String[] createExcludePatternsArray() {

        LinkedList<String> excludePatterns = new LinkedList<String>();
        try {
            String defaultExcludedFolders = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FOLDERS);
            for (String folder : StringUtils.split(defaultExcludedFolders, ",")) {
                String trimmedPattern = folder.trim();
                if (trimmedPattern != "") {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                }
            }

            String defaultExcludedFiles = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FILES);
            for (String file : StringUtils.split(defaultExcludedFiles, ",")) {
                String trimmedPattern = file.trim();
                if (trimmedPattern != "") {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }

            if (params.getCliSastParameters().isHasExcludedFoldersParam()) {
                for (String folder : params.getCliSastParameters().getExcludedFolders()) {
                    String trimmedPattern = folder.trim();
                    if (trimmedPattern != "") {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                    }
                }
            }

            if (params.getCliSastParameters().isHasExcludedFilesParam()) {
                for (String file : params.getCliSastParameters().getExcludedFiles()) {
                    String trimmedPattern = file.trim();
                    if (trimmedPattern != "") {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred creation of exclude patterns");
        }
        return excludePatterns.toArray(new String[]{});

    }
}
