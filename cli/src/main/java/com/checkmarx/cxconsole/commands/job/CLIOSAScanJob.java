package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.clients.rest.osa.CxRestOSAClient;
import com.checkmarx.clients.rest.osa.constant.FileNameAndShaOneForOsaScan;
import com.checkmarx.clients.rest.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.clients.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.clients.soap.sast.CxSoapSASTClient;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.cxosa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxconsole.cxosa.dto.CreateOSAScanResponse;
import com.checkmarx.cxconsole.cxosa.dto.OSAScanStatus;
import com.checkmarx.cxconsole.cxosa.dto.OSASummaryResults;
import com.checkmarx.cxconsole.cxosa.utils.Exception.OSAUtilException;
import com.checkmarx.cxconsole.cxosa.utils.OSAUtil;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseProjectsDisplayData;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.parameters.CLIOSAParameters;
import com.checkmarx.parameters.CLIScanParametersSingleton;
import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.checkmarx.cxconsole.commands.job.utils.PrintResultsUtils.printOSAResultsToConsole;
import static com.checkmarx.cxconsole.cxosa.dto.OSAScanStatusEnum.QUEUED;
import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.thresholds.ThresholdResolver.resolveThresholdExitCode;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIOSAScanJob extends CLIScanJob {

    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private CxRestOSAClient cxRestOSAClient;
    private CxSoapSASTClient cxSoapSASTClient;

    public CLIOSAScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        OSASummaryResults osaSummaryResults;
        CLIOSAParameters cliosaParameters = params.getCliOsaParameters();
        try {
            log.info("Project name is \"" + params.getCliMandatoryParameters().getProjectName() + "\"");

            // Connect to Checkmarx service, if not already connected.
            super.restLogin();
            cxRestOSAClient = new CxRestOSAClient(params.getCliMandatoryParameters().getOriginalHost(), this.cxRestLoginClient.getRestLoginResponseDTO());
            if (this.cxSoapLoginClient.getSessionId() == null && this.cxRestLoginClient.getRestLoginResponseDTO().getSessionId() == null) {
                super.soapLogin();
                sessionId = cxSoapLoginClient.getSessionId();
            }
            cxSoapSASTClient = new CxSoapSASTClient(this.cxSoapLoginClient.getCxSoapClient());

            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_OSA_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);

            //Request osa Scan
            log.info("");
            log.info("Request OSA scan");

            long projectId = locateProjectOnServer();

            String[] osaLocationPath = cliosaParameters.getOsaLocationPath() != null ? cliosaParameters.getOsaLocationPath() : new String[]{params.getCliSharedParameters().getLocationPath()};
            log.info("OSA source location: " + StringUtils.join(osaLocationPath, ", "));

            log.info("Setting up OSA analysis request");
            List<FileNameAndShaOneForOsaScan> osaFilesToScan;
            try {
                osaFilesToScan = OSAUtil.scanFiles(osaLocationPath, cliosaParameters.getOsaIncludedFiles(), cliosaParameters.getOsaExcludedFiles(),
                        cliosaParameters.getOsaExtractableIncludeFiles(), Integer.parseInt(cliosaParameters.getOsaScanDepth()));
            } catch (OSAUtilException e) {
                log.trace(e.getMessage());
                throw new CLIJobException("Error create OSA scan: " + e.getMessage());
            }

            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan;
            try {
                osaScan = cxRestOSAClient.createOSAScan(projectId, osaFilesToScan);
            } catch (CxRestOSAClientException e) {
                log.error("Error create OSA scan: " + e.getMessage());
                throw new CLIJobException("Error create OSA scan: " + e.getMessage());
            }
            String osaProjectSummaryLink = OSAUtil.composeProjectOSASummaryLink(params.getCliMandatoryParameters().getOriginalHost(), projectId);
            log.info("OSA scan created successfully");

            if (isAsyncScan) {
                log.info("Asynchronous scan, Waiting for OSA scan to queue");
            } else {
                log.info("Full scan initiated, Waiting for OSA scan to finish");
            }

            //wait for OSA scan to finish
            OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
            OSAScanStatus returnStatus;
            try {
                returnStatus = cxRestOSAClient.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler, isAsyncScan);
            } catch (CxRestOSAClientException e) {
                log.error("Error retrieving OSA scan status: " + e.getMessage());
                throw new CLIJobException("Error retrieving OSA scan status: " + e.getMessage());
            }

            if (isAsyncScan && returnStatus.getStatus() == QUEUED) {
                return SCAN_SUCCEEDED_EXIT_CODE;
            }
            if (!isAsyncScan) {
                log.info("OSA scan finished successfully");
                //OSA scan results
                try {
                    osaSummaryResults = cxRestOSAClient.getOSAScanSummaryResults(osaScan.getScanId());
                } catch (CxRestOSAClientException e) {
                    log.error("Error retrieving OSA scan summary results: " + e.getMessage());
                    throw new CLIJobException("Error retrieving OSA scan summary results: " + e.getMessage());
                }
                printOSAResultsToConsole(osaSummaryResults, osaProjectSummaryLink);

                //OSA reports
                String htmlFile = cliosaParameters.getOsaReportHTML();
                String pdfFile = cliosaParameters.getOsaReportPDF();
                String jsonFile = cliosaParameters.getOsaJson();
                try {
                    if (htmlFile != null || pdfFile != null || jsonFile != null) {
                        log.info("Creating CxOSA Reports");
                        log.info("-----------------------");
                        String workDirectory = JobUtils.gerWorkDirectory(params);

                        //OSA HTML report
                        if (htmlFile != null) {
                            String resultFilePath = PathHandler.resolveReportPath(params.getCliMandatoryParameters().getProjectName(), "HTML", htmlFile, OSA_REPORT_NAME + ".html", workDirectory);
                            cxRestOSAClient.createOsaHtmlReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA PDF report
                        if (pdfFile != null) {
                            String resultFilePath = PathHandler.resolveReportPath(params.getCliMandatoryParameters().getProjectName(), "PDF", pdfFile, OSA_REPORT_NAME + ".pdf", workDirectory);
                            cxRestOSAClient.createOsaPdfReport(osaScan.getScanId(), resultFilePath);
                        }
                        //OSA json reports
                        if (jsonFile != null) {
                            String resultFilePath = PathHandler.resolveReportPath(params.getCliMandatoryParameters().getProjectName(), "JSON", jsonFile, "", workDirectory);
                            cxRestOSAClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
                        }
                    }
                } catch (CxRestOSAClientException e) {
                    log.error("Error occurred during CxOSA reports. Error message: " + e.getMessage());
                    return errorCodeResolver(e.getMessage());
                }

                //Osa threshold calculation
                if (cliosaParameters.isOsaThresholdEnabled()) {
                    ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.OSA_SCAN, cliosaParameters.getOsaHighThresholdValue(), cliosaParameters.getOsaMediumThresholdValue(),
                            cliosaParameters.getOsaLowThresholdValue(), osaSummaryResults.getTotalHighVulnerabilities(),
                            osaSummaryResults.getTotalMediumVulnerabilities(), osaSummaryResults.getTotalLowVulnerabilities());
                    return resolveThresholdExitCode(thresholdDto);
                }
            } else {
                log.info("OSA scan queued successfully. Job finished");
            }
        } finally {
            if (cxRestOSAClient != null) {
                cxRestOSAClient.close();
            }
        }
        if (super.getErrorMsg() != null) {
            return errorCodeResolver(super.getErrorMsg());
        }

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

    private long locateProjectOnServer() throws CLIJobException {
        CxWSResponseProjectsDisplayData projectData;
        try {
            projectData = cxSoapSASTClient.getProjectsDisplayData(sessionId);
            for (ProjectDisplayData data : projectData.getProjectList().getProjectDisplayData()) {
                String projectFullName = data.getGroup() + "\\" + data.getProjectName();
                if (projectFullName.equalsIgnoreCase(params.getCliMandatoryParameters().getProjectNameWithPath())) {
                    return data.getProjectID();
                }
            }
        } catch (CxSoapClientValidatorException e) {
            throw new CLIJobException("The project: " + params.getCliMandatoryParameters().getProjectNameWithPath() + " was not found on the server. OSA scan requires an existing project on the server");
        }

        throw new CLIJobException("The project: " + params.getCliMandatoryParameters().getProjectNameWithPath() + " was not found on the server. OSA scan requires an existing project on the server");
    }
}