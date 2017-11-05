//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//
//public class GeneralResult extends SimpleResult {
//
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//	}
//
//
//	@Override
//    protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		// does nothing
//    }
//
//	@Override
//	public String toString() {
//		String result;
//		if (isSuccessfulResponse()) {
//			result = "" + this.getClass().getSimpleName() + "()";
//		} else {
//			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
//		}
//		return result;
//	}
//}
