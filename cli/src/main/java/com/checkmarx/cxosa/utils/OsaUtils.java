package com.checkmarx.cxosa.utils;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Galn on 06/04/2017.
 */
public abstract class OsaUtils {
    private static int numOfZippedFiles = 0;
    private static Logger log;
    private static final String TEMP_FILE_NAME_TO_ZIP = "CxZippedSource";

    public static File zipWorkspaceFolder(String fileExcluded, String folderExclusions, long maxZipSizeInBytes, String path, final Logger log)
            throws InterruptedException, IOException {

        String combinedFilterPattern = generatePattern(folderExclusions, fileExcluded);

        ZipListener zipListener = new ZipListener() {
            public void updateProgress(String fileName, long size) {
                numOfZippedFiles++;
                log.debug("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
            }
        };

        File tempFile = File.createTempFile(TEMP_FILE_NAME_TO_ZIP, ".bin");
        OutputStream fileOutputStream = new FileOutputStream(tempFile);

        File folder = new File(path);
        try {
            new Zipper().zip(folder, combinedFilterPattern, fileOutputStream, maxZipSizeInBytes, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            tempFile.delete();
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipSizeInBytes));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        if (log.isEnabledFor(Level.DEBUG)) {
            log.debug("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                    FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
            log.debug("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");
        }
        return tempFile;
    }


    public static String composeProjectOSASummaryLink(String url, long projectId) {
        return String.format(url + "/CxWebClient/portal#/projectState/%s/OSA", projectId);
    }


    private static String generatePattern(String folderExclusions, String filterPattern) throws IOException, InterruptedException {

        String excludeFoldersPattern = processExcludeFolders(folderExclusions);
        String excludeFilesPattern = processExcludeFiles(filterPattern);

        if (!StringUtils.isEmpty(excludeFilesPattern) && !StringUtils.isEmpty(excludeFoldersPattern)) {
            return excludeFilesPattern + "," + excludeFoldersPattern;
        } else {
            return excludeFilesPattern + excludeFoldersPattern;
        }
    }

    private static String processExcludeFolders(String folderExclusions) {
        if (StringUtils.isEmpty(folderExclusions)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        folderExclusions = folderExclusions.replace(",,", ",");
        String[] patterns = StringUtils.split(folderExclusions, ",\n");

        for (String p : patterns) {
            p = p.trim();
            if (p.length() > 0 && !p.equals("null")) {
                result.append("!**/");
                result.append(p);
                result.append("/**/*, ");
            }
        }
        log.debug("Exclude folders converted to: '" + result.toString() + "'");
        return result.toString();
    }

    private static String processExcludeFiles(String filesExclusions) {
        if (StringUtils.isEmpty(filesExclusions)) {
            return "";
        }
        filesExclusions.replace(",,", ",");
        StringBuilder result = new StringBuilder();
        filesExclusions = filesExclusions.replace(",,", ",");
        String[] patterns = StringUtils.split(filesExclusions, ",\n");
        for (String p : patterns) {
            p = p.trim();
            if (p.length() > 0 && !p.equals("null")) {
                result.append("!**/");
                result.append(p);
                result.append(", ");
            }
        }
        log.debug("Exclude files converted to: '" + result.toString() + "'");
        return result.toString();
    }

    public static void deleteTempFiles() {

        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            CxFileChecker.deleteFile(tempDir, TEMP_FILE_NAME_TO_ZIP, log);
        } catch (Exception e) {
            log.error("Failed to delete temp files: " + e.getMessage());
        }

    }
    public static void setLogger(Logger log) {
        OsaUtils.log = log;
    }
}
