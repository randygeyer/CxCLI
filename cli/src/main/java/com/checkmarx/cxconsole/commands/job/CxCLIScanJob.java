package com.checkmarx.cxconsole.commands.job;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.cxconsole.commands.CxConsoleCommand;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.LocationType;
import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.cxviewer.ws.generated.ConfigurationSet;
import com.checkmarx.cxviewer.ws.generated.Credentials;
import com.checkmarx.cxviewer.ws.generated.CurrentStatusEnum;
import com.checkmarx.cxviewer.ws.generated.Preset;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.cxviewer.ws.generated.ProjectSettings;
import com.checkmarx.cxviewer.ws.generated.RepositoryType;
import com.checkmarx.cxviewer.ws.generated.SourceCodeSettings;
import com.checkmarx.cxviewer.ws.generated.SourceFilterPatterns;
import com.checkmarx.cxviewer.ws.generated.SourceLocationType;
import com.checkmarx.cxviewer.ws.results.GetConfigurationsListResult;
import com.checkmarx.cxviewer.ws.results.GetPresetsListResult;
import com.checkmarx.cxviewer.ws.results.GetProjectConfigResult;
import com.checkmarx.cxviewer.ws.results.GetProjectDataResult;
import com.checkmarx.cxviewer.ws.results.RunScanResult;
import com.checkmarx.cxviewer.ws.results.UpdateScanCommentResult;

public class CxCLIScanJob extends CxScanJob {

	private byte[] zippedSourcesBytes;
    private long projectId = -1;
	
	private List<Preset> presets;
	private Preset selectedPreset;
	private List<ConfigurationSet> configs;
	private ConfigurationSet selectedConfig;
	private GetProjectConfigResult projectConfig;
	
	public CxCLIScanJob(ScanParams params) {
		super(params);
	}
	
