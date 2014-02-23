package com.checkmarx.cxviewer.ws;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.generated.*;
import com.checkmarx.cxviewer.ws.resolver.CxClientType;
import com.checkmarx.cxviewer.ws.results.GetConfigurationsListResult;
import com.checkmarx.cxviewer.ws.results.GetPresetsListResult;
import com.checkmarx.cxviewer.ws.results.GetProjectConfigResult;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import com.checkmarx.cxviewer.ws.results.GetStatusOfScanResult;
import com.checkmarx.cxviewer.ws.results.GetTeamsListResult;
import com.checkmarx.cxviewer.ws.results.LoginResult;
import com.checkmarx.cxviewer.ws.results.RunScanResult;
import com.checkmarx.cxviewer.ws.results.UpdateScanCommentResult;
import org.apache.commons.lang3.StringUtils;


public class WSMgr extends WSMgrBase {
	protected static final String WS_NAME="CxCLIWebServiceV1";
	protected CxCLIWebServiceV1Soap wService;
	
	public WSMgr(CxClientType clientType, String version) {
		super(clientType, version);
	}
	
	public String getWSName() {
		return WS_NAME;
	}
	
	public Object connectWebService(URL wsdlLocation) {
		QName serviceName = getWebServiceQName(wsdlLocation);
		
		CxCLIWebServiceV1 ws = new CxCLIWebServiceV1(wsdlLocation, serviceName);
		wService = ws.getCxCLIWebServiceV1Soap();
		return wService;
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
			String privateKey, boolean incremental, boolean visibleToOther, String[] excludeFilesPatterns, String[] excludeFoldersPatterns) {

		RunScanResult responseObj = new RunScanResult();

		CliScanArgs args = new CliScanArgs();
		args.setIsIncremental(incremental);
		args.setIsPrivateScan(!visibleToOther);
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
        filterPatterns.setExcludeFilesPatterns(StringUtils.join(excludeFilesPatterns,','));
        filterPatterns.setExcludeFoldersPatterns(StringUtils.join(excludeFoldersPatterns,','));
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
				case TFS:
                case PERFORCE:
					// sourceControlSetting.setUseSSL(false);
					sourceControlSetting.setUserCredentials(creds);
                    generateScanPaths = true;
					break;
				case GIT:
					sourceControlSetting.setGITBranch(locationBrach);
					if (privateKey != null) {
						sourceControlSetting.setProtocol(SourceControlProtocolType.SSH);
						sourceControlSetting.setUseSSL(true);
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

		if (generateScanPaths && locationpath!=null) {
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
			boolean incremental, boolean visibleToOther) {

		RunScanResult responseObj = new RunScanResult();

		CliScanArgs args = new CliScanArgs();
		args.setIsIncremental(incremental);
		args.setIsPrivateScan(!visibleToOther);

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
		CxWSReportRequest reportRequest=new CxWSReportRequest();
		reportRequest.setScanID(scanId);
		reportRequest.setType(CxWSReportType.fromValue(type));
		CxWSCreateReportResponse resp=wService.createScanReport(sessionId, reportRequest);
		CxLogger.getLogger().trace("ScanStatus response: " + resp);
		if (!resp.isIsSuccesfull()) {
			String err="Cannot create scan("+scanId+") "+type+" report: "+resp.getErrorMessage();
			CxLogger.getLogger().error(err);
			throw new Exception(err);
		}
		
		final long repoId=resp.getID();
		// check status report complete
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		FutureTask<CxWSReportStatusResponse> checkRepoStatusTask=new FutureTask<CxWSReportStatusResponse>(new Thread(), null) {
			CxWSReportStatusResponse statusResp;
            public void run() {
            	statusResp=wService.getScanReportStatus(sessionId, repoId);
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
        	statusResp=checkRepoStatusTask.get(reportTimeout, TimeUnit.MINUTES);
		}
		catch(Exception e) {
			String err="Timeout to get scan("+scanId+") "+type+" report("+repoId+")";
			CxLogger.getLogger().error(err);
			throw new Exception(err);
		}
        if (statusResp.isIsFailed()) {
			String err="Cannot get scan("+scanId+") "+type+" report("+repoId+") status: "+statusResp.getErrorMessage();
			CxLogger.getLogger().error(err);
			throw new Exception(err);
		}
		// get status report data
		CxWSResponseScanResults repoResp=wService.getScanReport(sessionId, repoId);
		if (!repoResp.isIsSuccesfull()) {
			String err="Cannot get data of scan("+scanId+") "+type+" report("+repoId+"): "+resp.getErrorMessage();
			CxLogger.getLogger().error(err);
			throw new Exception(err);
		}
		return repoResp.getScanResults();
	}
}
