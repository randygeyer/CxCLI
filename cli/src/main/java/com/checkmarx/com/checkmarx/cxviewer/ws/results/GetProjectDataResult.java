package com.checkmarx.cxviewer.ws.results;

import java.util.List;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseProjectsDisplayData;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;

public class GetProjectDataResult extends SimpleResult {

	private List<ProjectDisplayData> projectData;
	
	public List<ProjectDisplayData> getProjectData() {
		return projectData;
	}
	
	@Override
	protected void parseReturnValue(Element returnValueNode) {
		//no-op
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		if (responseObject instanceof CxWSResponseProjectsDisplayData) {
			CxWSResponseProjectsDisplayData projectsData = (CxWSResponseProjectsDisplayData) responseObject;
			projectData = projectsData.getProjectList().getProjectDisplayData();
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}
	
	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(results length = " + projectData.size() + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
		}
		return result;
	}
}
