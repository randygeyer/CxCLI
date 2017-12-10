package com.checkmarx.cxconsole.cxosa.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by Galn on 06/04/2017.
 */
public abstract class OsaUtils {

    private static Logger log = Logger.getLogger(LOG_NAME);

//    private static int numOfZippedFiles = 0;
    private static final String TEMP_FILE_NAME_TO_ZIP = "CxZippedSource";
    private static final String TEMP_COPIED_FOLDER = "CxTempSource";

    private OsaUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String composeProjectOSASummaryLink(String url, long projectId) {
        return String.format("%s/CxWebClient/portal#/projectState/%s/OSA", url, projectId);
    }

    public static void deleteTempFiles() {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            deleteTempPath(tempDir, TEMP_FILE_NAME_TO_ZIP);
            deleteTempPath(tempDir, TEMP_COPIED_FOLDER);
        } catch (Exception e) {
            log.error("Failed to delete temp files: " + e.getMessage());
        }
    }

    private static void deleteTempPath(String folder, String prefix) {
        GenericPrefixFilter filter = new GenericPrefixFilter(prefix);
        File dir = new File(folder);

        //list out all the file name with prefix
        String[] list = dir.list(filter);

        if (list == null || list.length == 0) return;

        File toDelete;

        for (String file : list) {
            toDelete = new File(folder + file);
            String type = "File: ";

            boolean isDeleted = false;
            if (toDelete.isDirectory()) {
                try {
                    type = "Folder: ";
                    FileUtils.deleteDirectory(dir);
                    isDeleted = true;
                } catch (IOException e) {
                    isDeleted = false;
                }

            } else if (toDelete.isFile()) {
                isDeleted = toDelete.delete();
            }

            log.debug(type + folder + file + " is deleted : " + isDeleted);
        }
    }

    //inner class, generic prefix filter
    private static class GenericPrefixFilter implements FilenameFilter {
        private String prefix;

        GenericPrefixFilter(String prefix) {
            this.prefix = prefix;
        }

        public boolean accept(File dir, String name) {
            return (name.startsWith(prefix));
        }
    }

//    public static File zipWorkspaceFolder(String[] fileExcluded, String[] folderExclusions, String[] fileIncluded, long maxZipSizeInBytes, String[] path, final Logger log)
//            throws IOException {
//
//        String[] combinedExcludePattern = generateExcludePattern(folderExclusions, fileExcluded);
//        String[] includeFilesPattern = processIncludedFiles(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_OSA_INCLUDED_FILES), fileIncluded);
//
//        ZipListener zipListener = new ZipListener() {
//            @Override
//            public void updateProgress(String fileName, long size) {
//                numOfZippedFiles++;
//                log.debug("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
//            }
//        };
//
//        File tempFile = File.createTempFile(TEMP_FILE_NAME_TO_ZIP, ".bin");
//        File tmpFolder = createTempDirectory();
//        for (String dirPath : path) {
//            File dir = new File(dirPath);
//            if (dir.isDirectory()) {
//                FileUtils.copyDirectory(dir, tmpFolder);
//            }
//        }
//        try (OutputStream fileOutputStream = new FileOutputStream(tempFile)) {
//            new Zipper().zip(tmpFolder, combinedExcludePattern, includeFilesPattern, fileOutputStream, maxZipSizeInBytes, zipListener);
//        } catch (Zipper.MaxZipSizeReached e) {
//            boolean verifyDeletion = tempFile.delete();
//            throw new IOException("Reached maximum upload size limit of " + FileUtils.byteCountToDisplaySize(maxZipSizeInBytes) + "Temporary directory deleted: " + verifyDeletion);
//        } catch (Zipper.NoFilesToZip e) {
//            throw new IOException("No files to zip");
//        }
//
//        log.debug("Zipping complete with " + numOfZippedFiles + " files, total compressed size: " +
//                FileUtils.byteCountToDisplaySize(tempFile.length() / 8 * 6)); // We print here the size of compressed sources before encoding to base 64
//        log.debug("Temporary file with zipped sources was created at: '" + tempFile.getAbsolutePath() + "'");
//
//        try {
//            FileUtils.deleteDirectory(tmpFolder);
//        } catch (IOException e) {
//            log.warn("Warning: failed to delete temporary folder: " + tmpFolder.getAbsolutePath());
//        }
//
//        return tempFile;
//    }
//
//    private static File createTempDirectory() throws IOException {
//        Path tempPath = Files.createTempDirectory(TEMP_COPIED_FOLDER);
//        final File temp = tempPath.toFile();
//        if (!(temp.delete())) {
//            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
//        }
//
//        if (!(temp.mkdir())) {
//            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
//        }
//
//        return (temp);
//    }
//
//    private static String[] generateExcludePattern(String[] folderExclusions, String[] filterPattern) {
//        String[] excludeFoldersPattern = processExcludeFolders(folderExclusions);
//        String[] excludeFilesPattern = processPatternFiles(filterPattern);
//
//        return ArrayUtils.addAll(excludeFilesPattern, excludeFoldersPattern);
//    }
//
//    private static String[] processExcludeFolders(String[] folderExclusions) {
//        if (folderExclusions == null) {
//            return null;
//        }
//
//        List<String> result = new ArrayList<>();
//        for (String p : folderExclusions) {
//            p = p.trim();
//            if (p.length() > 0 && !p.equals("null")) {
//                result.add("!**/" + p + "/**/*, ");
//            }
//        }
//        log.debug("Exclude folders converted to: '" + Arrays.toString(folderExclusions) + "'");
//        return folderExclusions;
//    }
//    private static String[] processPatternFiles(String[] filesExclusions) {
//        if (filesExclusions == null) {
//            return null;
//        }
//        List<String> result = new ArrayList<>();
//
//
//        for (String p : filesExclusions) {
//            p = p.trim();
//            if (p.length() > 0 && !p.equals("null")) {
//                result.add("!**/" + p.replace('\\', '/'));
//            }
//        }
//
//        log.debug("Exclude files converted to: '" + Arrays.toString(filesExclusions) + "'");
//        return result.toArray(new String[0]);
//    }

//    private static String[] processIncludedFiles(String configIncluded, String[] fileIncluded) {
//        List<String> defIncludedFiles = new ArrayList<>();
//
//        if (configIncluded != null) {
//            for (String file : StringUtils.split(configIncluded, ",")) {
//                String trimmedPattern = file.trim();
//                if (!Objects.equals(trimmedPattern, "")) {
//                    defIncludedFiles.add("**/" + trimmedPattern.replace('\\', '/'));
//                }
//            }
//        }
//
//        if (fileIncluded != null) {
//            for (String file : fileIncluded) {
//                String trimmedPattern = file.trim();
//                if (!Objects.equals(trimmedPattern, "")) {
//                    defIncludedFiles.add("**/" + trimmedPattern.replace('\\', '/'));
//                }
//            }
//        }
//
//        return defIncludedFiles.toArray(new String[0]);
//    }

}