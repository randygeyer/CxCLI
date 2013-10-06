package com.checkmarx.cxviewer.ws.results;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;

public class GeneralResult extends SimpleResult {

	@Override
	protected void parseReturnValue(Element returnValueNode) {
	}


	@Override
    protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		// does nothing
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
}
