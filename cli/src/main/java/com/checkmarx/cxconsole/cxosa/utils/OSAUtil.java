package com.checkmarx.cxconsole.cxosa.utils;

import com.checkmarx.clients.rest.osa.constant.FileNameAndShaOneForOsaScan;
import com.checkmarx.cxconsole.cxosa.utils.Exception.OSAUtilException;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

public class OSAUtil {

    private OSAUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String TEMP_FOLDER_FOR_FILES_EXTRACTION = "CxOSATempExtractedSource";

    private static Logger log = Logger.getLogger(LOG_NAME);
    private static String[] osaIncludedFiles;
    private static String[] osaExcludedFiles;
    private static String[] osaExcludedFolders;
    private static String[] osaExtractableIncludeFiles;
    private static final String[] EXTRACTABLE_EXTENSIONS = {"jar", "war", "ear", "sca", "gem", "whl", "egg", "tar", "tar.gz", "tgz", "zip", "rar"};
    private static final String[] WHITE_SOURCE_SUPPORTED_EXTENSIONS = {"jar", "war", "ear", "aar", "dll", "exe", "msi", "nupkg", "egg", "whl", "tar.gz", "gem", "deb", "udeb",
            "dmg", "drpm", "rpm", "pkg.tar.xz", "swf", "swc", "air", "apk", "zip", "gzip", "tar.bz2", "tgz", "c", "cc", "cp", "cpp", "css", "c++", "h", "hh", "hpp",
            "hxx", "h++", "m", "mm", "pch", "c#", "cs", "csharp", "go", "goc", "js", "plx", "pm", "ph", "cgi", "fcgi", "psgi", "al", "perl", "t", "p6m", "p6l", "nqp,6pl", "6pm",
            "p6", "php", "py", "rb", "swift", "clj", "cljx", "cljs", "cljc"};
    private static final String[] ZIP_EXTENSIONS = {"zip", "jar", "war", "ear", "egg", "whl", "sca"};
    private static final String[] TAR_EXTENSIONS = {"tar", "gem"};
    private static final String[] GZ_EXTENSIONS = {"tgz", "tar.gz"};
    private static final String[] RAR_EXTENSIONS = {"rar"};
    private static final String[] TAR_AND_GZ_EXTENSIONS = ArrayUtils.addAll(TAR_EXTENSIONS, GZ_EXTENSIONS);
    private static final String[] ALL_EXTENSIONS = ArrayUtils.addAll(WHITE_SOURCE_SUPPORTED_EXTENSIONS, EXTRACTABLE_EXTENSIONS);

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
                                                              String[] osaExcludedFiles, String[] osaExcludedFolder, String[] osaExtractableIncludeFiles,
                                                              int unzipDepth) throws OSAUtilException {
        File baseTempDir = new File(TEMP_FOLDER_FOR_FILES_EXTRACTION);
        OSAUtil.osaIncludedFiles = osaIncludedFiles;
        OSAUtil.osaExcludedFiles = osaExcludedFiles;
        OSAUtil.osaExcludedFolders = osaExcludedFolder;
        OSAUtil.osaExtractableIncludeFiles = osaExtractableIncludeFiles;

        File extractTempDir = null;
        ArrayList<FileNameAndShaOneForOsaScan> listToScan = new ArrayList<>();
        for (String baseDirectory : baseDirectories) {
            try {
                if (osaExcludedFolder.length > 0 && isPathExcluded(baseDirectory)) {
                    log.trace("The directory: " + baseDirectory + " is excluded from OSA analysis");
                    continue;
                }
                File baseDir = new File(baseDirectory);
                extractTempDir = createExtractTempDir(baseTempDir);
                listToScan.addAll(scanFilesRecursive(baseDir, extractTempDir, "", unzipDepth));
            } catch (Exception e) {
                log.trace("Failed to scan directory for OSA files: " + e.getMessage());
                throw new OSAUtilException("Failed to scan directory for OSA files: " + e.getMessage(), e);
            } finally {
                try {
                    if (extractTempDir != null) {
                        FileUtils.deleteDirectory(baseTempDir);
                    }
                } catch (Exception e) {
                    log.trace("Failed to delete temp directory: [" + extractTempDir.getAbsolutePath() + "]");
                }
            }
        }
        log.trace("List of files sent to WhiteSource: " + listToScan.toString());
        return listToScan;
    }

    private static boolean isPathExcluded(String baseDirectory) {
        for (String excludeFolderName : osaExcludedFolders) {
            if ((new File(baseDirectory)).isDirectory() && baseDirectory.contains(excludeFolderName)) {
                return true;
            }
        }
        return false;
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
            if (isCandidateForSha1(virtualFullPath)) {
                addSha1(file, ret);
            }

            if (isCandidateForExtract(virtualFullPath)) {
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
        return new ArrayList<>(FileUtils.listFiles(baseDir, ALL_EXTENSIONS, true));
    }

    //extract the OSA compatible files and archives to temporary directory. also filters by includes/excludes
    private static boolean extractToTempDir(File nestedTempDir, File archive, String virtualPath) {
        //uses zip4j
        if (isExtension(archive.getName(), ZIP_EXTENSIONS)) {
            log.trace(archive.getName() + " will be extracted (Zip or related)");
            return extractZipToTempDir(nestedTempDir, archive, virtualPath);
        }

        //uses common-compression
        if (isExtension(archive.getName(), TAR_AND_GZ_EXTENSIONS)) {
            log.trace(archive.getName() + " will be extracted (Tar or related)");
            return extractTarOrGZToTempDir(nestedTempDir, archive, virtualPath);
        }

        //uses junrar
        if (isExtension(archive.getName(), RAR_EXTENSIONS)) {
            log.trace(archive.getName() + " will be extracted (Rar or related)");
            return extractRarToTempDir(nestedTempDir, archive, virtualPath);
        }

        return nestedTempDir.exists();
    }

    private static boolean isExtension(String filename, String[] extensions) {
        for (String extension : extensions) {
            if (filename.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    private static boolean extractZipToTempDir(File nestedTempDir, File zip, String virtualPath) {
        try {
            ZipFile zipFile = new ZipFile(zip);
            List fileHeaders = zipFile.getFileHeaders();
            List<FileHeader> filtered = new ArrayList<>();

            //first, filter the relevant files
            for (Object fileHeader1 : fileHeaders) {
                FileHeader fileHeader = (FileHeader) fileHeader1;
                String fileName = fileHeader.getFileName();
                if (!fileHeader.isDirectory() && (isCandidateForSha1(virtualPath + "/" + fileName) || isCandidateForExtract(virtualPath + "/" + fileName))) {
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
                log.info("Failed to extract archive: [" + zip.getAbsolutePath() + "]: failed to create temp dir: [" + nestedTempDir.getAbsolutePath() + "]");
                return false;
            }

            //extract
            for (FileHeader fileHeader : filtered) {
                try {
                    zipFile.extractFile(fileHeader, nestedTempDir.getAbsolutePath());
                    log.trace("Extracting file: " + fileHeader.getFileName());
                } catch (ZipException e) {
                    String message = e.getMessage().contains("Negative seek offset") ? "Missing or empty archive file" : e.getMessage();
                    log.info("Failed to extract archive: [" + zip.getAbsolutePath() + "]: " + message, e);
                }
            }
        } catch (ZipException e) {
            String message = e.getMessage().contains("Negative seek offset") ? "Missing or empty archive file" : e.getMessage();
            log.info("Failed to extract archive: [" + zip.getAbsolutePath() + "]: " + message, e);
            return false;
        }

        return nestedTempDir.exists();
    }

    private static boolean extractTarOrGZToTempDir(File nestedTempDir, File tar, String virtualPath) {
        ArchiveInputStream tarInputStream = null;
        try {
            tarInputStream = getArchiveInputStream(tar);
            ArchiveEntry entry;
            log.trace("Extracting file: " + tar.getName());
            while ((entry = tarInputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (!entry.isDirectory() && (isCandidateForSha1(virtualPath + "/" + fileName) || isCandidateForExtract(virtualPath + "/" + fileName))) {
                    byte[] buffer = new byte[(int) entry.getSize()];
                    tarInputStream.read(buffer, 0, buffer.length);
                    FileUtils.writeByteArrayToFile(new File(nestedTempDir + "/" + fileName), buffer);
                }
            }
        } catch (IOException e) {
            log.info("Failed to extract archive: [" + tar.getAbsolutePath() + "]: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(tarInputStream);
        }

        return nestedTempDir.exists();
    }

    private static boolean isCandidateForSha1(String relativePath) {
        return isCandidate(relativePath, WHITE_SOURCE_SUPPORTED_EXTENSIONS, osaIncludedFiles, osaExcludedFiles);
    }

    private static boolean isCandidateForExtract(String relativePath) {
        return isCandidate(relativePath, EXTRACTABLE_EXTENSIONS, osaExtractableIncludeFiles, null);
    }

    private static ArchiveInputStream getArchiveInputStream(File archive) throws IOException {
        if (isExtension(archive.getName(), TAR_EXTENSIONS)) {
            return new TarArchiveInputStream(new FileInputStream(archive));
        }

        if (isExtension(archive.getName(), GZ_EXTENSIONS)) {
            return new TarArchiveInputStream(
                    new GzipCompressorInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(archive))));
        }

        return new ZipArchiveInputStream(new FileInputStream(archive));
    }

    private static boolean extractRarToTempDir(File nestedTempDir, File rar, String virtualPath) {
        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rar));

            List<com.github.junrar.rarfile.FileHeader> fileHeaders = archive.getFileHeaders();
            List<com.github.junrar.rarfile.FileHeader> filtered = new ArrayList<>();

            for (com.github.junrar.rarfile.FileHeader fileHeader : fileHeaders) {
                String fileName = fileHeader.getFileNameString();
                if (!fileHeader.isDirectory() && (isCandidateForSha1(virtualPath + "/" + fileName) || isCandidateForExtract(virtualPath + "/" + fileName))) {
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
                log.info("Failed to extract archive: [" + rar.getAbsolutePath() + "]: failed to create temp dir: [" + nestedTempDir.getAbsolutePath() + "]");
                return false;
            }

            //extract
            for (com.github.junrar.rarfile.FileHeader fileHeader : filtered) {
                try {
                    InputStream headerInputStream = archive.getInputStream(fileHeader);
                    File destinationFile = new File(nestedTempDir + "/" + fileHeader.getFileNameString());
                    FileUtils.copyInputStreamToFile(headerInputStream, destinationFile);
                    log.trace("Extracting file: " + fileHeader.getFileNameString());
                } catch (RarException e) {
                    log.info("Failed to extract archive: [" + rar.getAbsolutePath() + "]: " + e.getMessage(), e);
                }
            }

        } catch (RarException | IOException e) {
            log.info("Failed to extract archive: [" + rar.getAbsolutePath() + "]: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(archive);
        }

        return nestedTempDir.exists();
    }

    /**
     * flow:
     * <p>
     * 0. no match for extension -> return false
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
    private static boolean isCandidate(String relativePath, String[] extensions, String[] inclusions, String[] exclusions) {
        relativePath = relativePath.replaceAll("\\\\", "/");
        boolean isMatch = true;

        if(!isExtension(relativePath, extensions)) {
            return false;
        }

        if (exclusions != null) {
            for (String exclusion : exclusions) {
                if (SelectorUtils.matchPath("*." + exclusion, relativePath, false)) {
                    log.trace("The file: " + relativePath + " has excluded extension or is excluded from OSA analysis");
                    return false;
                }
            }
        }

        if (inclusions.length > 0) {
            for (String inclusion : inclusions) {
                if (SelectorUtils.matchPath("*." + inclusion, relativePath, false)) {
                    return true;
                }
            }
            isMatch = false;
        }

        return isMatch;
    }

    //calculate sha1 of file, and add the filename + sha1 to the output parameter ret
    private static void addSha1(File file, List<FileNameAndShaOneForOsaScan> ret) {
        try (BOMInputStream is = new BOMInputStream(new FileInputStream(file))) {
            String sha1 = DigestUtils.sha1Hex(is);
            ret.add(new FileNameAndShaOneForOsaScan(sha1, file.getName()));
            log.trace("The file: " + file.getName() + " with Sha1: " + sha1 + " was added to analysis");
        } catch (IOException e) {
            log.trace("Failed to calculate sha1 for file: [" + file.getAbsolutePath() + "]. exception message: " + e.getMessage());
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