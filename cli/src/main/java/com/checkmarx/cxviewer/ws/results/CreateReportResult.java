//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//public class CreateReportResult extends SimpleResult {
//
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//		// no-op
//	}
//
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		// no-op
//	}
//
//	@Override
//	public String toString() {
//		String result;
//		if (isSuccessfulResponse()) {
//			result = "" + this.getClass().getSimpleName() + "(request succeed = " + isSuccessfulResponse + ")";
//		} else {
//			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
//		}
//		return result;
//	}
//}
