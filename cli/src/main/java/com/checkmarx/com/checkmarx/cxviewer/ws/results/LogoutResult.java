package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import org.jdom.Element;

public class LogoutResult extends SimpleResult {

	@Override
	protected void parseReturnValue(Element returnValueNode) {
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
	// does nothing
    }
}
