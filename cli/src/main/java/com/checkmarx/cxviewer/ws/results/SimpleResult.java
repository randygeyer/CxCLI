package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

abstract public class SimpleResult {

	private String responseXmlString;

	boolean isSuccesfullResponse;

	protected String errorMessage;
	
	public SimpleResult() {
	}

	public String getResponseXmlString() {
		return responseXmlString;
	}

	public boolean isSuccesfullResponce() {
		return isSuccesfullResponse;
	}
	
	public void setSuccessfull(boolean isSuccess) {
		isSuccesfullResponse = isSuccess;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	abstract protected void parseReturnValue(Element returnValueNode);

    abstract protected void parseReturnValue(CxWSBasicRepsonse responseObject);

	public void parseResponseObject(CxWSBasicRepsonse responseObject){
		if (!responseObject.isIsSuccesfull()) {
			this.isSuccesfullResponse = false;
			this.errorMessage = responseObject.getErrorMessage();
		} else {
			this.isSuccesfullResponse = true;
			this.errorMessage = "";
			parseReturnValue(responseObject);
		}
	}

	public void parseResponseXmlString(String responseXmlString) {		
		this.responseXmlString = responseXmlString;
		//CxLogger.getLogger().trace("Parsing XML response string:\n" + this.responseXmlString);
		SAXBuilder sb = new SAXBuilder();
		sb.setIgnoringBoundaryWhitespace(true);
		Document doc;
		try {
			doc = sb.build(new StringReader(responseXmlString));
			for (Object childObj : doc.getRootElement().getChildren()) {
				Element child = (Element) childObj;
				if (child.getName().equals("IsSuccesfull")) {
					isSuccesfullResponse = Boolean.parseBoolean(child.getValue());
				} else if (child.getName().equals("Message")) {
					errorMessage = child.getValue();
				} else if (child.getName().equals("ReturnValue")) {
					parseReturnValue(child);
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected String getInvalidTypeErrMsg(Object responseObject) {
	    return "responseObject is invalid: input parameter type is ["
		    + responseObject.getClass().getName() + "] - "
		    + this.getClass().getName()
		    + " is expecting other as parameter of parseReturnValue()";
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
