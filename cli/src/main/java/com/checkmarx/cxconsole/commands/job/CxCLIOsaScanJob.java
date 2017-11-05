package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxosa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxosa.dto.CreateOSAScanResponse;
import com.checkmarx.cxosa.dto.OSAScanStatus;
import com.checkmarx.cxosa.dto.OSASummaryResults;
import com.checkmarx.cxosa.utils.OsaUtils;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import com.checkmarx.login.rest.CxRestLoginClient;
import com.checkmarx.login.rest.CxRestOSAClient;
import com.checkmarx.login.rest.dto.RestLoginResponseDTO;
import com.checkmarx.login.soap.CxSoapLoginClient;
import com.checkmarx.login.soap.CxSoapSASTClient;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import com.checkmarx.parameters.CLIScanParameters;
import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.checkmarx.cxosa.dto.OSAScanStatusEnum.QUEUED;
import static com.checkmarx.exitcodes.Constants.ExitCodes.GENERAL_ERROR_EXIT_CODE;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.login.soap.utils.SoapClientUtils.resolveServerProtocol;
import static com.checkmarx.thresholds.ThresholdResolver.resolveThresholdExitCode;

public class CxCLIOsaScanJob extends CxScanJob {

    private String workDirectory = "";
    private CxRestOSAClient cxRestOSAClient;
    private String osaProjectSummaryLink;
    private boolean scanOsaOnly = false;
    private long projectId = -1;
    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private boolean isAsyncOsaScan = false;

    public CxCLIOsaScanJob(CLIScanParameters params, CxSoapLoginClient cxSoapLoginClient, String sessionId, long projectId, boolean isAsnycOsaScan) {
        super(params);
        this.cxSoapLoginClient = cxSoapLoginClient;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.scanOsaOnly = false;
        this.isAsyncOsaScan = isAsnycOsaScan;
    }

    public CxCLIOsaScanJob(CLIScanParameters params, boolean isAsyncOsaScan) {
        super(params);
        this.scanOsaOnly = true;
        this.isAsyncOsaScan = isAsyncOsaScan;
    }

