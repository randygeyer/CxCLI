package com.checkmarx.login.soap;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.exceptions.CxSoapSASTClientException;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import com.checkmarx.parameters.CLIScanParametersSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapSASTClient {

    private CxCLIWebServiceV1Soap cxSoapClient;
    private Logger log = Logger.getLogger(LOG_NAME);

    private static final String SOAP_ACTION_URL = "http://Checkmarx.com/v7/GetScanSummary";
    private static final String SDK_URL = "/Cxwebinterface/sdk/cxsdkwebservice.asmx";

    public CxSoapSASTClient(CxCLIWebServiceV1Soap cxSoapClient) {
        this.cxSoapClient = cxSoapClient;
    }

    public CxWSBasicRepsonse updateScanComment(String sessionID, long scanID, String comment) throws CxSoapClientValidatorException {
        CxWSBasicRepsonse response = cxSoapClient.updateScanComment(sessionID, scanID, comment);

        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            throw new CxSoapClientValidatorException("Error retrieve comment update response from server: " + e.getMessage());
        }

        log.trace("UpdateScanComment response: " + response);
        return response;
    }

    public CxWSResponseProjectsDisplayData getProjectsDisplayData(String sessionId) throws CxSoapClientValidatorException {
        CxWSResponseProjectsDisplayData response = cxSoapClient.getProjectsDisplayData(sessionId);

        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            throw new CxSoapClientValidatorException("Error retrieve project display data from server: " + e.getMessage());
        }

        log.trace("ProjectsData response: " + response);
        return response;
    }

    public CxWSResponseProjectConfig getProjectConfiguration(String sessionId, long projectId) throws CxSoapClientValidatorException {
        CxWSResponseProjectConfig response = cxSoapClient.getProjectConfiguration(sessionId, projectId);

        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.error("Error retrieve configuration from server: " + e.getMessage());
            throw new CxSoapClientValidatorException("Error retrieve configuration from server: " + e.getMessage());
        }

        log.trace("ProjectsConfig response: " + response.getProjectConfig());
        return response;
    }

    public CxWSResponseRunID cliScan(String sessionId, long presetId, long configId, SourceLocationType locationType, byte[] fileBytes, RepositoryType repositoryType, CLIScanParametersSingleton parameters) throws CxSoapSASTClientException {

        CliScanArgs args = new CliScanArgs();
        args.setIsIncremental(parameters.getCliSastParameters().isIncrementalScan());
        args.setIsPrivateScan(!parameters.getCliSharedParameters().isVisibleOthers());
        args.setIgnoreScanWithUnchangedCode(parameters.getCliSastParameters().isForceScan());
        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setProjectName(parameters.getCliMandatoryParameters().getProjectNameWithPath());
        projectSettings.setPresetID(presetId);
        projectSettings.setScanConfigurationID(configId);

        // Source code setting
        SourceCodeSettings srcCodeSettings = new SourceCodeSettings();

        LocalCodeContainer localCodeContainer;
        srcCodeSettings.setSourceOrigin(locationType);
        SourceFilterPatterns filterPatterns = new SourceFilterPatterns();
        filterPatterns.setExcludeFilesPatterns(StringUtils.join(parameters.getCliSastParameters().getExcludedFiles(), ','));
        filterPatterns.setExcludeFoldersPatterns(StringUtils.join(parameters.getCliSastParameters().getExcludedFolders(), ','));
        srcCodeSettings.setSourceFilterLists(filterPatterns);
        boolean generateScanPaths = false;

        Credentials creds = new Credentials();
        creds.setUser(parameters.getCliSastParameters().getLocationUser());
        creds.setPass(parameters.getCliSastParameters().getLocationPass());
        switch (locationType) {
            case LOCAL:
                localCodeContainer = new LocalCodeContainer();
                localCodeContainer.setZippedFile(fileBytes);
                localCodeContainer.setFileName(parameters.getCliSharedParameters().getLocationPath());
                srcCodeSettings.setPackagedCode(localCodeContainer);
                break;
            case SHARED:
                srcCodeSettings.setUserCredentials(creds);
                generateScanPaths = true;
                break;
            case SOURCE_CONTROL:
                SourceControlSettings sourceControlSetting = new SourceControlSettings();
                sourceControlSetting.setServerName(parameters.getCliSastParameters().getLocationURL());
                if (parameters.getCliSastParameters().getLocationPort() != null) {
                    sourceControlSetting.setPort(parameters.getCliSastParameters().getLocationPort());
                }
                sourceControlSetting.setRepository(repositoryType);
                switch (repositoryType) {
                    case SVN:
                        sourceControlSetting.setUserCredentials(creds);
                        generateScanPaths = true;
                        if (parameters.getCliSastParameters().getPrivateKey() != null) {
                            initPrivateKey(sourceControlSetting, parameters.getCliSastParameters().getPrivateKey());
                        }
                        break;
                    case TFS:
                    case PERFORCE:
                        sourceControlSetting.setUserCredentials(creds);
                        generateScanPaths = true;
                        if (parameters.getCliSastParameters().isPerforceWorkspaceMode()) {
                            sourceControlSetting.setPerforceBrowsingMode(CxWSPerforceBrowsingMode.WORKSPACE);
                        } else {
                            sourceControlSetting.setPerforceBrowsingMode(CxWSPerforceBrowsingMode.DEPOT);
                        }
                        break;
                    case GIT:
                        sourceControlSetting.setGITBranch(parameters.getCliSastParameters().getLocationBranch());
                        if (parameters.getCliSastParameters().getPrivateKey() != null) {
                            initPrivateKey(sourceControlSetting, parameters.getCliSastParameters().getPrivateKey());
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

        if (generateScanPaths && parameters.getCliSharedParameters().getLocationPath() != null) {
            ArrayOfScanPath paths = new ArrayOfScanPath();

            for (String lpath : parameters.getCliSharedParameters().getLocationPath().split(";")) {
                ScanPath lscanPath = new ScanPath();
                lscanPath.setPath(lpath);
                lscanPath.setIncludeSubTree(false);

                paths.getScanPath().add(lscanPath);

            }
            srcCodeSettings.setPathList(paths);
        }

        args.setPrjSettings(projectSettings);
        args.setSrcCodeSettings(srcCodeSettings);

        CxWSResponseRunID response = cxSoapClient.scan(sessionId, args);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            throw new CxSoapSASTClientException("Error scanning project: " + e.getMessage());
        }

        log.trace("cliScan response: " + response);
        return response;
    }

    private void initPrivateKey(SourceControlSettings sourceControlSetting, String privateKey) {
        sourceControlSetting.setUseSSH(true);
        sourceControlSetting.setProtocol(SourceControlProtocolType.SSH);
        sourceControlSetting.setSSHPrivateKey(privateKey);
        sourceControlSetting.setSSHPublicKey("EmptyStab");
    }

    public CxWSResponseScanStatus getStatusOfScan(String runId, String sessionId) throws CxSoapSASTClientException {
        CxWSResponseScanStatus response = cxSoapClient.getStatusOfSingleScan(sessionId, runId);

        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            throw new CxSoapSASTClientException("Error retrieving scan status from server: " + e.getMessage());
        }

        log.trace("ScanStatus response: " + response);
        return response;
    }

    public byte[] getScanReport(final String sessionId, final long scanId, final String type) throws CxSoapSASTClientException {
        // create status report
        CxWSReportRequest reportRequest = new CxWSReportRequest();
        reportRequest.setScanID(scanId);
        reportRequest.setType(CxWSReportType.fromValue(type));
        final CxWSCreateReportResponse resp = cxSoapClient.createScanReport(sessionId, reportRequest);
        log.trace("ScanStatus response: " + resp);
        if (!resp.isIsSuccesfull()) {
            String err = "Cannot create scan(" + scanId + ") " + type + " report: " + resp.getErrorMessage();
            log.error(err);
            throw new CxSoapSASTClientException(err);
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
            log.error(err);
            throw new CxSoapSASTClientException(err);
        }
        if (statusResp.isIsFailed()) {
            String err = "Cannot get scan(" + scanId + ") " + type + " report(" + repoId + ") status: " + statusResp.getErrorMessage();
            log.error(err);
            throw new CxSoapSASTClientException(err);
        }
        // get status report dto
        CxWSResponseScanResults repoResp = cxSoapClient.getScanReport(sessionId, repoId);
        if (!repoResp.isIsSuccesfull()) {
            String err = "Cannot get dto of scan(" + scanId + ") " + type + " report(" + repoId + "): " + repoResp.getErrorMessage();
            log.error(err);
            log.error(repoResp.isContainsAllResults());
            throw new CxSoapSASTClientException(err);
        }
        return repoResp.getScanResults();
    }

    public String getScanSummary(String cliServer, String sessionId, long scanId) throws CxSoapSASTClientException {
        String sdkServer = cliServer.concat(SDK_URL);
        URL wsURL = null;
        URLConnection connection;
        try {
            wsURL = new URL(sdkServer);
            connection = wsURL.openConnection();

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
        } catch (IOException e) {
            throw new CxSoapSASTClientException("Error retrieve SAST scan results: " + e.getMessage());
        }
    }
}
