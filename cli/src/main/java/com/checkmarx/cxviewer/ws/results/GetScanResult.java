//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//
///**
// * Represents XML response for invoking
// * web-service getScanResults() method
// *
// * @author Olekisy Mysnyk
// * @version 1.0 / 2011
// */
//public class GetScanResult extends SimpleResult {
//
//	private String reportURL;
//
//	/* (non-Javadoc)
//	 * @see com.checkmarx.cxviewer.ws.results.SimpleResult#parseReturnValue(org.jdom.Element)
//	 */
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//		for (Object childObj : returnValueNode.getChildren()) {
//			Element child = (Element) childObj;
//			if (child.getName().equals("ReportURL")) {
//				reportURL = child.getValue();
//			}
//		}
//	}
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
//
//	public String getReportURL() {
//		return reportURL;
//	}
//
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		// no-op
//	}
//}
