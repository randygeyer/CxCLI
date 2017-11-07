package com.checkmarx.cxosa.utils;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galn on 06/04/2017.
 */
public abstract class OsaUtils {

    private static int numOfZippedFiles = 0;
    private static Logger log;
    private static final String TEMP_FILE_NAME_TO_ZIP = "CxZippedSource";
    private static final String TEMP_COPIED_FOLDER = "CxTempSource";

    public static File zipWorkspaceFolder(String[] fileExcluded, String[] folderExclusions, String[] fileIncluded, long maxZipSizeInBytes, String[] path, final Logger log)
            throws InterruptedException, IOException {

        String[] combinedExcludePattern = generateExcludePattern(folderExclusions, fileExcluded);
        String[] includeFilesPattern = processIncludedFiles(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_OSA_INCLUDED_FILES), fileIncluded);

        ZipListener zipListener = new ZipListener() {
            public void updateProgress(String fileName, long size) {
                numOfZippedFiles++;
                log.debug("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
            }
        };


        File tempFile = File.createTempFile(TEMP_FILE_NAME_TO_ZIP, ".bin");
        File tmpFolder = createTempDirectory();
        for (String dirPath : path) {
            File dir = new File(dirPath);
            if (dir.isDirectory()) {
                FileUtils.copyDirectory(dir, tmpFolder);
            }
        }
        try(OutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            new Zipper().zip(tmpFolder, combinedExcludePattern, includeFilesPattern, fileOutputStream, maxZipSizeInBytes, zipListener);
        } catch (Zipper.MaxZipSizeReached e) {
            tempFile.delete();
            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipSizeInBytes));
        } catch (Zipper.NoFilesToZip e) {
            throw new IOException("No files to zip");
        }

        log.debug("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
                FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
        log.debug("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");

        try {
            FileUtils.deleteDirectory(tmpFolder);
        } catch (IOException e) {
            log.warn("Warning: failed to delete temporary folder: " + tmpFolder.getAbsolutePath());
        }

        return tempFile;
    }


    public static String composeProjectOSASummaryLink(String url, long projectId) {
        return String.format(url + "/CxWebClient/portal#/projectState/%s/OSA", projectId);
    }

    private static File createTempDirectory()
            throws IOException {
        final File temp;

        temp = File.createTempFile(TEMP_COPIED_FOLDER, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    private static String[] generateExcludePattern(String[] folderExclusions, String[] filterPattern) {

        String[] excludeFoldersPattern = processExcludeFolders(folderExclusions);
        String[] excludeFilesPattern = processPatternFiles(filterPattern);

        return ArrayUtils.addAll(excludeFilesPattern, excludeFoldersPattern);
    }

    private static String[] processExcludeFolders(String[] folderExclusions) {
        if (folderExclusions == null) {
            return folderExclusions;
        }

        List<String> result = new ArrayList<>();


        for (String p : folderExclusions) {
            p = p.trim();
            if (p.length() > 0 && !p.equals("null")) {
                result.add("!**/" + p + "/**/*, ");
            }
        }
        log.debug("Exclude folders converted to: '" + folderExclusions.toString() + "'");
        return folderExclusions;
    }

    private static String[] processPatternFiles(String[] filesExclusions) {

        if (filesExclusions == null) {
            return filesExclusions;
        }
        List<String> result = new ArrayList<>();


        for (String p : filesExclusions) {
            p = p.trim();
            if (p.length() > 0 && !p.equals("null")) {
                result.add("!**/" + p.replace('\\', '/'));
            }
        }

        log.debug("Exclude files converted to: '" + filesExclusions.toString() + "'");
        return result.toArray(new String[0]);
    }

    private static String[] processIncludedFiles(String configIncluded, String[] fileIncluded) {
        List<String> defIncludedFiles = new ArrayList<>();

        if (configIncluded != null) {
            for (String file : StringUtils.split(configIncluded, ",")) {
                String trimmedPattern = file.trim();
                if (trimmedPattern != "") {
                    defIncludedFiles.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }
        }

        if (fileIncluded != null) {
            for (String file : fileIncluded) {
                String trimmedPattern = file.trim();
                if (trimmedPattern != "") {
                    defIncludedFiles.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }
        }

        return defIncludedFiles.toArray(new String[0]);
    }


    public static void deleteTempFiles() {

        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            CxFileUtils.deleteTempPath(tempDir, TEMP_FILE_NAME_TO_ZIP, log);
            CxFileUtils.deleteTempPath(tempDir, TEMP_COPIED_FOLDER, log);
        } catch (Exception e) {
            log.error("Failed to delete temp files: " + e.getMessage());
        }

    }

    public static void setLogger(Logger log) {
        OsaUtils.log = log;
    }
}
