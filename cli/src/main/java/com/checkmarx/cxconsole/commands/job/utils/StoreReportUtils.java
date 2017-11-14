package com.checkmarx.cxconsole.commands.job.utils;

import com.checkmarx.clients.soap.sast.CxSoapSASTClient;
import com.checkmarx.clients.soap.sast.exceptions.CxSoapSASTClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 07/11/2017.
 */
public class StoreReportUtils {

    protected static Logger log = Logger.getLogger(LOG_NAME);

    private StoreReportUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void downloadAndStoreReport(String projectName, String fileName, String type, long scanId, CxSoapSASTClient cxSoapSASTClient, String sessionId, String srcPath) throws CLIJobException {
        type = type.toUpperCase();
        String folderPath = srcPath;
        String resultFilePath = "";
        if (folderPath == null || folderPath.isEmpty()) {
            folderPath = System.getProperty("user.dir") + File.separator + PathHandler.normalizePathString(projectName);
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        resultFilePath = PathHandler.initFilePath(projectName, fileName, "." + type.toLowerCase(), folderPath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(resultFilePath));) {
            log.info("Saving results to " + type + " file: [" + resultFilePath + "]");
            fileOutputStream.write(cxSoapSASTClient.getScanReport(sessionId, scanId, type));
        } catch (IOException e) {
            log.info("Error creating " + type + " results. I/O error. " + e.getMessage());
            log.trace("", e);
        } catch (CxSoapSASTClientException e) {
            log.error("Error getting scan report: " + e.getMessage());
            throw new CLIJobException("Error getting scan report: " + e.getMessage());
        }
    }
}
