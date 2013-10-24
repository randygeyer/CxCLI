package com.checkmarx.cxviewer.data;

import java.io.Serializable;

import com.checkmarx.cxviewer.annotation.PersisentCompoundField;
import com.checkmarx.cxviewer.annotation.PersistentField;
import com.checkmarx.cxviewer.ws.generated.CxWSSingleResultData;
import com.checkmarx.cxviewer.ws.results.SeverityEnum;

public class ScanResultPoint implements Serializable {

	private static final long serialVersionUID = -1591173858551195556L;

	@PersisentCompoundField
	private ScanResultPath path;

	@PersistentField
	private long pathId;

	@PersistentField
	private String srcFileName;
	
	@PersistentField
	private String srcDirectory;
	
	@PersistentField
	private long srcLine;
	
	@PersistentField
	private String srcObject;
	
	@PersistentField
	private String destFileName;
	
	@PersistentField
	private String destDirectory;
	
	@PersistentField
	private long destLine;
	
	@PersistentField
	private String destObject;
	
	@PersistentField
	private String assignedUser;

	//private int line;

	//private int column;
	
	//private boolean falsePositive;
	
	@PersistentField
	private String remark;
	
	@PersistentField
	private SeverityEnum severity;
	
	@PersistentField
	private String state;

	public ScanResultPoint() {
		//this.paths = new ArrayList<ScanResultPath>();
	}
	
	public String getSrcFileName() {
		return srcFileName;
	}

	public void setSrcFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}

	public String getSrcDirectory() {
		return srcDirectory;
	}

	public void setSrcDirectory(String srcDirectory) {
		this.srcDirectory = srcDirectory;
	}

	public long getSrcLine() {
		return srcLine;
	}

	public void setSrcLine(long srcLine) {
		this.srcLine = srcLine;
	}

	public String getSrcObject() {
		return srcObject;
	}

	public void setSrcObject(String srcObject) {
		this.srcObject = srcObject;
	}

	public String getDestFileName() {
		return destFileName;
	}

	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

	public String getDestDirectory() {
		return destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}

	public long getDestLine() {
		return destLine;
	}

	public void setDestLine(long destLine) {
		this.destLine = destLine;
	}

	public String getDestObject() {
		return destObject;
	}

	public void setDestObject(String destObject) {
		this.destObject = destObject;
	}

	/*public void setPaths(List<ScanResultPath> paths) {
		this.paths = paths;
	}*/

	public void setPathId(long nodeId) {
		this.pathId = nodeId;
	}

	public ScanResultPath getPath() {
		return path;
	}
	
	public void setPath(ScanResultPath path) {
		this.path = path;
	}
	/*public void setFileName(String fileName) {
		this.sourceFileName = fileName;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public void setColumn(int column) {
		this.column = column;
	}*/

	/*public List<ScanResultPath> getPaths() {
		return paths;
	}*/

	public long getPathId() {
		return pathId;
	}

	/*public String getFileName() {
		return sourceFileName;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
	
	public boolean isFalsePositive() {
		return falsePositive;
	}
	
	public void setFalsePositive (boolean falsePositive) {
		this.falsePositive = falsePositive;
	}*/
	
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public SeverityEnum getSeverity() {
		return severity;
	}
	
	public void setSeverity(SeverityEnum severity) {
		this.severity = severity;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getAssignedUser() {
		return assignedUser;
	}
	
	public void setAssignedUser(String assignedUser) {
		this.assignedUser = assignedUser;
	}

	/*public String getName() {
		return sourceFileName;
	}*/
	
	@Override
	public String toString() {
		return "<scanResultPoint>\nnodeId=" + pathId + "\n\tfileName="  + "\n</scanResultPoint>";
	}
	
	public boolean containsNode(ScanResultPathNode node) {
		
		/*if (paths == null || paths.size() == 0 || node == null) {
			return false;
		}*/
		if (path == null) {
			return false;
		}
		
		for (ScanResultPathNode pathNode : path.getPathNodes()) {
			if (pathNode.equals(node)) {
				return true;
			}
		}
		return false;
	}
	
	public static ScanResultPoint adaptResult(CxWSSingleResultData data) {
		ScanResultPoint resultModel = new ScanResultPoint();
		resultModel.setRemark(data.getComment());
		resultModel.setSeverity(SeverityEnum.byCode(data.getSeverity()));
		resultModel.setState("" + data.getState());
		resultModel.setPathId(data.getPathId());
		resultModel.setSrcFileName(data.getSourceFile());
		resultModel.setSrcDirectory(data.getSourceFolder());
		resultModel.setSrcLine(data.getSourceLine());
		resultModel.setSrcObject(data.getSourceObject());
		
		resultModel.setDestFileName(data.getDestFile());
		resultModel.setDestDirectory(data.getDestFolder());
		resultModel.setDestLine(data.getDestLine());
		resultModel.setDestObject(data.getDestObject());
		resultModel.setAssignedUser(data.getAssignedUser());
		
		return resultModel;
	}
}