    @Override
    public Integer call() throws Exception {
        OSASummaryResults osaSummaryResults;
        int exitCode = GENERAL_ERROR_EXIT_CODE;
        try {
            log.info("Project name is \"" + params.getCliMandatoryParameters().getProjectName() + "\"");
            // Connect to Checkmarx service.
            String generatedHost = null;
            cxSoapLoginClient = ConfigMgr.getWSMgr();
//            String hostWithProtocol = resolveServerProtocol(params.getCliMandatoryParameters().getOriginalHost());
//            if (hostWithProtocol != null) {
//                params.getCliMandatoryParameters().setOriginalHost(hostWithProtocol);
//            } else {
//                throw new Exception("Failed to validate server connectivity");
//            }
            if (scanOsaOnly) {
                URL wsdlLocation = new URL(SoapClientUtils.buildHostWithWSDL(params.getCliMandatoryParameters().getOriginalHost()));
                // Logging into the Checkmarx service.
                login(wsdlLocation);
            }

            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_OSA_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);

            //Request osa Scan
            log.info("");
            log.info("Request OSA scan");
            RestLoginResponseDTO restLoginResponseDTO;
            if (params.getCliMandatoryParameters().isHasUserParam() && params.getCliMandatoryParameters().isHasPasswordParam()) {
                cxRestLoginClient = new CxRestLoginClient(params.getCliMandatoryParameters().getOriginalHost(), params.getCliMandatoryParameters().getUsername(), params.getCliMandatoryParameters().getPassword());
                // Logging into the OSA service.
                restLoginResponseDTO = cxRestLoginClient.credentialsLogin();
            } else {
                cxRestLoginClient = new CxRestLoginClient(params.getCliMandatoryParameters().getOriginalHost(), params.getCliMandatoryParameters().getToken());
                restLoginResponseDTO = cxRestLoginClient.tokenLogin();
            }
            cxRestOSAClient = new CxRestOSAClient(params.getCliMandatoryParameters().getOriginalHost(), restLoginResponseDTO, log);

            if (projectId == -1) {
                projectId = locateProjectOnServer();
            }

            OsaUtils.setLogger(log);
            String[] osaLocationPath = params.getCliOsaParameters().getOsaLocationPath() != null ? params.getCliOsaParameters().getOsaLocationPath() : new String[]{params.getCliSharedParameters().getLocationPath()};
            log.info("OSA source location: " + StringUtils.join(osaLocationPath, ", "));
            log.info("Zipping dependencies");
            File zipForOSA = OsaUtils.zipWorkspaceFolder(params.getCliOsaParameters().getOsaExcludedFiles(), params.getCliOsaParameters().getOsaExcludedFolders(), params.getCliOsaParameters().getOsaIncludedFiles(), maxZipSize, osaLocationPath, log);
            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan = cxRestOSAClient.createOSAScan(projectId, zipForOSA);
            osaProjectSummaryLink = OsaUtils.composeProjectOSASummaryLink(params.getCliMandatoryParameters().getOriginalHost(), projectId);
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
            OSAScanStatus returnStatus = cxRestOSAClient.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler, isAsyncOsaScan);
            if (isAsyncOsaScan && returnStatus.getStatus() == QUEUED) {
                exitCode = SCAN_SUCCEEDED_EXIT_CODE;
            }
            if (!isAsyncOsaScan) {
                log.info("OSA scan finished successfully");
                exitCode = SCAN_SUCCEEDED_EXIT_CODE;

                //OSA scan results
                osaSummaryResults = cxRestOSAClient.getOSAScanSummaryResults(osaScan.getScanId());
                printOSAResultsToConsole(osaSummaryResults, osaProjectSummaryLink);

                //Osa threshold calculation
                if (params.getCliOsaParameters().isOsaThresholdEnabled()) {
                    ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.OSA_SCAN, params.getCliOsaParameters().getOsaHighThresholdValue(), params.getCliOsaParameters().getOsaMediumThresholdValue(),
                            params.getCliOsaParameters().getOsaLowThresholdValue(), osaSummaryResults.getTotalHighVulnerabilities(),
                            osaSummaryResults.getTotalMediumVulnerabilities(), osaSummaryResults.getTotalLowVulnerabilities());
                    exitCode = resolveThresholdExitCode(thresholdDto, log);
                }

                //OSA reports
                String htmlFile = params.getCliOsaParameters().getOsaReportHTML();
                String pdfFile = params.getCliOsaParameters().getOsaReportPDF();
                String jsonFile = params.getCliOsaParameters().getOsaJson();
                try {
                    if (htmlFile != null || pdfFile != null || jsonFile != null) {
                        log.info("Creating CxOSA Reports");
                        log.info("-----------------------");
                        workDirectory = gerWorkDirectory();

                        //OSA HTML report
                        if (htmlFile != null) {
                            String resultFilePath = resolveReportPath("HTML", htmlFile, OSA_REPORT_NAME + ".html");
                            cxRestOSAClient.createOsaHtmlReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA PDF report
                        if (pdfFile != null) {
                            String resultFilePath = resolveReportPath("PDF", pdfFile, OSA_REPORT_NAME + ".pdf");
                            cxRestOSAClient.createOsaPdfReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA json reports
                        if (jsonFile != null) {
                            String resultFilePath = resolveReportPath("JSON", jsonFile, "");
                            cxRestOSAClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
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
            if (cxRestOSAClient != null) {
                cxRestOSAClient.close();
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
        CxSoapSASTClient cxSoapSASTClient = new CxSoapSASTClient(cxSoapLoginClient.getCxSoapClient());
        GetProjectDataResult projectData = cxSoapSASTClient.getProjectsDisplayData(sessionId);
        for (ProjectDisplayData data : projectData.getProjectData()) {
            String projectFullName = data.getGroup() + "\\" + data.getProjectName();
            if (projectFullName.equalsIgnoreCase(params.getCliMandatoryParameters().getProjectNameWithPath())) {
                return data.getProjectID();
            }
        }
        throw new Exception("The project: " + params.getCliMandatoryParameters().getProjectNameWithPath() + " was not found on the server. OSA scan requires an existing project on the server");

    }

    @Override
    protected String getProjectName() {
        return params.getCliMandatoryParameters().getProjectNameWithPath();
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
