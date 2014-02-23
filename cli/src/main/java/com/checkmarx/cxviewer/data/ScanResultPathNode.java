package com.checkmarx.cxviewer.data;

import java.io.Serializable;

import com.checkmarx.cxviewer.annotation.PersistentField;

public class ScanResultPathNode implements Serializable {

	private static final long serialVersionUID = -1361815067771030664L;

	@PersistentField(getterMapping = "NodeFileName")
	private String fileName;
	
	@PersistentField
	private String fullName;

	@PersistentField
	private int line;

	@PersistentField
	private int column;

	@PersistentField
	private int nodeId;

	@PersistentField
	private String name;

	@PersistentField
	private int length;

	private transient ScanResult scanResult;

    public ScanResultPathNode() {
	}
	
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public ScanResult getScanResult() {
		return scanResult;
	}

	public void setScanResult(ScanResult scanResult) {
		this.scanResult = scanResult;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLength(int length) {
		this.length = length;
	}

    @Deprecated
	public String getAdaptedFileName() {
		return scanResult.getProjectName()+'/'+fileName.replace('\\', '/');
	}
	
	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

    @Override
	public String toString() {
		return "<pathnode>\n\tfilename: " + fileName + "\n\tline: " + line + "\n\tcolumn: " + column + "\n\tlength: " + length + "\n\tname: " + name + "\n\tnodeId: " + nodeId + "\n\tscanResult: " + scanResult + "\n</pathnode>";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ScanResultPathNode)) {
			return false;
		}
		ScanResultPathNode comparative = (ScanResultPathNode) obj;
		if (this.fileName.equals(comparative.fileName)
				&& this.name.equals(comparative.name)
				&& this.column == comparative.column
				&& this.line == comparative.line				
				&& this.length == comparative.length
				/*&& this.nodeId == comparative.nodeId*/) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}

}