	@Override
	public Integer call() throws Exception {
		
		//String projectName = getProjectValidName();
		if (log.isEnabledFor(Level.INFO)) {
			log.info("Project name is \"" + params.getProjName()  + "\"");
		}
		
		// Connect
		wsMgr = ConfigMgr.getWSMgr();		
		URL wsdlLocation = wsMgr.makeWsdlLocation(params.getHost());

		// Login
		login(wsdlLocation);
		
		// locate project and get corresponding projectId
		if (log.isEnabledFor(Level.INFO)) {
			log.info("Read preset and configuration settings");
		}
		locateProjectOnServer(params.getFullProjName(), sessionId);
		
		if (params.getLocationType() == null && this.projectConfig!=null) {
			if (!this.projectConfig.getProjectConfig().getSourceCodeSettings().getSourceOrigin().equals(SourceLocationType.LOCAL)) {
				params.setLocationType(getLocationType(this.projectConfig.getProjectConfig().getSourceCodeSettings()));
			}
		}
		
		if (params.getLocationType() == LocationType.folder) {
			long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_MAX_ZIP_SIZE);
			maxZipSize *= (1024*1024);
			
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
		checkProjectType(sessionId, projectId);
		
		// request scan
		if (log.isEnabledFor(Level.INFO)) {
			log.info("Request scan");
		}
		requestScan(sessionId);
		
		// wait for scan completion
		ExecutorService executor = Executors.newSingleThreadExecutor();
		WaitScanCompletionJob waiterJob = new WaitScanCompletionJob(wsMgr, sessionId, runId);
		waiterJob.setLog(log);
		try {
			Future<Boolean> furute = executor.submit(waiterJob);
			// wait for scan completion
			furute.get();
			
			scanId=waiterJob.getScanId();
		}
		catch (ExecutionException e) {
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

		if (params.isIgnoreScanWithUnchangedSource() && scanId == -1 && waiterJob.getCurrentStatusEnum() == CurrentStatusEnum.FINISHED) {
            log.info("Scan finished with ScanId = (-1): finish Scan Job");
            return CxConsoleCommand.CODE_OK;
		}

		//update scan comment
		String comment=params.getScanComment();
		if (comment!=null) {
			UpdateScanCommentResult result=wsMgr.updateScanComment(sessionId, scanId, comment);
			if (!result.isSuccesfullResponce()) {
				log.warn("Cannot update the scan comment: "+result.getErrorMessage());
			}
		}
		
		//get results
		if (log.isEnabledFor(Level.INFO)) {
			log.info("Retrieving results");
		}
		
		if (params.getReportType()!=null) {
			log.info("report type: " + params.getReportType());
			String resultsPath = params.getReportFile();
			if (resultsPath == null) {
				resultsPath = normalizePathString(params.getProjName()) + "."+params.getReportType().toLowerCase();
			}
			downloadAndStoreReport(resultsPath, params.getReportType());
		}

		// Store to xml anyway
		String resultsFileName = params.getXmlFile();
		if (resultsFileName == null) {
			resultsFileName = normalizePathString(params.getProjName()) + ".xml";
		}

		//Document doc = getResultsXML();
		storeXMLResults(resultsFileName, wsMgr.getScanReport(sessionId, scanId, "XML"));

		
		return CxConsoleCommand.CODE_OK;
	}
	
	private LocationType getLocationType(SourceCodeSettings scSettings) {
		SourceLocationType slType=scSettings.getSourceOrigin();
		if (slType.equals(SourceLocationType.LOCAL)) {
			return LocationType.folder;
		}
		else
		if (slType.equals(SourceLocationType.SHARED)) {
			return LocationType.shared;
		}
		else
		if (slType.equals(SourceLocationType.SOURCE_CONTROL)) {
			RepositoryType rType=scSettings.getSourceControlSetting().getRepository();
			if (rType.equals(RepositoryType.TFS)) {
				return LocationType.tfs;
			}
			else
			if (rType.equals(RepositoryType.GIT)) {
				return LocationType.git;
			}
			else
			if (rType.equals(RepositoryType.SVN)) {
				return LocationType.svn;
			}
            else
            if (rType.equals(RepositoryType.PERFORCE)) {
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
		
		GetPresetsListResult presetsResult = wsMgr.getPresetsList(sessionId);
		presets = presetsResult.getPresetList();
		GetConfigurationsListResult configsResult = wsMgr.getConfigurationsList(sessionId);
		configs = configsResult.getConfigList();
		
		if (log.isEnabledFor(Level.TRACE)) {
			log.trace("Preset response:" + presetsResult);
			log.trace("Configurations response:" + configsResult);
		}
		if (params.getLocationType() == null) {
			int getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(
					ConfigMgr.KEY_PROGRESS_INTERVAL);
			
			while ((getPrjsResult == null || !getPrjsResult.isSuccesfullResponce())
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
					getPrjsResult = wsMgr.getProjectsDisplayData(sessionId);
				} catch (Throwable e) {
					errMsg = e.getMessage();
					count++;
					if (log.isEnabledFor(Level.TRACE)) {
						log.trace("Error during fetching existing projects data.", e);
					}
		
					if (log.isEnabledFor(Level.INFO)) {
						log.info("Error occurred during fetching existing projects data: " + errMsg + ". Operation retry " + count);
					}
				}
				
				if ((getPrjsResult != null) && !getPrjsResult.isSuccesfullResponce()) {
					errMsg = getPrjsResult.getErrorMessage();
					if (log.isEnabledFor(Level.ERROR)) {
						log.error("Existing projects data fetching was unsuccessful.");
					}
					count++;
					if (log.isEnabledFor(Level.INFO)) {
						log.info("Existing projects data fetching unsuccessful: " + getPrjsResult.getErrorMessage() + ". Operation retry " + count);
					}
				}
				
				if ((getPrjsResult == null || !getPrjsResult.isSuccesfullResponce())
						&& count < retriesNum) {
					try {
						Thread.sleep(getStatusInterval * 1000);
					} catch (InterruptedException ex) {
						// no-op
					}
				}
			}
			
			if ((getPrjsResult != null) && !getPrjsResult.isSuccesfullResponce()) {
				throw new Exception("Existing projects data fetching was unsuccessful. " + (errMsg == null ? "" : errMsg));
			} else if (getPrjsResult == null) {
				throw new Exception("Error occurred during existing projects data fetching. " + errMsg);
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
							fullProjectName += ("\\" +locationParts[i]);
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
					log=Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
					log.error("Scan command failed since no source location was provided.");
					throw new Exception("Scan command failed since no source location was provided.");
				}
			}
			
			projectConfig = wsMgr.getProjectConfiguration(sessionId, projectId);
			if ((projectConfig != null) && !projectConfig.isSuccesfullResponce()) {
				throw new Exception("Project configuration fetching was unsuccessful. " 
						+ (projectConfig.getErrorMessage() == null ? "" : projectConfig.getErrorMessage()));
			}
			
			if (log.isEnabledFor(Level.TRACE)) {
				log.trace("Existing projects data response:" + getPrjsResult);
			}
		}
	}
	
	private void checkProjectType(String sessionId, long projectId) throws Exception {
	
		if (params.getPresetName() != null) {
			selectedPreset = null;
			if (presets != null) {
				for (Preset preset : presets) {
					if (preset.getPresetName().equals(params.getPresetName())) {
						selectedPreset = preset;
						break;
					}
				}
				
				if (selectedPreset == null) {
					throw new Exception("Preset [" + params.getPresetName() + "] is not found");
				}
			}
		} else {
			if (presets != null && presets.size() > 0) {
				selectedPreset = new Preset(); // Zero preset will be send. Server will decide what preset to use.
			}
		}
		
		if (params.getConfiguration() != null) {
			selectedConfig = null;
			if (configs != null) {
				for (ConfigurationSet config : configs) {
					if (config.getConfigSetName().equals(
							params.getConfiguration())) {
						selectedConfig = config;
						break;
					}
				}

				if (selectedConfig == null) {
					throw new Exception("Configuration ["
							+ params.getConfiguration() + "] is not found");
				}
			}
		} else {
			if (configs != null) {
				for (ConfigurationSet config : configs) {
					if (config.getConfigSetName().equals("Default Configuration")) {
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
		if (params.getLocationType() != null) {
			switch (params.getLocationType()) {
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
				log=Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
				log.error("Scan command failed since no source location was provided.");
				throw new Exception("Scan command failed since no source location was provided.");
			}
		}
		
		// Start scan
		int getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

		while ((runScanResult == null || !runScanResult.isSuccesfullResponce())	&& count < retriesNum) {
			
			try {
				if (params.getLocationType() == null) {
					ProjectSettings prjSett = projectConfig.getProjectConfig().getProjectSettings();
					SourceCodeSettings srcCodeSett = projectConfig.getProjectConfig().getSourceCodeSettings();
					if (params.getLocationUser() != null &&  params.getLocationPassword() != null) {
						Credentials creds = new Credentials();
						creds.setUser(params.getLocationUser());
						creds.setPass(params.getLocationPassword());
						srcCodeSett.setUserCredentials(creds);
					}
					
					if (params.getLocationBranch() != null) {
						srcCodeSett.getSourceControlSetting().setGITBranch(params.getLocationBranch());
					}

                    SourceFilterPatterns filterPatterns = new SourceFilterPatterns();
                    filterPatterns.setExcludeFilesPatterns(StringUtils.join(params.getExcludedFiles(),','));
                    filterPatterns.setExcludeFoldersPatterns(StringUtils.join(params.getExcludedFolders(),','));
                    srcCodeSett.setSourceFilterLists(filterPatterns);

					runScanResult = wsMgr.cliScan(sessionId, prjSett, srcCodeSett,params.isValidateFix(), params.isVisibleOthers(),params.isIgnoreScanWithUnchangedSource());
				}
				else {
					runScanResult = wsMgr.cliScan(sessionId, /*"CxServer\\" +*/ params.getFullProjName(),
							(selectedPreset == null ? 0 : selectedPreset.getID()), 
							(selectedConfig == null ? 0 : selectedConfig.getID()),
							locationType, params.getLocationPath(), zippedSourcesBytes,
							params.getLocationUser(), params.getLocationPassword(),
							repoType, params.getLocationURL(), 
							params.getLocationPort(), params.getLocationBranch(),
							params.getPrivateKey(),
							params.isValidateFix(), params.isVisibleOthers(),
                            params.getExcludedFiles(), params.getExcludedFolders(), params.isIgnoreScanWithUnchangedSource());
				}
			}
			catch (Throwable e) {
				errMsg = e.getMessage();
				count++;
				if (log.isEnabledFor(Level.TRACE)) {
					log.trace("Error during quering existing project scan run.", e);
				}

				if (log.isEnabledFor(Level.INFO)) {
					log.info("Error occurred during existing project scan request: " + errMsg + ". Operation retry " + count);
				}
			}
			
			if ((runScanResult != null) && !runScanResult.isSuccesfullResponce()) {
				errMsg = runScanResult.getErrorMessage();
				if (log.isEnabledFor(Level.ERROR)) {
					log.error("Existing project scan request was unsuccessful.");
				}
				count++;
				if (log.isEnabledFor(Level.INFO)) {
					log.info("Existing project scan run request unsuccessful: " + runScanResult.getErrorMessage() + ". Operation retry " + count);
				}
			}
			
			if ((runScanResult == null || !runScanResult.isSuccesfullResponce()) && count < retriesNum) {
				try {
					Thread.sleep(getStatusInterval * 1000);
				}
				catch (InterruptedException ex) {
					// no-op
				}
			}
		}
		
		if ((runScanResult != null) && !runScanResult.isSuccesfullResponce()) {
			throw new Exception("Existing project scan request was unsuccessful. " + (errMsg == null ? "" : errMsg));
		}
		else
		if (runScanResult == null) {
			throw new Exception("Error occurred during existing project scan. " + errMsg);
		}
		
		if (log.isEnabledFor(Level.TRACE)) {
			log.trace("Existing project scan request response:" + runScanResult);
		}
		runId = runScanResult.getRunId();
	}
	
	private boolean packFolder(long maxZipSize) {
		File projectDir = new File(params.getLocationPath());
        if (!projectDir.exists()) {
            //if there is a semicolon separator, take the first path
            String[] paths = params.getLocationPath().split(";");
            if (paths != null && paths.length > 0){
                projectDir = new File(paths[0]);
            }
            if (projectDir.exists()) {
                params.setLocationPath(paths[0]);
            }
            else{
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Project directory [" + params.getLocationPath()
                            + "] does not exist.");
                }
                return false;
            }
        }

		if (!projectDir.isDirectory()) {
			if (log.isEnabledFor(Level.ERROR)) {
				log.error("Project path [" + params.getLocationPath()
						+ "] should point to a directory.");
			}
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
            zippedSourcesBytes =  zipper.zip(new File(params.getLocationPath()),excludePatterns, includeAllPatterns, maxZipSize,listener);

        } catch (Exception e)
        {
            log.trace(e);
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Error occurred during zipping source files. Error message: " + e.getMessage());
            }
            return false;
        }
        return true;
	}

    private String[] createExcludePatternsArray(){

        LinkedList<String> excludePatterns = new LinkedList<String>();
        try{
            String defaultExcludedFolders = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FOLDERS);
            for(String folder : StringUtils.split(defaultExcludedFolders,","))
            {
                String trimmedPattern = folder.trim();
                if (trimmedPattern != "")
                {
                    excludePatterns.add("**/"+trimmedPattern.replace('\\', '/')+"/**/*");
                }
            }

            String defaultExcludedFiles = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FILES);
            for(String file : StringUtils.split(defaultExcludedFiles, ","))
            {
                String trimmedPattern = file.trim();
                if (trimmedPattern != "")
                {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }

            if (params.hasExcludedFoldersParam())
            {
                for(String folder : params.getExcludedFolders())
                {
                    String trimmedPattern = folder.trim();
                    if (trimmedPattern != "")
                    {
                        excludePatterns.add("**/"+trimmedPattern.replace('\\', '/') +"/**/*");
                    }
                }
            }

            if (params.hasExcludedFilesParam())
            {
                for(String file : params.getExcludedFiles())
                {
                    String trimmedPattern = file.trim();
                    if (trimmedPattern != "")
                    {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                    }
                }
            }
        } catch (Exception e)
        {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Error occurred creation of exclude patterns");
            }
        }
        return excludePatterns.toArray(new String[]{});

    }

    @Override
	protected String getProjectName() {		
		return params.getFullProjName();
	}
}
