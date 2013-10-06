package com.checkmarx.cxviewer.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.checkmarx.cxviewer.annotation.PersisentCompoundField;
import com.checkmarx.cxviewer.annotation.PersistentField;

public class ScanResult implements Serializable {
	
	private static final long serialVersionUID = 8939628307552711963L;

	@PersisentCompoundField
	private List<ScanResultQuery> queries;
	
	@PersistentField
	private String projectName;
	
	@PersistentField
	private long projectId;
	
	// Bound project related fields
	@PersistentField
	private Date queuedTime;
	
	@PersistentField
	private long scanId;
	
	@PersistentField
	private long resultId;
	
	@PersistentField
	private String runId;

	public List<ScanResultQuery> getQueries() {
		return queries;
	}

	public void setResults(List<ScanResultQuery> results) {
		this.queries = results;
	}
	
	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}
	
	public void setQueuedTime(Date queuedTime) {
		this.queuedTime = queuedTime;
	}
	
	public Date getQueuedTime() {
		return queuedTime;
	}
	
	public void setScanId(long scanId) {
		this.scanId = scanId;
	}
	
	public long getScanId() {
		return scanId;
	}
	
	public long getResultId() {
		return resultId;
	}

	public void setResultId(long resultId) {
		this.resultId = resultId;
	}
	
	public String getRunId() {
		return runId;
	}
	
	public void setRunId(String runId) {
		this.runId = runId;
	}

	public ScanResultPoint findPathPoint(long pathId) {
		for (ScanResultQuery result : queries) {
			List<ScanResultPoint> queryRes = result.getQueryResults();
			for (ScanResultPoint point : queryRes) {
				if (point.getPathId() == pathId) {
					return point;
				}
			}
		}
		return null;
	}
}
