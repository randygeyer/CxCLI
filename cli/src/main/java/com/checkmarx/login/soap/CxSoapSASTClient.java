package com.checkmarx.login.soap;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.cxviewer.ws.results.*;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapSASTClient {

    private CxCLIWebServiceV1Soap cxSoapClient;

    private static final String SOAP_ACTION_URL = "http://Checkmarx.com/v7/GetScanSummary";
    private static final String SDK_URL = "/Cxwebinterface/sdk/cxsdkwebservice.asmx";

    public CxSoapSASTClient(CxCLIWebServiceV1Soap cxSoapClient) {
        this.cxSoapClient = cxSoapClient;
    }

    public UpdateScanCommentResult updateScanComment(String sessionID, long scanID, String comment) {
        CxWSBasicRepsonse response = cxSoapClient.updateScanComment(sessionID, scanID, comment);

        UpdateScanCommentResult responseObj = new UpdateScanCommentResult();
        responseObj.parseResponseObject(response);
        CxLogger.getLogger().trace("UpdateScanComment response: " + responseObj);

        return responseObj;
    }

    public GetProjectDataResult getProjectsDisplayData(String sessionId) {
        GetProjectDataResult responseObj = new GetProjectDataResult();
        CxWSBasicRepsonse responce = cxSoapClient.getProjectsDisplayData(sessionId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("ProjectsData response: " + responseObj);

        return responseObj;
    }

    public GetProjectConfigResult getProjectConfiguration(String sessionId, long projectId) {
        GetProjectConfigResult responseObj = new GetProjectConfigResult();
        CxWSBasicRepsonse responce = cxSoapClient.getProjectConfiguration(sessionId, projectId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("ProjectsConfig response: " + responseObj);
        return responseObj;
    }

    public RunScanResult cliScan(String sessionId, String fullProjectName, long presetId, long configId,
                                 SourceLocationType locationType, String locationpath, byte[] fileBytes, String user, String password,
                                 RepositoryType repositoryType, String locationURL, Integer locationport, String locationBrach,
                                 String privateKey, boolean incremental, boolean visibleToOther, String[] excludeFilesPatterns,
                                 String[] excludeFoldersPatterns, boolean ignoreScanWithUnchangedSource, boolean isPerforceWorkspaceMode) {

        RunScanResult responseObj = new RunScanResult();

        CliScanArgs args = new CliScanArgs();
        args.setIsIncremental(incremental);
        args.setIsPrivateScan(!visibleToOther);
        args.setIgnoreScanWithUnchangedCode(ignoreScanWithUnchangedSource);
        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setProjectName(fullProjectName);
        projectSettings.setPresetID(presetId);
        projectSettings.setScanConfigurationID(configId);

        // Source code setting
        SourceCodeSettings srcCodeSettings = new SourceCodeSettings();

        LocalCodeContainer localCodeContainer;
        srcCodeSettings.setSourceOrigin(locationType);
        SourceFilterPatterns filterPatterns = new SourceFilterPatterns();
        filterPatterns.setExcludeFilesPatterns(StringUtils.join(excludeFilesPatterns, ','));
        filterPatterns.setExcludeFoldersPatterns(StringUtils.join(excludeFoldersPatterns, ','));
        srcCodeSettings.setSourceFilterLists(filterPatterns);
        boolean generateScanPaths = false;

        Credentials creds = new Credentials();
        creds.setUser(user);
        creds.setPass(password);
        if (locationType != null) {
            switch (locationType) {
                case LOCAL:
                    localCodeContainer = new LocalCodeContainer();
                    localCodeContainer.setZippedFile(fileBytes);
                    localCodeContainer.setFileName(locationpath);
                    srcCodeSettings.setPackagedCode(localCodeContainer);
                    break;
                case SHARED:
                    srcCodeSettings.setUserCredentials(creds);
                    generateScanPaths = true;
                    break;
                case SOURCE_CONTROL:
                    SourceControlSettings sourceControlSetting = new SourceControlSettings();
                    sourceControlSetting.setServerName(locationURL);
                    if (locationport != null) {
                        sourceControlSetting.setPort(locationport);
                    }
                    sourceControlSetting.setRepository(repositoryType);
                    switch (repositoryType) {
                        case SVN:
                            sourceControlSetting.setUserCredentials(creds);
                            generateScanPaths = true;
                            if (privateKey != null) {
                                sourceControlSetting.setUseSSH(true);
                                sourceControlSetting.setProtocol(SourceControlProtocolType.SSH);
                                sourceControlSetting.setSSHPrivateKey(privateKey);
                                sourceControlSetting.setSSHPublicKey("EmptyStab");
                            }
                            break;
                        case TFS:
                        case PERFORCE:
                            sourceControlSetting.setUserCredentials(creds);
                            generateScanPaths = true;
                            if (isPerforceWorkspaceMode) {
                                sourceControlSetting.setPerforceBrowsingMode(CxWSPerforceBrowsingMode.WORKSPACE);
                            } else {
                                sourceControlSetting.setPerforceBrowsingMode(CxWSPerforceBrowsingMode.DEPOT);
                            }
                            break;
                        case GIT:
                            sourceControlSetting.setGITBranch(locationBrach);
                            if (privateKey != null) {
                                sourceControlSetting.setUseSSH(true);
                                sourceControlSetting.setProtocol(SourceControlProtocolType.SSH);
                                sourceControlSetting.setSSHPrivateKey(privateKey);
                                sourceControlSetting.setSSHPublicKey("EmptyStab");
                            }
                            break;
                        default:
                            break;
                    }
                    srcCodeSettings.setSourceControlSetting(sourceControlSetting);
                    break;
                default:
                    break;
            }
        }

        if (generateScanPaths && locationpath != null) {
            ArrayOfScanPath paths = new ArrayOfScanPath();

            for (String lpath : locationpath.split(";")) {
                ScanPath lscanPath = new ScanPath();
                lscanPath.setPath(lpath);
                lscanPath.setIncludeSubTree(false);

                paths.getScanPath().add(lscanPath);

            }
            srcCodeSettings.setPathList(paths); // TODO: Check when the pathList web service parameter is needed
        }

        args.setPrjSettings(projectSettings);
        args.setSrcCodeSettings(srcCodeSettings);

        CxWSBasicRepsonse responce = cxSoapClient.scan(sessionId, args);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("cliScan response: " + responseObj);

        return responseObj;
    }

    public RunScanResult cliScan(String sessionId, ProjectSettings projectSettings, SourceCodeSettings srcCodeSettings,
                                 boolean incremental, boolean visibleToOther, boolean ignoreScanWithUnchangedSource) {

        RunScanResult responseObj = new RunScanResult();

        CliScanArgs args = new CliScanArgs();
        args.setIsIncremental(incremental);
        args.setIsPrivateScan(!visibleToOther);
        args.setIgnoreScanWithUnchangedCode(ignoreScanWithUnchangedSource);

        args.setPrjSettings(projectSettings);
        args.setSrcCodeSettings(srcCodeSettings);

        CxWSBasicRepsonse responce = cxSoapClient.scan(sessionId, args);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("cliScan response: " + responseObj);

        return responseObj;
    }

    public GetStatusOfScanResult getStatusOfScan(String runId, String sessionId) {
        CxWSBasicRepsonse responce = cxSoapClient.getStatusOfSingleScan(sessionId, runId);

        GetStatusOfScanResult responseObj = new GetStatusOfScanResult();
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("ScanStatus response: " + responseObj);

        return responseObj;
    }

    public byte[] getScanReport(final String sessionId, final long scanId, final String type) throws Exception {
        // create status report
        CxWSReportRequest reportRequest = new CxWSReportRequest();
        reportRequest.setScanID(scanId);
        reportRequest.setType(CxWSReportType.fromValue(type));
        CxWSCreateReportResponse resp = cxSoapClient.createScanReport(sessionId, reportRequest);
        CxLogger.getLogger().trace("ScanStatus response: " + resp);
        if (!resp.isIsSuccesfull()) {
            String err = "Cannot create scan(" + scanId + ") " + type + " report: " + resp.getErrorMessage();
            CxLogger.getLogger().error(err);
            throw new Exception(err);
        }

        final long repoId = resp.getID();
        // check status report complete
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        FutureTask<CxWSReportStatusResponse> checkRepoStatusTask = new FutureTask<CxWSReportStatusResponse>(new Thread(), null) {
            CxWSReportStatusResponse statusResp;

            @Override
            public void run() {
                statusResp = cxSoapClient.getScanReportStatus(sessionId, repoId);
                if (statusResp.isIsReady()) {
                    // status report is ready
                    scheduler.shutdown();
                    set(statusResp);
                }
                //continue task
            }
        };

        scheduler.scheduleAtFixedRate(checkRepoStatusTask, 1, 3, TimeUnit.SECONDS);
        CxWSReportStatusResponse statusResp;
        int reportTimeout = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.REPORT_TIMEOUT);
        try {
            statusResp = checkRepoStatusTask.get(reportTimeout, TimeUnit.MINUTES);
        } catch (Exception e) {
            String err = "Timeout to get scan(" + scanId + ") " + type + " report(" + repoId + ")";
            CxLogger.getLogger().error(err);
            throw new Exception(err);
        }
        if (statusResp.isIsFailed()) {
            String err = "Cannot get scan(" + scanId + ") " + type + " report(" + repoId + ") status: " + statusResp.getErrorMessage();
            CxLogger.getLogger().error(err);
            throw new Exception(err);
        }
        // get status report dto
        CxWSResponseScanResults repoResp = cxSoapClient.getScanReport(sessionId, repoId);
        if (!repoResp.isIsSuccesfull()) {
            String err = "Cannot get dto of scan(" + scanId + ") " + type + " report(" + repoId + "): " + resp.getErrorMessage();
            CxLogger.getLogger().error(err);
            throw new Exception(err);
        }
        return repoResp.getScanResults();
    }

    public String getScanSummary(String cliServer, String sessionId, long scanId) throws Exception {
        String sdkServer = cliServer.concat(SDK_URL);
        URL wsURL = new URL(sdkServer);
        URLConnection connection = wsURL.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        String xmlInput =
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v7=\"http://Checkmarx.com/v7\">\n" +
                        "   <soap:Header/>\n" +
                        "   <soap:Body>\n" +
                        "      <v7:GetScanSummary>\n" +
                        "         <!--Optional:-->\n" +
                        "         <v7:SessionID>" + sessionId + "</v7:SessionID>\n" +
                        "         <v7:ScanID>" + Long.toString(scanId) + "</v7:ScanID>\n" +
                        "      </v7:GetScanSummary>\n" +
                        "   </soap:Body>\n" +
                        "</soap:Envelope>\n";

        bout.write(xmlInput.getBytes());
        byte[] b = bout.toByteArray();
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction", SOAP_ACTION_URL);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        OutputStream out = httpConn.getOutputStream();
        //Write the content of the request to the outputstream of the HTTP Connection.
        out.write(b);
        out.close();

        //Read the response.
        InputStreamReader isr =
                new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);

        //Write the SOAP message response to a String.
        String outputString = "";
        String responseString = "";
        StringBuilder sb = new StringBuilder();
        while ((responseString = in.readLine()) != null) {
            sb.append(responseString);
        }
        outputString = sb.toString();

        return outputString;
    }
}
