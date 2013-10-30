package com.checkmarx.cxconsole.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipPacker {

    private static Logger logger = Logger.getLogger(ZipPacker.class);

	private String srcFolder;
	private String destZipFile;
	private File[] ignoredFolders;
	private String[] ignoredExtensions;

	
	public ZipPacker(String srcFolder, String destZipFile,
			File[] ignoredFolders, String ignoredExtensions) {
		
		this.srcFolder = srcFolder;
		this.destZipFile = destZipFile;
		this.ignoredFolders = ignoredFolders;
		this.ignoredExtensions = splitCommaSeparatedString(ignoredExtensions);

        //Debug output
        if (logger.isDebugEnabled())
        {
            LinkedList<String> ignoredFoldersList = new LinkedList<String>();
            for (File f : this.ignoredFolders)
            {
                ignoredFoldersList.add(f.toString());
            }
            logger.debug("List of folders to ignore: " + StringUtils.join(ignoredFoldersList,", "));
            logger.debug("List of extensions to ignore: " + StringUtils.join(this.ignoredExtensions,", "));
        }
	}


	public boolean zipFolder() {
		
		ZipOutputStream zOut = null;
		FileOutputStream fileWriter = null;
		try {
			fileWriter = new FileOutputStream(destZipFile);
			zOut = new ZipOutputStream(fileWriter);
			packFolder(zOut, new File(srcFolder), "");
			zOut.flush();
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			return false;
		} catch (IOException e) {
			// TODO: handle exception
			return false;
		} finally {
			if (zOut != null) {
				try {
					zOut.close();
				} catch (IOException e) {
					// TODO: handle exception
				}
			}
		}
		return true;
	}
	
	private void packFolder(ZipOutputStream zOut, File folder, String entryPath) {
		
		for (File file : folder.listFiles()) {
			//File file = new File(folder.getPath() + "/" + fileName); //$NON-NLS-1$
            String entryName = entryPath + "/" + file.getName(); //$NON-NLS-1$
			if (file.isDirectory()) {
				if (!isIgnoredFolder(file)) {
					packFolder(zOut, file, entryName);
				}
			} else if (!isIgnoredExtension(file)) {
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					zOut.putNextEntry(new ZipEntry(entryName));
					while ((len = in.read(buf)) > 0) {
						zOut.write(buf, 0, len);
					}
                    logger.debug("Packed source file: " + entryName);
				} catch (IOException e) {
					// TODO: handle exception
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO: handle exception
						}
					}
				}
			}
		}
	}

	private boolean isIgnoredFolder(File folder) {
        for (File ignoredFolder : ignoredFolders) {
            try {
                if (folder.getCanonicalFile().equals(ignoredFolder))
                {
                    logger.debug("Folder ignored: " + folder);
                    return true;
                }
            } catch (IOException e)
            {
                // This exception occurs if folder is not accessible in the file system
                // In this case we can ignore this folder
                Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");
                log.warn("Could not read file: " + folder);
                log.warn(e.getMessage());
                return false;
            }

		}
		return false;
	}

	private boolean isIgnoredExtension(File file) {
		String fileNameLowerCase = file.getName().toLowerCase();
		for (String ignoredExtension : ignoredExtensions) {
			if (!ignoredExtension.isEmpty() && fileNameLowerCase.endsWith(ignoredExtension.trim())) {
                logger.debug("File ignored by extension: " + file);
                return true;
			}
		}
		return false;
	}
	
	private String[] splitCommaSeparatedString(String commaSeparatedStrng) {
		String[] array;
		array = commaSeparatedStrng.split(",");
		if (array == null) {
			array = new String[0];
		}		
		return array;
	}
	
	private String[] splitSemicolonSeparatedString(String semicolonSeparatedStrng) {
		String[] array;
		array = semicolonSeparatedStrng.split(";");
		if (array == null) {
			array = new String[0];
		}		
		return array;
	}
}
