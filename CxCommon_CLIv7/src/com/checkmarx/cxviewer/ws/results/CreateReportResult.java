package com.checkmarx.cxviewer.ws.results;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;

public class CreateReportResult extends SimpleResult {

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		// no-op
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		// no-op
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(request succeed = " + isSuccesfullResponse + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
		}
		return result;
	}
}
