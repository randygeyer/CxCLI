package com.checkmarx.cxconsole.cxosa.utils;

import com.checkmarx.clients.rest.osa.constant.FileNameAndShaOneForOsaScan;
import com.checkmarx.cxconsole.cxosa.utils.Exception.OSAUtilException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;
import static com.checkmarx.cxconsole.utils.ConfigMgr.*;

public class OSAUtil {

    private static Logger log = Logger.getLogger(LOG_NAME);

    private static List<String> exclusions;
    private static List<String> inclusions;
    private static List<String> extractableIncluded;

    private enum ListType {EXCLUSION, INCLUSION, EXTRACTABLE_INCLUDE}

    private static final String TEMP_FOLDER_FOR_FILES_EXTRACTION = "CxOSATempExtractedSource";

    private OSAUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String composeProjectOSASummaryLink(String url, long projectId) {
        return String.format("%s/CxWebClient/portal#/projectState/%s/OSA", url, projectId);
    }

    /**
     * scan for OSA compatible files and returns a list of sha1 + filename
     * the scan also recursively extracts archive files and scan its contents
     *
     * @return - list of sha1 + filename from the baseDir and within archives
     * @throws OSAUtilException - expected errors: create temp dir, list files
     *                          error handling for fail: extract archive / calculate sha1 / delete temp dir -  warning is logged
     */
    public static List<FileNameAndShaOneForOsaScan> scanFiles(String[] baseDirectories, String[] osaIncludedFiles,
                                                              String[] osaExcludedFiles, String[] osaExtractableIncludeFiles,
                                                              int unzipDepth) throws OSAUtilException {
        exclusions = setUpExtensionsList(osaExcludedFiles, ListType.EXCLUSION);
        inclusions = setUpExtensionsList(osaIncludedFiles, ListType.INCLUSION);
        extractableIncluded = setUpExtensionsList(osaExtractableIncludeFiles, ListType.EXTRACTABLE_INCLUDE);
        File baseTempDir = new File(TEMP_FOLDER_FOR_FILES_EXTRACTION);
        File extractTempDir = null;
        ArrayList<FileNameAndShaOneForOsaScan> listToScan = new ArrayList<>();
        for (String baseDirectory : baseDirectories) {
            try {
                File baseDir = new File(baseDirectory);
                extractTempDir = createExtractTempDir(baseTempDir);
                listToScan.addAll(scanFilesRecursive(baseDir, extractTempDir, "", unzipDepth));
            } catch (Exception e) {
                log.trace("Failed to scan directory for OSA files: " + e.getMessage());
                throw new OSAUtilException("Failed to scan directory for OSA files: " + e.getMessage(), e);
            } finally {
                try {
                    if (extractTempDir != null) {
                        FileUtils.deleteDirectory(extractTempDir);
                    }
                } catch (Exception e) {
                    log.trace("Failed to delete temp directory: [" + extractTempDir.getAbsolutePath() + "]");
                }
            }
        }
        return listToScan;
    }

