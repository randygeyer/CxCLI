package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import org.jdom.Element;

import com.checkmarx.cxviewer.ws.generated.CxWSResponseProjectConfig;
import com.checkmarx.cxviewer.ws.generated.ProjectConfiguration;
import com.checkmarx.cxviewer.ws.generated.UserPermission;

public class GetProjectConfigResult extends SimpleResult {

	private ProjectConfiguration projectConfig;
	private UserPermission userPermission;

	public ProjectConfiguration getProjectConfig() {
		return projectConfig;
	}

	public UserPermission getUserPermission() {
		return userPermission;
	}

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		//no-op
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		if (responseObject instanceof CxWSResponseProjectConfig) {
			CxWSResponseProjectConfig projectsData = (CxWSResponseProjectConfig) responseObject;
			projectConfig = projectsData.getProjectConfig();
			userPermission = projectsData.getPermission();
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}

	@Override
	public String toString() {
		String result;
		if (isSuccessfulResponse()) {
			result = "" + this.getClass().getSimpleName() + "(projectConfig= " + projectConfig + " )";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
		}
		return result;
	}
}
