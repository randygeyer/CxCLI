package com.checkmarx.cxconsole.commands.job.utils;

import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobUtilException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by nirli on 05/11/2017.
 */
public class PathHandler {

    protected static Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");

    private PathHandler() {
        throw new IllegalStateException("Utility class");
    }

    public static String initFilePath(String projectName, String fileName, String extension, String parentDirectoryPath) throws CLIJobUtilException {
        String resultFilePath = "";

        File resultFile = new File(fileName);
        String fileNamePath = resultFile.getPath();
        if (resultFile.isAbsolute()) {
            // Path is absolute
            if (fileNamePath.endsWith(File.separator)) {
                //Directory path
                File resDirs = new File(fileNamePath);
                if (!resDirs.exists()) {
                    boolean result = resDirs.mkdirs();
                    if (!result) {
                        throw new CLIJobUtilException("Error with directory");
                    }
                }
                resultFilePath = fileNamePath + File.separator + normalizePathString(projectName + extension);
            } else {
                // File path
                if (fileName.contains(File.separator)) {
                    String dirPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
                    File xmlResDirs = new File(dirPath);
                    if (!xmlResDirs.exists()) {
                        boolean result = xmlResDirs.mkdirs();
                        if (!result) {
                            throw new CLIJobUtilException("Error with directory");
                        }
                    } else if (xmlResDirs.isFile()) {
                        //cannot create directory - file already exists
                        log.error("Unable to create directory hierarchy [" + xmlResDirs.getAbsolutePath() + "] for storing results: file with same name already exists.");
                        throw new CLIJobUtilException("Unable to create directory hierarchy [" + xmlResDirs.getAbsolutePath() + "] for storing results: file with same name already exists.");
                    }
                }
                resultFilePath = fileName;
            }
        } else {
            // Path is not absolute
            if (!fileName.toLowerCase().endsWith(extension.toLowerCase())) {
                //Directory path
                String dirPath = parentDirectoryPath + File.separator + fileName;
                File resDirs = new File(dirPath);
                if (!resDirs.exists()) {
                    boolean result = resDirs.mkdirs();
                    if (!result) {
                        log.error("Error with directory");
                        throw new CLIJobUtilException("Error with directory");
                    }
                }
                resultFilePath = dirPath + File.separator + normalizePathString(projectName + extension);
            } else {
                //File path
                if (fileName.contains(File.separator)) {
                    String dirPath = parentDirectoryPath + File.separator + fileName.substring(0, fileName.lastIndexOf(File.separator));
                    File xmlResDirs = new File(dirPath);
                    if (!xmlResDirs.exists()) {
                        boolean result = xmlResDirs.mkdirs();
                        if (!result) {
                            log.error("Error with directory");
                            throw new CLIJobUtilException("Error with directory");
                        }
                    } else if (xmlResDirs.isFile()) {
                        //cannot create directory - file already exists
                        log.error("Unable to create directory hierarchy [" + xmlResDirs.getAbsolutePath() + "] for storing results: file with same name already exists.");
                        throw new CLIJobUtilException("");
                    }
                }

                resultFilePath = parentDirectoryPath + File.separator + fileName;
            }
        }

        return resultFilePath;
    }

    public static String normalizePathString(String projectName) {
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

    public static String resolveReportPath(String projectName, String ext, String file, String reportName, String workDirectory) throws CLIJobUtilException {
        String toLog = "";
        if (!isFilenameValid(file)) {
            if (!StringUtils.isEmpty(file)) {
                toLog = "The path you specified is invalid. ";
            }
            file = reportName;
            log.warn(toLog + "Using default location for " + ext + " report.");
        }
        return PathHandler.initFilePath(projectName, file, "." + ext.toLowerCase(), workDirectory);
    }

    private static boolean isFilenameValid(String filePath) {
        Boolean ret = true;
        try {
            File file = new File(filePath);
            if (file.isDirectory() || StringUtils.isEmpty(filePath)) {
                ret = false;
            }
            file.getCanonicalPath();
        } catch (IOException e) {
            ret = false;
        }
        return ret;
    }

}
