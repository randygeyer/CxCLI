package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
import org.jdom.Element;

public class LoginResult extends SimpleResult {

	private String sessionId;
	private boolean isScanner;
	private boolean isAllowedManageUsers;
	private CxWSResponseLoginData loginData;

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			if (child.getName().equals("SessionId")) {
				sessionId = child.getValue();
			}
		}
	}

	public String getSessionId() {
		return sessionId;
	}

    public boolean isScanner() {
	return isScanner;
    }

    public boolean isAllowedManageUsers() {
	return isAllowedManageUsers;
    }



	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(SessionId="+ sessionId + ")";
		}
		else {
			result = "" + this.getClass().getSimpleName() + "(Message="+ getErrorMessage() + ")";
		}
		return result;
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {

		if (responseObject instanceof CxWSResponseLoginData) {
			loginData = ((CxWSResponseLoginData) responseObject);
			this.sessionId = loginData.getSessionId();
			this.isScanner = loginData.isIsScanner();
			this.isAllowedManageUsers = loginData.isIsAllowedToManageUsers();
		}
		else {
			throw new IllegalArgumentException("responseObject is invalid: input parameter type is [" + responseObject.getClass().getName() + "] - "
												+ this.getClass().getName() + " is expection other as parameter of parseReturnValue()");
		}
	}

	public CxWSResponseLoginData getLoginData() {
		return loginData;
	}
}
