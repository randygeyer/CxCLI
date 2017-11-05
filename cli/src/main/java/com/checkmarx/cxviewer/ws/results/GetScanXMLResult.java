//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//import com.checkmarx.cxviewer.ws.generated.CxWSResponseScanResults;
//
//public class GetScanXMLResult extends SimpleResult {
//
//	private String reportURL;
//	private String xml;
//	private byte[] xmlData;
//
//	@Deprecated
//	/**
//	 * getXmlReport() should be used instead
//	 *
//	 * @return
//	 */
//	public String getReportURL() {
//		return reportURL;
//	}
//
//	@Deprecated
//	public String getXmlReport() {
//		return xml;
//	}
//
//	public byte[] getXmlData() {
//		return xmlData;
//	}
//
//	// <ReportURL>http://184.73.67.93/CxReports/gul0jifh.ggh.xml</ReportURL>
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
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		if (responseObject instanceof CxWSResponseScanResults) {
//			CxWSResponseScanResults xmlReportResponse = (CxWSResponseScanResults) responseObject;
//			this.xml = new String(xmlReportResponse.getScanResults());
//			this.xml = this.xml.substring(3, this.xml.length());
//			this.xmlData = xmlReportResponse.getScanResults();
//			this.reportURL = null;
//		} else {
//			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
//		}
//	}
//}
