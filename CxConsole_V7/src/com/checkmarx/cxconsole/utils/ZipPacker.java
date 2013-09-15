package com.checkmarx.cxconsole.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipPacker {
	
	private String srcFolder;
	private String destZipFile;
	private String[] ignoredFolders;
	private String[] ignoredExtensions;

	
	public ZipPacker(String srcFolder, String destZipFile,
			String ignoredFolders, String ignoredExtensions) {
		
		this.srcFolder = srcFolder;
		this.destZipFile = destZipFile;
		this.ignoredFolders = splitSemicolonSeparatedString(ignoredFolders);
		this.ignoredExtensions = splitCommaSeparatedString(ignoredExtensions);
	}

	/**
	 * Packs source folder <code>srcFolder</code> into zip archive
	 * 
	 * @param srcFolder - path to source folder to be packed
	 * @param destZipFile - path of destination archive 
	 * @param ignoredFolders - list of comma-separated folders to excluded from archive
	 * @param ignoredExtensions - list of comma-separated file extensions to excluded from archive
	 */
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
		
		File file;
		String entryName;
		for (String fileName : folder.list()) {
			file = new File(folder.getPath() + "/" + fileName); //$NON-NLS-1$
			entryName = entryPath + "/" + fileName; //$NON-NLS-1$
			if (file.isDirectory()) {
				if (!isIgnoredFolder(fileName)) {
					packFolder(zOut, file, entryName);
				}
			} else if (!isIgnoredExtension(fileName)) {
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					zOut.putNextEntry(new ZipEntry(entryName));
					while ((len = in.read(buf)) > 0) {
						zOut.write(buf, 0, len);
					}
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

	private boolean isIgnoredFolder(String folderName) {
		String folderNameLowerCase = folderName.toLowerCase();
		for (String ignoredFolderName : ignoredFolders) {
			if (folderNameLowerCase.contains(ignoredFolderName.trim().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean isIgnoredExtension(String fileName) {
		String fileNameLowerCase = fileName.toLowerCase();
		for (String ignoredExtension : ignoredExtensions) {
			if (!ignoredExtension.isEmpty() && fileNameLowerCase.endsWith(ignoredExtension.trim())) {
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
