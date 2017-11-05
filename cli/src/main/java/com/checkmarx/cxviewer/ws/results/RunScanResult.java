package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseRunID;

import org.jdom.Element;

public class RunScanResult extends SimpleResult {

	private String runId;
	private long projectId;

	public String getRunId() {
		return runId;
	}

	public long getProjectId() {
		return projectId;
	}

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			if (child.getName().equals("ScanId")) {
				runId = child.getValue();
			}
		}
	}

	@Override
	public String toString() {
		String result;
		if (isSuccessfulResponse()) {
			result = "" + this.getClass().getSimpleName() +
					"(scanId:" + runId + ",projectId:" + projectId + ")";
		} else {
			result = "" + this.getClass().getSimpleName() +
					"(Message=" + getErrorMessage() + ")";
		}
		return result;
	}

    @Override
    protected void parseReturnValue(CxWSBasicRepsonse responseObject) {

    	if (responseObject instanceof CxWSResponseRunID) {
    		CxWSResponseRunID runResponse =
    				(CxWSResponseRunID) responseObject;
    		this.runId = runResponse.getRunId();
    		this.projectId = runResponse.getProjectID();
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
    }
}
