package com.checkmarx.cxosa.utils;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by Galn on 28/02/2017.
 */
abstract class CxFileUtils {

    static void deleteTempPath(String folder, String prefix, Logger log) {
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
}
