package com.checkmarx.cxviewer.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.resolver.CxClientType;


abstract class WSMgrBase {
	protected static String WS_INTERFACE_URL_MARKER = "Cxwebinterface";
	protected static String WS_WSDL_URL_MARKER = "wsdl";
	protected static String WS_ASMX_URL_MARKER = ".asmx";
	
	protected static String WS_NAMESPACE = "http://Checkmarx.com";


	
	/**
	 * get webservice name
	 * @return String
	 */
	protected abstract String getWSName();

	/**
	 * @throws WebServiceException
	 * @param wsdlLocation
	 * @return
	 */
	public abstract Object connectWebService(URL wsdlLocation);
	


	public String resolveServiceLocation(String serverName) throws Exception {
		if (serverName.endsWith("asmx")) {
			return validateAndGetServerName(serverName);
		}
		
		String wsdlLocation=WSResolver.getServiceURL(serverName);
		if (wsdlLocation==null) {
			throw new Exception("Cannot resolve WS location");
		}
		return wsdlLocation;
	}
	
	public String validateAndGetServerName(String serverName) throws Exception {
		if (serverName.endsWith("asmx")) {
			return checkServerNameProtocol(serverName);
		}

		String correctedServerName = "";
		String portalURLEnding = WS_INTERFACE_URL_MARKER + "/" + getWSName()+WS_ASMX_URL_MARKER;
		if (!serverName.toUpperCase().endsWith(portalURLEnding.toUpperCase())) {

			if (serverName.toUpperCase().endsWith((portalURLEnding + "?" + WS_WSDL_URL_MARKER).toUpperCase())) {
				correctedServerName = serverName.substring(0, serverName.length() - 5);
			}
			else
			if (serverName.toUpperCase().endsWith(WS_INTERFACE_URL_MARKER.toUpperCase())) {
				correctedServerName = serverName + "/" + getWSName()+WS_ASMX_URL_MARKER;
				try {
					String protocolCheckedName = checkServerNameProtocol(correctedServerName);
					return protocolCheckedName;
				}
				catch (Exception e) {
					// ignore
				}
				
				throw new Exception("Server name is invalid");
			}
			else {
				correctedServerName = serverName + "/" + WS_INTERFACE_URL_MARKER + "/" + getWSName()+WS_ASMX_URL_MARKER;
				try {
					return checkServerNameProtocol(correctedServerName);
				}
				catch (Exception e) {
					// ignore
				}
				throw new Exception("Server name is invalid");
			}
		}
		else {
			correctedServerName = serverName;
		}

		return checkServerNameProtocol(correctedServerName);
	}
	
	public static URL makeWsdlLocation(String serverName) {
		try {
			return tryMakeWsdlURL(serverName);
		}
		catch (MalformedURLException e) {
			CxLogger.getLogger().error(e);
			return null;
		}
	}

	protected static URL tryMakeWsdlURL(String serverName) throws MalformedURLException {
		return new URL(serverName);
	}

	protected String checkServerNameProtocol(String serverName) throws IOException {
		String srvName = serverName;
		if (isGoodServerName(srvName)) {
			return srvName;
		}

		if (!serverName.startsWith("http") && !serverName.startsWith("https")) {
			srvName = "https://" + serverName;
			if (isGoodServerName(srvName)) {
				return srvName;
			}

			srvName = "http://" + serverName;
			if (isGoodServerName(srvName)) {
				return srvName;
			}
		}

		// throws relevant Exception
		URL wsdlLocation;
		wsdlLocation = tryMakeWsdlURL(serverName);
		InputStream is = wsdlLocation.openStream();
		is.close();
		return serverName;
	}

	protected boolean isGoodServerName(String serverName) throws IOException {
		if (serverName == null || serverName.isEmpty()) {
			return false;
		}
		URL wsdlLocation;
		try {
			wsdlLocation = tryMakeWsdlURL(serverName);
			connectWebService(wsdlLocation);
		}
		catch (Throwable e) {
			CxLogger.getLogger().trace("Exception during connection to webservice. Assume server location \"" + serverName + "\" as incorrect.");
			return false;
		}
		return true;
	}

	/**
	 * @deprecated Check whether name is legal: - cannot start with digit - can
	 *             contain only latin letters, spaces and "@" sign (C# class
	 *             name restrictions)
	 * @param name
	 * @return
	 */
	public static boolean isValidProjectName(String name) {
		Pattern nameCheckPattern = Pattern.compile("[\\d]+.*");
		Matcher m = nameCheckPattern.matcher(name);
		if (m.matches()) {
			// Cannot start from digit
			return false;
		}

		nameCheckPattern = Pattern.compile("[@\\w ]+");
		m = nameCheckPattern.matcher(name);
		return m.matches();
	}
	

}
