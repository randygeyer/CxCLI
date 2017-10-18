package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.login.rest.CxRestClient;
import com.checkmarx.cxosa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxosa.dto.CreateOSAScanResponse;
import com.checkmarx.cxosa.dto.OSAScanStatus;
import com.checkmarx.cxosa.dto.OSASummaryResults;
import com.checkmarx.cxosa.utils.OsaUtils;
import com.checkmarx.cxviewer.ws.WSMgr;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.checkmarx.cxosa.dto.OSAScanStatusEnum.QUEUED;
import static com.checkmarx.exitcodes.Constants.ExitCodes.GENERAL_ERROR_EXIT_CODE;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.thresholds.ThresholdResolver.resolveThresholdExitCode;

public class CxCLIOsaScanJob extends CxScanJob {

    private String workDirectory = "";
    private String osaProjectSummaryLink;
    private boolean scanOsaOnly = false;
    private long projectId = -1;
    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private boolean isAsyncOsaScan = false;

    public CxCLIOsaScanJob(ScanParams params, WSMgr wsMgr, String sessionId, long projectId, boolean isAsnycOsaScan) {
        super(params);
        this.wsMgr = wsMgr;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.scanOsaOnly = false;
        this.isAsyncOsaScan = isAsnycOsaScan;
    }

    public CxCLIOsaScanJob(ScanParams params, boolean isAsyncOsaScan) {
        super(params);
        this.scanOsaOnly = true;
        this.isAsyncOsaScan = isAsyncOsaScan;
    }

    @Override
    public Integer call() throws Exception {
        OSASummaryResults osaSummaryResults;
        int exitCode = GENERAL_ERROR_EXIT_CODE;
        try {
            log.info("Project name is \"" + params.getProjName() + "\"");
            // Connect to Checkmarx service.
            String generatedHost = null;
            wsMgr = ConfigMgr.getWSMgr();
            try {
                generatedHost = wsMgr.resolveServiceLocation(params.getHost());
            } catch (Exception e) {
                throw e;
            }
            if (!params.getOriginHost().contains("http")) {
                if (generatedHost.contains("https://")) {
                    params.setOriginHost("https://" + params.getOriginHost());
                } else {
                    params.setOriginHost("http://" + params.getOriginHost());
                }
            }
            params.setHost(generatedHost);
            if (scanOsaOnly) {
                URL wsdlLocation = wsMgr.makeWsdlLocation(params.getHost());
                // Logging into the Checkmarx service.
                login(wsdlLocation);
            }

            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_OSA_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);

            //Request osa Scan
            log.info("");
            log.info("Request OSA scan");
            if (params.hasUserParam() && params.hasPasswordParam()) {
                restClient = new CxRestClient(params.getOriginHost(), params.getUser(), params.getPassword(), log);
                // Logging into the OSA service.
                restClient.login();
            }
            else {
                restClient = new CxRestClient(params.getOriginHost(), params.getToken(), log);
            }

            if (projectId == -1) {
                projectId = locateProjectOnServer();
            }

            OsaUtils.setLogger(log);
            String[] osaLocationPath = params.getOsaLocationPath() != null ? params.getOsaLocationPath() : new String[]{params.getLocationPath()};
            log.info("OSA source location: " + StringUtils.join(osaLocationPath, ", "));
            log.info("Zipping dependencies");
            File zipForOSA = OsaUtils.zipWorkspaceFolder(params.getOsaExcludedFiles(), params.getOsaExcludedFolders(), params.getOsaIncludedFiles(), maxZipSize, osaLocationPath, log);
            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan = restClient.createOSAScan(projectId, zipForOSA);
            osaProjectSummaryLink = OsaUtils.composeProjectOSASummaryLink(params.getOriginHost(), projectId);
            log.info("OSA scan created successfully");

            if (zipForOSA.exists() && !zipForOSA.delete()) {
                log.warn("Warning: failed to delete temporary zip file: " + zipForOSA.getAbsolutePath());
            }

            if (isAsyncOsaScan) {
                log.info("Waiting for OSA scan to queue");
            } else {
                log.info("Waiting for OSA scan to finish");
            }
            //wait for OSA scan to finish
            OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
            osaConsoleScanWaitHandler.setLogger(log);
            OSAScanStatus returnStatus = restClient.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler, isAsyncOsaScan);
            if (isAsyncOsaScan && returnStatus.getStatus() == QUEUED) {
                exitCode = SCAN_SUCCEEDED_EXIT_CODE;
            }
            if (!isAsyncOsaScan) {
                log.info("OSA scan finished successfully");
                exitCode = SCAN_SUCCEEDED_EXIT_CODE;

                //OSA scan results
                osaSummaryResults = restClient.getOSAScanSummaryResults(osaScan.getScanId());
                printOSAResultsToConsole(osaSummaryResults, osaProjectSummaryLink);

                //Osa threshold calculation
                if (params.isOsaThresholdEnabled()) {
                    ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.OSA_SCAN, params.getOsaHighThresholdValue(), params.getOsaMediumThresholdValue(),
                            params.getOsaLowThresholdValue(), osaSummaryResults.getTotalHighVulnerabilities(),
                            osaSummaryResults.getTotalMediumVulnerabilities(), osaSummaryResults.getTotalLowVulnerabilities());
                    exitCode = resolveThresholdExitCode(thresholdDto, log);
                }

