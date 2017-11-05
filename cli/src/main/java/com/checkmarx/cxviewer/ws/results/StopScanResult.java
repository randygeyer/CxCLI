//package com.checkmarx.cxviewer.ws.results;
//
//import org.jdom.Element;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//
//public class StopScanResult extends SimpleResult {
//
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//	}
//
//
//	@Override
//	public String toString() {
//		String result;
//		if (isSuccessfulResponse()) {
//			result = "" + this.getClass().getSimpleName() + "()"; //$NON-NLS-1$ //$NON-NLS-2$
//		} else {
//			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		}
//		return result;
//	}
//
//
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		//does nothing
//	}
//}
