package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.cxviewer.ws.WSMgr;
import com.checkmarx.cxviewer.ws.results.GetConfigurationsListResult;
import com.checkmarx.cxviewer.ws.results.GetPresetsListResult;
import com.checkmarx.cxviewer.ws.results.GetTeamsListResult;
import com.checkmarx.cxviewer.ws.results.LoginResult;
import com.checkmarx.login.rest.CxRestLoginClient;
import com.checkmarx.login.rest.exception.CxLoginClientException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

public class CxScanJob implements Callable<Integer> {

    protected Logger log;

    /**
     * Scan parameters
     */
    protected ScanParams params;

    /*
     * WebService manager used to connect and communicate server
     */
    protected WSMgr wsMgr;

    protected CxRestLoginClient cxRestLoginClient;
    // Current scan identifier
    protected String runId;
    // Current session ID
    protected String sessionId;
    // Current project Id
    protected long projectId;
    // Current scan Id
    protected long scanId;
    // Current result Id
    protected long resultId;

    private static String errorMsg;


    // Utility fields for accessing and saving results from anonymous  classes
    protected GetPresetsListResult presetsListResult;
    protected GetTeamsListResult teamsListResult;
    protected GetConfigurationsListResult configurationsListResult;

    public CxScanJob(ScanParams params) {
        this.params = params;
    }

    protected void login(URL wsdlLocation) throws Exception {

        RetriableOperation login = new RetriableOperation(log) {
            private URL wsdlLocation;

            @Override
            void operation() throws Exception {
                try {
                    wsMgr.connectWebService(wsdlLocation);
                } catch (Exception e) {
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("WS connection error", e);
                    }
                    error = "Cannot establish connection with the server.";
                    finished = true;
                    return;
                }

				/*GeneralResult result = wsMgr.checkVersion(CxClientType.CLI, ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_VERSION), Const.WS_API_DEFAULT_VER);
                if (!result.isSuccesfullResponce()) {
					error = result.getErrorMessage();
					if (log.isEnabledFor(Level.INFO)) {
						log.info("Version unsupported: " + error);
					}
					finished = true;
					return;
				}*/

                if (log.isEnabledFor(Level.INFO)) {
                    log.info("Logging into the Checkmarx service.");
                }

                // Login
                LoginResult r = null;
                sessionId = null;
                if (isWindows() && params.isSsoLoginUsed()) {
                    //SSO login
                    r = wsMgr.ssoLogin("", "");
                } else if (params.hasUserParam() && params.hasPasswordParam()) {
                    //Applicative login with user name and password
                    r = wsMgr.login(params.getUser(), params.getPassword());
                    if (r != null && r.getSessionId() != null && !r.getSessionId().isEmpty()) {
                        sessionId = r.getSessionId();
                    }
                } else if (params.hasTokenParam()) {
                    cxRestLoginClient = new CxRestLoginClient(params.getOriginHost(), params.getToken(), log);
                    try {
                        sessionId = cxRestLoginClient.tokenLogin().getSessionId();
                    } catch (CxLoginClientException e) {
                        error = "Unsuccessful login.\\n" + e.getMessage();
                        if (log.isEnabledFor(Level.TRACE)) {
                            log.trace(error);
                        }
                        finished = true;
                        return;
                    }
                }

                // 2 methods of login failed(username + password/token)
                if (sessionId == null) {
                    String message = "Unsuccessful login.";
                    if (r != null) {
                        message += ((r.getErrorMessage() != null && !r.getErrorMessage().isEmpty()) ? " Error message:" + r.getErrorMessage() : "Login or password might be incorrect.");
                    }
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace(message);
                    }
                    error = message;
                    finished = true;
                }
                finished = true;
            }

            @Override
            String getOperationName() {
                return "login";
            }