                //OSA reports
                String htmlFile = params.getOsaReportHTML();
                String pdfFile = params.getOsaReportPDF();
                String jsonFile = params.getOsaJson();
                try {
                    if (htmlFile != null || pdfFile != null || jsonFile != null) {
                        log.info("Creating CxOSA Reports");
                        log.info("-----------------------");
                        workDirectory = gerWorkDirectory();

                        //OSA HTML report
                        if (htmlFile != null) {
                            String resultFilePath = resolveReportPath("HTML", htmlFile, OSA_REPORT_NAME + ".html");
                            restClient.createOsaHtmlReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA PDF report
                        if (pdfFile != null) {
                            String resultFilePath = resolveReportPath("PDF", pdfFile, OSA_REPORT_NAME + ".pdf");
                            restClient.createOsaPdfReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA json reports
                        if (jsonFile != null) {
                            String resultFilePath = resolveReportPath("JSON", jsonFile, "");
                            restClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error occurred during CxOSA reports. Error message: " + e.getMessage());
                    exitCode = errorCodeResolver(e.getMessage());
                }
            } else {
                log.info("OSA scan queued successfully. Job finished");
            }
        } finally {
            if (super.getErrorMsg() != null) {
                exitCode = errorCodeResolver(super.getErrorMsg());
            }
            OsaUtils.deleteTempFiles();
            if (restClient != null) {
                restClient.close();
            }
        }

        return exitCode;
    }

    private String resolveReportPath(String ext, String file, String reportName) {
        String toLog = "";
        if (!isFilenameValid(file)) {
            if (!StringUtils.isEmpty(file)) {
                toLog = "The path you specified is invalid. ";
            }
            file = reportName;
            log.warn(toLog + "Using default location for " + ext + " report.");
        }
        return initFilePath(file, "." + ext.toLowerCase(), workDirectory);
    }

    private boolean isFilenameValid(String filePath) {
        Boolean ret = true;
        try {
            File file = new File(filePath);
            if (file.isDirectory() || StringUtils.isEmpty(filePath)) {
                ret = false;
            }
            file.getCanonicalPath();
        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }

    private long locateProjectOnServer() throws Exception {
        GetProjectDataResult projectData = wsMgr.getProjectsDisplayData(sessionId);
        for (ProjectDisplayData data : projectData.getProjectData()) {
            String projectFullName = data.getGroup() + "\\" + data.getProjectName();
            if (projectFullName.equalsIgnoreCase(params.getFullProjName())) {
                return data.getProjectID();
            }
        }
        throw new Exception("The project: " + params.getFullProjName() + " was not found on the server. OSA scan requires an existing project on the server");

    }

    @Override
    protected String getProjectName() {
        return params.getFullProjName();
    }

    private void printOSAResultsToConsole(OSASummaryResults osaSummaryResults, String osaProjectSummaryLink) {
        log.info("----------------------------Checkmarx Scan Results(CxOSA):-------------------------------");
        log.info("");
        log.info("------------------------");
        log.info("OSA vulnerabilities Summary:");
        log.info("------------------------");
        log.info("OSA high severity results: " + osaSummaryResults.getTotalHighVulnerabilities());
        log.info("OSA medium severity results: " + osaSummaryResults.getTotalMediumVulnerabilities());
        log.info("OSA low severity results: " + osaSummaryResults.getTotalLowVulnerabilities());
        log.info("Vulnerability score: " + osaSummaryResults.getVulnerabilityScore());
        log.info("");
        log.info("-----------------------");
        log.info("Libraries Scan Results:");
        log.info("-----------------------");
        log.info("Open-source libraries: " + osaSummaryResults.getTotalLibraries());
        log.info("Vulnerable and outdated: " + osaSummaryResults.getVulnerableAndOutdated());
        log.info("Vulnerable and updated: " + osaSummaryResults.getVulnerableAndUpdated());
        log.info("Non-vulnerable libraries: " + osaSummaryResults.getNonVulnerableLibraries());
        log.info("");
        log.info("");
        log.info("OSA scan results location: " + osaProjectSummaryLink);
        log.info("-----------------------------------------------------------------------------------------");
    }

}
