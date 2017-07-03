package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.commands.CxConsoleCommand;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.cxosa.CxRestClient;
import com.checkmarx.cxosa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxosa.dto.CreateOSAScanResponse;
import com.checkmarx.cxosa.dto.OSASummaryResults;
import com.checkmarx.cxosa.utils.OsaUtils;
import com.checkmarx.cxviewer.ws.WSMgr;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class CxCLIOsaScanJob extends CxScanJob {

    private String workDirectory = "";
    private String osaProjectSummaryLink;
    private boolean scanOsaOnly = false;
    private long projectId = -1;
    private static final String OSA_REPORT_NAME = "CxOSAReport";


    public CxCLIOsaScanJob(ScanParams params, WSMgr wsMgr, String sessionId, long projectId) {
        super(params);
        this.wsMgr = wsMgr;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.scanOsaOnly = false;
    }

    public CxCLIOsaScanJob(ScanParams params) {
        super(params);
        this.scanOsaOnly = true;
    }

    @Override
    public Integer call() throws Exception {
        try {
            if (scanOsaOnly) {
                log.info("Project name is \"" + params.getProjName() + "\"");
                // Connect to Checkmarx service.
                wsMgr = ConfigMgr.getWSMgr();
                URL wsdlLocation = wsMgr.makeWsdlLocation(params.getHost());
                // Logging into the Checkmarx service.
                login(wsdlLocation);
            }

            long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_OSA_MAX_ZIP_SIZE);
            maxZipSize *= (1024 * 1024);

            //Request osa Scan
            log.info("");
            log.info("Request OSA scan");
            restClient = new CxRestClient(params.getOriginHost(), params.getUser(), params.getPassword(), log);
            // Logging into the OSA service.
            restClient.login();

            if (projectId == -1) {
                projectId = locateProjectOnServer();
            }

            OsaUtils.setLogger(log);
            String osaLocationPath = params.getOsaLocationPath() != null ? params.getOsaLocationPath() : params.getLocationPath();
            log.info("OSA source location: " + osaLocationPath);
            log.info("Zipping dependencies");
            File zipForOSA = OsaUtils.zipWorkspaceFolder(StringUtils.join(params.getOsaExcludedFiles(), ','), StringUtils.join(params.getOsaExcludedFolders(), ','), maxZipSize, osaLocationPath, log);
            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan = restClient.createOSAScan(projectId, zipForOSA);
            osaProjectSummaryLink = OsaUtils.composeProjectOSASummaryLink(params.getOriginHost(), projectId);
            log.info("OSA scan created successfully");

            if (zipForOSA.exists() && !zipForOSA.delete()) {
                log.warn("Warning: failed to delete temporary zip file: " + zipForOSA.getAbsolutePath());
            }

            //wait for OSA scan to finish
            log.info("Waiting for OSA scan to finish");
            OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
            osaConsoleScanWaitHandler.setLogger(log);
            restClient.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler);
            log.info("OSA scan finished successfully");

            //OSA scan results
            OSASummaryResults osaSummaryResults = restClient.getOSAScanSummaryResults(osaScan.getScanId());
            printOSAResultsToConsole(osaSummaryResults, osaProjectSummaryLink);

            //OSA reports
            String htmlFile = params.getOsaReportHTML();
            String pdfFile = params.getOsaReportPDF();
            String jsonFile = params.getOsaJson();
            try {
                if (htmlFile != null || pdfFile != null || jsonFile != null) {
                    log.info("Creating CxOSA reports");
                    log.info("-----------------------");
                    workDirectory = gerWorkDirectory();

                    //OSA HTML report
                    if (htmlFile != null) {
                        if (!isFilenameValid(htmlFile)) {
                            htmlFile = OSA_REPORT_NAME + ".html";
                            log.warn("The path you specified for the HTML Report is invalid.");
                        }
                        String resultFilePath = initFilePath(htmlFile, ".html", workDirectory);
                        restClient.createOsaHtmlReport(osaScan.getScanId(), resultFilePath);
                    }
                    //OSA PDF report
                    if (pdfFile != null) {
                        if (!isFilenameValid(pdfFile)) {
                        pdfFile =  OSA_REPORT_NAME + ".pdf";
                            log.warn("The path you specified for the PDF Report is invalid.");
                        }
                        String resultFilePath = initFilePath(pdfFile, ".pdf", workDirectory);
                        restClient.createOsaPdfReport(osaScan.getScanId(), resultFilePath);
                    }
                    //OSA json reports
                    if (jsonFile != null) {
                        if (!isFilenameValid(jsonFile)) {
                            jsonFile = "";
                            log.warn("The path you specified for the Json Reports is invalid.");
                        }
                        String resultFilePath = initFilePath(jsonFile, ".json", workDirectory);
                        restClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred during CxOSA reports. Error message: " + e.getMessage());
            }
        } finally {
            OsaUtils.deleteTempFiles();
            restClient.close();
        }

        return CxConsoleCommand.CODE_OK;
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
        log.info("Vulnerabilities Summary:");
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
        log.info("OSA scan results location: " + osaProjectSummaryLink);
        log.info("-----------------------------------------------------------------------------------------");
    }

}
