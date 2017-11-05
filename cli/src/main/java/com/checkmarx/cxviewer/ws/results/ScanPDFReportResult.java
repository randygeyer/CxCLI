//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//import com.checkmarx.cxviewer.ws.generated.CxWSResponseScanResults;
//
//public class ScanPDFReportResult extends SimpleResult {
//
//	private byte [] pdfData;
//
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//		// no-op
//	}
//
//	public byte[] getPdfData() {
//		return pdfData;
//	}
//
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		if (responseObject instanceof CxWSResponseScanResults) {
//			CxWSResponseScanResults pdfStatus = (CxWSResponseScanResults) responseObject;
//			pdfData = pdfStatus.getScanResults();
//		} else {
//			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
//		}
//	}
//
//	@Override
//	public String toString() {
//		String result;
//		if (isSuccessfulResponse()) {
//			result = "" + this.getClass().getSimpleName() + "(results length = " + pdfData.length + ")";
//		} else {
//			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
//		}
//		return result;
//	}
//}
