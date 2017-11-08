package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.commands.job.utils.PrintResultsUtil;
import com.checkmarx.cxconsole.commands.job.utils.StoreReportUtils;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.login.soap.CxSoapSASTClient;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.dto.PresetDTO;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.exceptions.CxSoapLoginClientException;
import com.checkmarx.login.soap.exceptions.CxSoapSASTClientException;
import com.checkmarx.login.soap.providers.ScanPrerequisitesValidator;
import com.checkmarx.login.soap.providers.exceptions.CLISoapProvidersException;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import com.checkmarx.parameters.CLIScanParameters;
import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.checkmarx.cxconsole.commands.constants.LocationType.FOLDER;
import static com.checkmarx.cxconsole.commands.constants.LocationType.getCorrespondingType;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.thresholds.ThresholdResolver.resolveThresholdExitCode;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLISASTScanJob extends CLIScanJob {

    private static final int LOW_VULNERABILITY_RESULTS = 0;
    private static final int MEDIUM_VULNERABILITY_RESULTS = 1;
    private static final int HIGH_VULNERABILITY_RESULTS = 2;

    private CxWSResponseProjectConfig projectConfig;
    private PresetDTO selectedPreset;
    private ConfigurationDTO selectedConfiguration;
    private CxSoapSASTClient cxSoapSASTClient;
    private long projectId;
    private byte[] zippedSourcesBytes;
    private String runId;
    private SourceLocationType sourceLocationType;
    private RepositoryType repoType;


    public CLISASTScanJob(CLIScanParameters params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        log.info("Project name is \"" + params.getCliMandatoryParameters().getProjectName() + "\"");

        if (params.getCliMandatoryParameters().isHasTokenParam()) {
            super.restLogin();
            URL wsdlLocation = null;
            try {
                wsdlLocation = new URL(SoapClientUtils.buildHostWithWSDL(params.getCliMandatoryParameters().getOriginalHost()));
                this.cxSoapLoginClient.initSoapClient(wsdlLocation);
            } catch (MalformedURLException | CxSoapLoginClientException e) {
                log.error("Error initiate SOAP client: " + e.getMessage());
                throw new CLIJobException("Error initiate SOAP client: " + e.getMessage());
            }
        } else {
            super.soapLogin();
        }
        cxSoapSASTClient = new CxSoapSASTClient(this.cxSoapLoginClient.getCxSoapClient());

        locateProjectOnServer(params.getCliMandatoryParameters().getProjectNameWithPath(), sessionId);
        ScanPrerequisitesValidator scanPrerequisitesValidator;

        try {
            scanPrerequisitesValidator = new ScanPrerequisitesValidator(cxSoapLoginClient.getCxSoapClient(), sessionId);
            selectedPreset = scanPrerequisitesValidator.validateJobPreset(params.getCliSastParameters().getPresetName());
            selectedConfiguration = scanPrerequisitesValidator.validateJobConfiguration(params.getCliSastParameters().getConfiguration());
            log.info("Scan prerequisites validated successfully");
        } catch (CLISoapProvidersException e) {
            throw new CLIJobException("Error initialize project prerequisites: " + e.getMessage());
        }

        if (projectConfig != null && !projectConfig.getProjectConfig().getSourceCodeSettings().getSourceOrigin().equals(SourceLocationType.LOCAL)) {
            params.getCliSharedParameters().setLocationType(getLocationType(projectConfig.getProjectConfig().getSourceCodeSettings()));
            if (params.getCliSharedParameters().getLocationType() == LocationType.PERFORCE) {
                boolean isWorkspace = (this.projectConfig.getProjectConfig().getSourceCodeSettings().getSourceControlSetting().getPerforceBrowsingMode() == CxWSPerforceBrowsingMode.WORKSPACE);
                params.getCliSastParameters().setPerforceWorkspaceMode(isWorkspace);
            }
        }

        if (params.getCliSharedParameters().getLocationType() == FOLDER) {
            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);
            if (!packFolder(maxZipSize)) {
                throw new CLIJobException("Error during packing sources.");
            }

            // check packed sources size
            if (zippedSourcesBytes == null || zippedSourcesBytes.length == 0) {
                // if size is greater that restricted value, stop scan
                log.error("Packing sources has failed: empty packed source ");
                throw new CLIJobException("Packing sources has failed: empty packed source ");
            }

            if (zippedSourcesBytes.length > maxZipSize) {
                // if size greater that restricted value, stop scan
                log.error("Packed project size is greater than " + maxZipSize);
                throw new CLIJobException("Packed project size is greater than " + maxZipSize);
            }
        }

        // request scan
        log.info("Request SAST scan");
        requestScan(sessionId);
        log.info("SAST scan created successfully");

        // wait for scan completion
        if (isAsyncScan) {
            log.info("Asynchronous scan initiated, Waiting for SAST scan to queue.");
        } else {
            log.info("Full scan initiated, Waiting for SAST scan to finish.");
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        WaitScanCompletionJob waiterJob = new WaitScanCompletionJob(cxSoapSASTClient, sessionId, runId, isAsyncScan);
        long scanId;
        try {
            Future<Boolean> furore = executor.submit(waiterJob);
            // wait for scan completion
            furore.get();

            scanId = waiterJob.getScanId();
            if (isAsyncScan) {
                log.info("SAST scan queued. Job finished");
            } else {
                log.info("SAST scan finished. Retrieving scan results");
            }

        } catch (Exception e) {
            log.trace("Error occurred during scan progress monitoring: " + e.getMessage());
            throw new CLIJobException("Error occurred during scan progress monitoring: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        //update scan comment
        String comment = params.getCliSharedParameters().getScanComment();
        if (comment != null) {
            CxWSBasicRepsonse result = null;
            try {
                result = cxSoapSASTClient.updateScanComment(sessionId, scanId, comment);
            } catch (CxSoapClientValidatorException e) {
                if (result != null && result.getErrorMessage() != null) {
                    log.warn("Cannot update the scan comment: " + result.getErrorMessage());
                }
            }
        }

        if (!isAsyncScan) {
            String resultsFileName = params.getCliSastParameters().getXmlFile();
            if (resultsFileName == null) {
                resultsFileName = PathHandler.normalizePathString(params.getCliMandatoryParameters().getProjectName()) + ".xml";
            }
            String scanSummary = null;
            try {
                scanSummary = cxSoapSASTClient.getScanSummary(params.getCliMandatoryParameters().getOriginalHost(), sessionId, scanId);
                storeXMLResults(resultsFileName, cxSoapSASTClient.getScanReport(sessionId, scanId, "XML"));
            } catch (CxSoapSASTClientException e) {
                log.error("Error retrieving scan summary report: " + e.getMessage());
                throw new CLIJobException("Error retrieving scan summary report: " + e.getMessage());
            }

            //SAST print results
            int[] scanResults = JobUtils.parseScanSummary(scanSummary);
            PrintResultsUtil.printSASTResultsToConsole(scanResults);

            //SAST reports
            if (!params.getCliSastParameters().getReportType().isEmpty()) {
                for (int i = 0; i < params.getCliSastParameters().getReportType().size(); i++) {
                    log.info("Report type: " + params.getCliSastParameters().getReportType().get(i));
                    String resultsPath = params.getCliSastParameters().getReportFile().get(i);
                    if (resultsPath == null) {
                        resultsPath = PathHandler.normalizePathString(params.getCliMandatoryParameters().getProjectName()) + "." + params.getCliSastParameters().getReportType().get(i).toLowerCase();
                    }
                    StoreReportUtils.downloadAndStoreReport(params.getCliMandatoryParameters().getProjectName(), resultsPath, params.getCliSastParameters().getReportType().get(i),
                            scanId, cxSoapSASTClient, sessionId, params.getCliMandatoryParameters().getSrcPath());
                }
            }

            //SAST threshold calculation
            if (params.getCliSastParameters().isSastThresholdEnabled()) {
                ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.SAST_SCAN, params.getCliSastParameters().getSastHighThresholdValue(), params.getCliSastParameters().getSastMediumThresholdValue(),
                        params.getCliSastParameters().getSastLowThresholdValue(), scanResults[HIGH_VULNERABILITY_RESULTS], scanResults[MEDIUM_VULNERABILITY_RESULTS], scanResults[LOW_VULNERABILITY_RESULTS]);
                return resolveThresholdExitCode(thresholdDto);
            }
        }

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

    private void requestScan(String sessionId) throws CLIJobException {
        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        CxWSResponseRunID runScanResult = null;
        int count = 0;
        String errMsg = "";

        sourceLocationTypeResolver();

        // Start scan
        long getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);
        while ((runScanResult == null || !runScanResult.isIsSuccesfull()) && count < retriesNum) {
            try {
                runScanResult = cxSoapSASTClient.cliScan(sessionId, selectedPreset.getId(), selectedConfiguration.getId(), sourceLocationType, zippedSourcesBytes, repoType, params);
            } catch (CxSoapSASTClientException e) {
                errMsg = e.getMessage();
                count++;
                log.trace("Error during quering existing project scan run.", e);
                log.info("Error occurred during existing project scan request: " + errMsg + ". Operation retry " + count);
            }

            if ((runScanResult != null) && !runScanResult.isIsSuccesfull()) {
                errMsg = runScanResult.getErrorMessage();
                log.error("Existing project scan request was unsuccessful.");
                count++;
                log.info("Existing project scan run request unsuccessful: " + runScanResult.getErrorMessage() + ". Operation retry " + count);
            }

            if ((runScanResult == null || !runScanResult.isIsSuccesfull()) && count < retriesNum) {
                try {
                    Thread.sleep(getStatusInterval * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if ((runScanResult != null) && !runScanResult.isIsSuccesfull()) {
            throw new CLIJobException("Existing project scan request was unsuccessful. " + (errMsg == null ? "" : errMsg));
        } else if (runScanResult == null) {
            throw new CLIJobException("Error occurred during existing project scan. " + errMsg);
        }

        log.trace("Existing project scan request response: runId:" + runScanResult.getRunId());
        runId = runScanResult.getRunId();
    }

    private void sourceLocationTypeResolver() throws CLIJobException {
        if (params.getCliSharedParameters().getLocationType() != null) {
            sourceLocationType = getCorrespondingType(params.getCliSharedParameters().getLocationType());
            if (params.getCliSharedParameters().getLocationType() != LocationType.FOLDER && params.getCliSharedParameters().getLocationType() != LocationType.SHARED) {
                repoType = RepositoryType.fromValue(params.getCliSharedParameters().getLocationType().getLocationTypeStringValue());
            }
        } else {
            sourceLocationType = projectConfig.getProjectConfig().getSourceCodeSettings().getSourceOrigin();
            if (sourceLocationType == SourceLocationType.LOCAL) {
                log.error("Scan command failed since no source location was provided.");
                throw new CLIJobException("Scan command failed since no source location was provided.");
            }
        }
    }

    private void locateProjectOnServer(String projectName, String sessionId) throws CLIJobException {
        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        CxWSResponseProjectsDisplayData getPrjsResult = null;
        int count = 0;
        String errMsg = "";
        if (params.getCliSharedParameters().getLocationType() == null) {
            long getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(
                    ConfigMgr.KEY_PROGRESS_INTERVAL);

            while ((getPrjsResult == null || !getPrjsResult.isIsSuccesfull())
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
                } catch (CxSoapClientValidatorException e) {
                    errMsg = e.getMessage();
                    count++;
                    log.trace("Error during fetching existing projects dto.", e);
                    log.info("Error occurred during fetching existing projects dto: " + errMsg + ". Operation retry " + count);
                }

                if ((getPrjsResult != null) && !getPrjsResult.isIsSuccesfull()) {
                    errMsg = getPrjsResult.getErrorMessage();
                    log.error("Existing projects dto fetching was unsuccessful.");
                    count++;
                    log.info("Existing projects dto fetching unsuccessful: " + getPrjsResult.getErrorMessage() + ". Operation retry " + count);
                }

                if ((getPrjsResult == null || !getPrjsResult.isIsSuccesfull())
                        && count < retriesNum) {
                    try {
                        Thread.sleep(getStatusInterval * 1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if ((getPrjsResult != null) && !getPrjsResult.isIsSuccesfull()) {
                throw new CLIJobException("Existing projects dto fetching was unsuccessful. " + (errMsg == null ? "" : errMsg));
            } else if (getPrjsResult == null) {
                throw new CLIJobException("Error occurred during existing projects dto fetching. " + errMsg);
            } else {
                List<ProjectDisplayData> prjData = getPrjsResult.getProjectList().getProjectDisplayData();
                for (ProjectDisplayData projectData : prjData) {
                    String fullProjectName = "";
                    String[] locationParts = projectData.getGroup().split("\\-\\>");
                    if (locationParts.length > 0) {
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
            }

            try {
                projectConfig = cxSoapSASTClient.getProjectConfiguration(sessionId, projectId);
            } catch (CxSoapClientValidatorException e) {
                log.error("Error retrieving project information from server (project ID: " + projectId + " ): " + e.getMessage());
                throw new CLIJobException("Error retrieving project information from server (project ID: " + projectId + " ): " + e.getMessage());
            }
            log.trace("Existing projects dto response:" + getPrjsResult);
        }
    }

    private LocationType getLocationType(SourceCodeSettings scSettings) {
        SourceLocationType slType = scSettings.getSourceOrigin();
        if (slType.equals(SourceLocationType.LOCAL)) {
            return FOLDER;
        } else if (slType.equals(SourceLocationType.SHARED)) {
            return LocationType.SHARED;
        } else if (slType.equals(SourceLocationType.SOURCE_CONTROL)) {
            RepositoryType rType = scSettings.getSourceControlSetting().getRepository();
            if (rType.equals(RepositoryType.TFS)) {
                return LocationType.TFS;
            } else if (rType.equals(RepositoryType.GIT)) {
                return LocationType.GIT;
            } else if (rType.equals(RepositoryType.SVN)) {
                return LocationType.SVN;
            } else if (rType.equals(RepositoryType.PERFORCE)) {
                return LocationType.PERFORCE;
            }
        }

        return null;
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
                if (!Objects.equals(trimmedPattern, "")) {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                }
            }

            String defaultExcludedFiles = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FILES);
            for (String file : StringUtils.split(defaultExcludedFiles, ",")) {
                String trimmedPattern = file.trim();
                if (!Objects.equals(trimmedPattern, "")) {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }

            if (params.getCliSastParameters().isHasExcludedFoldersParam()) {
                for (String folder : params.getCliSastParameters().getExcludedFolders()) {
                    String trimmedPattern = folder.trim();
                    if (!Objects.equals(trimmedPattern, "")) {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                    }
                }
            }

            if (params.getCliSastParameters().isHasExcludedFilesParam()) {
                for (String file : params.getCliSastParameters().getExcludedFiles()) {
                    String trimmedPattern = file.trim();
                    if (!Objects.equals(trimmedPattern, "")) {
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