    private static List<String> setUpExtensionsList(String[] extensionStringList, ListType listType) {
        String[] strArray = new String[0];
        String regex = "\\s*,\\s*";
        if (extensionStringList != null && extensionStringList.length > 0) {
            return new ArrayList<>(Arrays.asList(extensionStringList));
        } else if (listType.equals(ListType.EXCLUSION)) {
            strArray = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_EXCLUDED_FILES)).split(regex);
        } else if (listType.equals(ListType.INCLUSION)) {
            strArray = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_INCLUDED_FILES)).split(regex);
        } else if (listType.equals(ListType.EXTRACTABLE_INCLUDE)) {
            strArray = (ConfigMgr.getCfgMgr().getProperty(KEY_OSA_EXTRACTABLE_INCLUDE_FILES)).split(regex);
        }
        return new ArrayList<>(Arrays.asList(strArray));
    }


    /**
     * recursive function used by the wrapper scanFiles()
     *
     * @param virtualPath - base path used to filter against, as if the extracted files were extracted to baseDir/extractableFileName.extension
     * @param tempDir     - temporary directory to extract files to
     */
    private static List<FileNameAndShaOneForOsaScan> scanFilesRecursive(File baseDir, File tempDir, String virtualPath, int depth) {
        List<FileNameAndShaOneForOsaScan> ret = new ArrayList<>();

        if (depth < 0) {
            return ret;
        }

        List<File> files = getFiles(baseDir);

        for (File file : files) {
            String virtualFullPath = virtualPath + getRelativePath(baseDir, file);
            boolean candidate = isCandidate(virtualFullPath);
            if (candidate) {
                addSha1(file, ret);
            }

            if (candidate && isExtractable(file.getName())) {
                //this directory should be created by the extractToTempDir() if there is any files to extract
                File nestedTempDir = new File(tempDir.getAbsolutePath() + "/" + file.getName() + "_extracted");

                boolean extracted = extractToTempDir(nestedTempDir, file, virtualFullPath);
                if (!extracted) {
                    continue;
                }

                List<FileNameAndShaOneForOsaScan> tmp = scanFilesRecursive(nestedTempDir, nestedTempDir, virtualFullPath, depth - 1);
                ret.addAll(tmp);
            }
        }

        return ret;
    }

    //list file compatible to OSA, and the files that are extractable
    private static List<File> getFiles(File baseDir) {
        String[] compatibleAndExtractableFiles = ArrayUtils.addAll(inclusions.toArray(new String[1]), extractableIncluded.toArray(new String[1]));
        for (int i = 0; i < compatibleAndExtractableFiles.length; i++) {
            compatibleAndExtractableFiles[i] = compatibleAndExtractableFiles[i].replace("*.", "");
        }
        return new ArrayList<>(FileUtils.listFiles(baseDir, compatibleAndExtractableFiles, true));
    }

    //extract the OSA compatible files and archives to temporary directory. also filters by includes/excludes

    private static boolean extractToTempDir(File nestedTempDir, File zip, String virtualPath) {
        try {
            ZipFile zipFile = new ZipFile(zip);
            List fileHeaders = zipFile.getFileHeaders();
            List<FileHeader> filtered = new ArrayList<>();

            //first, filter the relevant files
            for (Object fileHeader1 : fileHeaders) {
                FileHeader fileHeader = (FileHeader) fileHeader1;
                String fileName = fileHeader.getFileName();
                if (!fileHeader.isDirectory() && (isExtractable(fileName) || isCandidate(virtualPath + "/" + fileName, inclusions.toArray(new String[1])))) {
                    filtered.add(fileHeader);
                }
            }

            //now, extract the relevant files (if any):
            if (filtered.isEmpty()) {
                return false;
            }

            //create the temp dir to extract to
            nestedTempDir.mkdirs();
            if (!nestedTempDir.exists()) {
                log.trace("Failed to extract archive: [" + zip.getAbsolutePath() + "]: failed to create temp dir: [" + nestedTempDir.getAbsolutePath() + "]");
                return false;
            }

            //extract
            for (FileHeader fileHeader : filtered) {
                try {
                    zipFile.extractFile(fileHeader, nestedTempDir.getAbsolutePath());
                } catch (ZipException e) {
                    log.trace("Failed to extract archive: [" + zip.getAbsolutePath() + "]: " + e.getMessage(), e);
                }
            }

        } catch (ZipException e) {
            log.trace("Failed to extract archive: [" + zip.getAbsolutePath() + "]: " + e.getMessage(), e);
            return false;
        }

        return nestedTempDir.exists();
    }

    private static boolean isExtractable(String fileName) {
        return FilenameUtils.isExtension(fileName, extractableIncluded);
    }

    /**
     * flow:
     * 1. no include, no exclude -> don't filter. return true
     * 2. no include, yes exclude -> return isExcludeMatch(file) ? false : true
     * 3. yes include, no exclude -> return isIncludeMatch(file) ?  true : false
     * <p>
     * 4. yes include, yes exclude ->
     * if(isExcludeMatch(file)) {
     * return false
     * }
     * <p>
     * return isIncludeMatch(file))
     *
     * @param relativePath
     * @return
     */
    private static boolean isCandidate(String relativePath) {
        relativePath = relativePath.replaceAll("\\\\", "/");
        boolean isMatch = true;

        for (String exclusion : exclusions) {
            if (SelectorUtils.matchPath(exclusion, relativePath, false)) {
                return false;
            }
        }

        if (!inclusions.isEmpty()) {
            for (String inclusion : inclusions) {
                if (SelectorUtils.matchPath(inclusion, relativePath, false)) {
                    return true;
                }
            }
            isMatch = false;
        }

        return isMatch;
    }

    //matched by supported extensions and include/exclude filters
    private static boolean isCandidate(String relativePath, String[] supportedExtensions) {
        return FilenameUtils.isExtension(relativePath, supportedExtensions) && isCandidate(relativePath);
    }

    //calculate sha1 of file, and add the filename + sha1 to the output parameter ret
    private static void addSha1(File file, List<FileNameAndShaOneForOsaScan> ret) {
        try (BOMInputStream is = new BOMInputStream(new FileInputStream(file))) {
            String sha1 = DigestUtils.sha1Hex(is);
            ret.add(new FileNameAndShaOneForOsaScan(sha1, file.getName()));
            log.trace("The file: " + file.getName() + " with Sha1: " + sha1 + " was added to Analysis");
        } catch (IOException e) {
            log.warn("Failed to calculate sha1 for file: [" + file.getAbsolutePath() + "]. exception message: " + e.getMessage());
        }
    }

    private static String getRelativePath(File baseDir, File file) {
        Path pathAbsolute = file.toPath();
        Path pathBase = baseDir.toPath();
        return "/" + pathBase.relativize(pathAbsolute).toString();
    }

    private static File createExtractTempDir(File tempDir) throws OSAUtilException {

        File extractTempDir = new File(tempDir.getAbsolutePath() + "/CxOSA_extract");

        extractTempDir.mkdirs();
        if (!extractTempDir.exists()) {
            throw new OSAUtilException("Failed to create directory [" + extractTempDir.getAbsolutePath() + "]");
        }

        return extractTempDir;
    }

}