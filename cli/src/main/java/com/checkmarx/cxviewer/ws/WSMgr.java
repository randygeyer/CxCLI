package com.checkmarx.cxviewer.ws;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.utils.CXFConfigurationUtils;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.cxviewer.ws.results.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class WSMgr extends WSMgrBase {
    final static private Logger logger = Logger.getLogger(WSMgr.class);
    protected static final String WS_NAME = "CxCLIWebServiceV1";
    protected CxCLIWebServiceV1Soap wService;
    private static final String SOAP_ACTION_URL = "http://Checkmarx.com/v7/GetScanSummary";
    private static final String SDK_URL = "/Cxwebinterface/sdk/cxsdkwebservice.asmx";
    private static final String CLI_URL = "/cxwebinterface/CLI/CxCLIWebServiceV1.asmx";


    @Override
    public String getWSName() {
        return WS_NAME;
    }

    @Override
    public void connectWebService(URL wsdlLocation) {
        try {
            final URL wsdlLocationWithWSDL = new URL(wsdlLocation.toString() + "?WSDL");
            CxCLIWebServiceV1 ws = new CxCLIWebServiceV1(wsdlLocationWithWSDL);
            wService = ws.getCxCLIWebServiceV1Soap();

            CXFConfigurationUtils.disableSchemaValidation(wService);

            if ("false".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                CXFConfigurationUtils.setNTLMAuthentication(wService);
            }

        } catch (MalformedURLException e) {
            // We should never get here, as the correctness of wsdlLocation was already checked
            logger.fatal("Malformed URL: " + wsdlLocation);
        }
    }

    public LoginResult login(String userName, String password) {
        LoginResult responseObj = new LoginResult();
        Credentials creds = new Credentials();
        creds.setUser(userName);
        creds.setPass(password);

        CxWSBasicRepsonse responce = wService.login(creds, 1033);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("login response :" + responseObj);

        return responseObj;
    }

    public LoginResult ssoLogin(String userName, String sid) {
        LoginResult responseObj = new LoginResult();
        Credentials encCreds = new Credentials();
        encCreds.setUser(userName);
        encCreds.setPass(sid);

        CxWSBasicRepsonse responce = wService.ssoLogin(encCreds, 1033);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().info("login response :" + responseObj);

        return responseObj;
    }

    public GetPresetsListResult getPresetsList(String sessionId) {
        GetPresetsListResult responseObj = new GetPresetsListResult();
        CxWSBasicRepsonse responce = wService.getPresetList(sessionId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("Presets list response: " + responseObj);

        return responseObj;
    }

    public GetTeamsListResult getTeamsList(String sessionId) {
        GetTeamsListResult responseObj = new GetTeamsListResult();
        CxWSBasicRepsonse responce = wService.getAssociatedGroupsList(sessionId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("Teams list response: " + responseObj);

        return responseObj;
    }

    public GetConfigurationsListResult getConfigurationsList(String sessionId) {
        GetConfigurationsListResult responseObj = new GetConfigurationsListResult();
        CxWSBasicRepsonse responce = wService.getConfigurationSetList(sessionId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("Configurations list response: " + responseObj);

        return responseObj;
    }

    public UpdateScanCommentResult updateScanComment(String sessionID, long scanID, String comment) {
        CxWSBasicRepsonse responce = wService.updateScanComment(sessionID, scanID, comment);

        UpdateScanCommentResult responseObj = new UpdateScanCommentResult();
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("UpdateScanComment response: " + responseObj);

        return responseObj;
    }

    public GetProjectDataResult getProjectsDisplayData(String sessionId) {
        GetProjectDataResult responseObj = new GetProjectDataResult();
        CxWSBasicRepsonse responce = wService.getProjectsDisplayData(sessionId);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("ProjectsData response: " + responseObj);

        return responseObj;
    }

    public GetProjectConfigResult getProjectConfiguration(String sessionId, long projectId) {
        GetProjectConfigResult responseObj = new GetProjectConfigResult();
        CxWSBasicRepsonse responce = wService.getProjectConfiguration(sessionId, projectId);
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
        // projectSettings.setAssociatedGroupID(usergroup);
        // projectSettings.setProjectID(projectId);
        projectSettings.setProjectName(fullProjectName);
        projectSettings.setPresetID(presetId);
        projectSettings.setScanConfigurationID(configId);
        // projectSettings.setAssociatedGroupID(null);

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
                    // sourceControlSetting.setRepositoryName(locationRepository);
                    if (locationport != null) {
                        sourceControlSetting.setPort(locationport);
                    }
                    // sourceControlSetting.setPort(0);
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
                            // sourceControlSetting.setUseSSL(false);
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

        // srcCodeSettings.setPathList(null);
        // srcCodeSettings.setSourcePullingAction(null);
        args.setPrjSettings(projectSettings);
        args.setSrcCodeSettings(srcCodeSettings);

        CxWSBasicRepsonse responce = wService.scan(sessionId, args);
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

        CxWSBasicRepsonse responce = wService.scan(sessionId, args);
        responseObj.parseResponseObject(responce);
        CxLogger.getLogger().trace("cliScan response: " + responseObj);

        return responseObj;
    }

    public GetStatusOfScanResult getStatusOfScan(String runId, String sessionId) {
        CxWSBasicRepsonse responce = wService.getStatusOfSingleScan(sessionId, runId);

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
        CxWSCreateReportResponse resp = wService.createScanReport(sessionId, reportRequest);
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
                statusResp = wService.getScanReportStatus(sessionId, repoId);
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
        // get status report data
        CxWSResponseScanResults repoResp = wService.getScanReport(sessionId, repoId);
        if (!repoResp.isIsSuccesfull()) {
            String err = "Cannot get data of scan(" + scanId + ") " + type + " report(" + repoId + "): " + resp.getErrorMessage();
            CxLogger.getLogger().error(err);
            throw new Exception(err);
        }
        return repoResp.getScanResults();
    }

    public String getScanSummary(String cliServer, String sessionId, long scanId) throws Exception {
        String sdkServer = cliServer.replace(CLI_URL, SDK_URL);
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