            public RetriableOperation setParam(URL wsdlLocation) {
                this.wsdlLocation = wsdlLocation;
                return this;
            }
        }.setParam(wsdlLocation);

        login.run();
        String error = login.getError();
        if (error != null) {
            errorMsg = error;
            throw new Exception(error);
        }
    }

    protected void storeXMLResults(String fileName, byte[] resultBytes) {
        File resFile = initFile(fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(resFile.getAbsolutePath());
            fOut.write(resultBytes);
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Saving xml results to file [" + resFile.getAbsolutePath() + "] failed");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", e);
            }
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }


    private File initFile(String fileName) {

        String folderPath = gerWorkDirectory();
        String resultFilePath = initFilePath(fileName, ".xml", folderPath);
        return new File(resultFilePath);
    }

    public String gerWorkDirectory() {
        String folderPath = params.getSrcPath();

        if (folderPath == null || folderPath.isEmpty()) {
            //in case of ScanProject command
            String prjName = normalizePathString(params.getProjName());
            /*if (prjName.contains("\\")) {
				prjName = prjName.replace('\\', '_');
			}
			if (prjName.contains("/")) {
				prjName = prjName.replace('/', '_');
			}*/
            folderPath = System.getProperty("user.dir") + File.separator + /*params.getProjName()*/prjName;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            //folderPath = "";
        }
        return folderPath;
    }

    protected String normalizePathString(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_DEF_PROJECT_NAME);
        }

        String normalPathName = "";
        normalPathName = projectName.replace("\\", "_");
        normalPathName = normalPathName.replace("/", "_");
        normalPathName = normalPathName.replace(":", "_");
        normalPathName = normalPathName.replace("?", "_");
        normalPathName = normalPathName.replace("*", "_");
        normalPathName = normalPathName.replace("\"", "_");
        normalPathName = normalPathName.replace("<", "_");
        normalPathName = normalPathName.replace(">", "_");
        normalPathName = normalPathName.replace("|", "_");
        return normalPathName;
    }

    public String initFilePath(String fileName, String extention, String parentDirectoryPath) {
        String resultFilePath = "";

        File resultFile = new File(fileName);
        fileName = resultFile.getPath();
        if (resultFile.isAbsolute()) {
            // Path is absolute
            if (fileName.endsWith(File.separator)) {
                //Directory path
                File resDirs = new File(fileName);
                if (!resDirs.exists()) {
                    resDirs.mkdirs();
                }
                resultFilePath = fileName + File.separator + normalizePathString(getProjectName()) + extention;
            } else {
                // File path
                if (fileName.contains(File.separator)) {
                    String dirPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
                    File xmlResDirs = new File(dirPath);
                    if (!xmlResDirs.exists()) {
                        xmlResDirs.mkdirs();
                    } else if (xmlResDirs.isFile()) {
                        //cannot create directory - file already exists
                        if (log.isEnabledFor(Level.ERROR)) {
                            log.error("Unable to create directory hierarchy [" + xmlResDirs.getAbsolutePath() + "] for storing results: file with same name already exists.");
                        }
                    }
                }
                resultFilePath = fileName;
            }
        } else {
            // Path is not absolute
            if (!fileName.toLowerCase().endsWith(/*File.separator*/extention.toLowerCase())) {
                //Directory path
                String dirPath = parentDirectoryPath + File.separator + fileName;
                File resDirs = new File(dirPath);
                if (!resDirs.exists()) {
                    resDirs.mkdirs();
                }
                resultFilePath = dirPath + File.separator + normalizePathString(getProjectName()) + extention;
            } else {
                //File path
                if (fileName.contains(File.separator)) {
                    String dirPath = parentDirectoryPath + File.separator + fileName.substring(0, fileName.lastIndexOf(File.separator));
                    File xmlResDirs = new File(dirPath);
                    if (!xmlResDirs.exists()) {
                        xmlResDirs.mkdirs();
                    } else if (xmlResDirs.isFile()) {
                        //cannot create directory - file already exists
                        if (log.isEnabledFor(Level.ERROR)) {
                            log.error("Unable to create directory hierarchy [" + xmlResDirs.getAbsolutePath() + "] for storing results: file with same name already exists.");
                        }
                    }
                }

                resultFilePath = parentDirectoryPath + File.separator + fileName;
            }
        }

        return resultFilePath;
    }

    protected void downloadAndStoreReport(String fileName, String type) throws Exception {
        type = type.toUpperCase();
        FileOutputStream fileOutputStream = null;
        try {
            String folderPath = params.getSrcPath();
            String resultFilePath = "";
            if (folderPath == null || folderPath.isEmpty()) {
                folderPath = System.getProperty("user.dir") + File.separator + normalizePathString(params.getProjName());
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                //folderPath = "";
            }

            resultFilePath = initFilePath(fileName, "." + type.toLowerCase(), folderPath);

            if (log.isEnabledFor(Level.INFO)) {
                log.info("Saving " + type + " results to file [" + resultFilePath + "]");
            }
            //File resultsFile;
            fileOutputStream = new FileOutputStream(new File(resultFilePath));
            fileOutputStream.write(wsMgr.getScanReport(sessionId, scanId, type));
        } catch (FileNotFoundException e) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Error creating " + type + " results URL. Specified file is whether directory, or other file error occurred.");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", e);
            }
        } catch (IOException e) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Error creating " + type + " results URL. I/O error.");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", e);
            }
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    protected HttpURLConnection connectToURL(String url) {
        URL connectUrl;
        try {
            connectUrl = new URL(url);
        } catch (MalformedURLException ex) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Retrieved invalid PDF results URL.");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", ex);
            }
            return null;
        }

        HttpURLConnection request;
        try {
            request = (HttpURLConnection) connectUrl.openConnection();
        } catch (IOException ex) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Error retrieving PDF results URL. Failed to open connection");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", ex);
            }
            return null;
        }

        try {
            request.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            request.connect();
        } catch (IOException ex) {
            if (log.isEnabledFor(Level.INFO)) {
                log.info("Error retieving PDF results URL. Failed to connect");
            }
            if (log.isEnabledFor(Level.TRACE)) {
                log.trace("", ex);
            }
            return null;
        }

        return request;
    }

    protected boolean isProjectDirectoryValid() {
        File projectDir = new File(params.getLocationPath());
        if (!projectDir.exists()) {
            //if there is a semicolon separator, take the first path
            String[] paths = params.getLocationPath().split(";");
            if (paths != null && paths.length > 0) {
                projectDir = new File(paths[0]);
            }
            if (projectDir.exists()) {
                params.setLocationPath(paths[0]);
            } else {
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

        return true;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    /*
     * Generates project name according to folder name
     */
    protected String getProjectName() {
        String projPath = params.getSrcPath();
        String projectName;
        if (params.getFolderProjName() != null && !params.getFolderProjName().isEmpty()) {
            projectName = params.getFolderProjName();
        } else {
            projectName = projPath.substring(projPath.lastIndexOf(File.separator) + 1, projPath.length());
        }

        return projectName;
    }

    abstract class RetriableOperation {
        protected boolean finished = false;
        protected Logger log;
        protected String error;

        public RetriableOperation(Logger log) {
            this.log = log;
        }

        public void run() throws Exception {
            int retries = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
            int count = 0;
            while (!finished) {
                try {
                    operation();
                } catch (Exception e) {
                    if (count >= retries) {
                        throw e;
                    }
                    count++;
                    if (log.isEnabledFor(Level.TRACE)) {
                        log.trace("Error occurred during retiable operation", e);
                    }
                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Error occurred during " + getOperationName() + ". Operation retry " + count);
                    }
                }
            }
        }

        abstract void operation() throws Exception;

        abstract String getOperationName();

        public String getError() {
            return error;
        }
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }

    public static boolean isWindows() {
        boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
        return isWindows;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
