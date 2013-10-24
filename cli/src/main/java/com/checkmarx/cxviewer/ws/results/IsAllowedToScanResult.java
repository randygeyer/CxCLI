package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import org.jdom.Element;

public class IsAllowedToScanResult extends SimpleResult {

	private boolean isAllowed;
	
	@Override
	protected void parseReturnValue(Element returnValueNode) {
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			if (child.getName().equals("IsAllowedToScan")) {
				isAllowed = Boolean.parseBoolean(child.getValue());
			}
		}
	}
	
	public boolean isAllowedToScan() {
		return isAllowed;
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "()";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
		}
		return result;
	}

    @Override
    protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
	//does nothing
    }
}
