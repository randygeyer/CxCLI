package com.checkmarx.cxviewer.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.resolver.CxClientType;
import com.checkmarx.cxviewer.ws.resolver.CxWSResolver;
import com.checkmarx.cxviewer.ws.resolver.CxWSResolverSoap;
import com.checkmarx.cxviewer.ws.resolver.CxWSResponseDiscovery;


public class WSResolver {
	protected static final String WS_NAME="CxWSResolver";
	protected static String WS_NAMESPACE = "http://Checkmarx.com";
	protected static String WS_URL="/cxwebinterface/cxWSResolver.asmx?WSDL";
	
	protected static CxWSResolverSoap wService;

	
	public static String getServiceURL(String server, String clientType, int version) {
		CxClientType type=CxClientType.NONE;
		try {
			type=CxClientType.fromValue(clientType);
		}
		catch(Exception e) {
		}
		wService=getWService(server);
		if (wService!=null) {
			CxWSResponseDiscovery resp=wService.getWebServiceUrl(type, version);
			if (!resp.isIsSuccesfull()) {
				CxLogger.getLogger().error("CxWebserviceResolver error: "+resp.getErrorMessage());
			}
			return resp.getServiceURL();
		}
		else {
			CxLogger.getLogger().error("No CxWebserviceResolver is available");
		}
		return null;
	}
	
	protected static CxWSResolverSoap getWService(String server) {
		if (wService==null) {
			URL wsdlLocation=null;
			
			String wsdlURL=server+WS_URL;
			if (!wsdlURL.startsWith("http")) {
				try {
					//try HTTP
					wsdlURL="http://"+server+WS_URL;
					wsdlLocation =new URL(wsdlURL);
				}
				catch (MalformedURLException e) {
					CxLogger.getLogger().error("Cannot access CxWebserviceResolver "+wsdlURL+": "+e.getMessage());
					//try HTTPS
					wsdlURL="https://"+server+WS_URL;
					wsdlLocation=null;
				}
			}
			
			if (wsdlLocation==null) {
				try {
					wsdlLocation =new URL(wsdlURL);
				}
				catch (MalformedURLException e) {
					CxLogger.getLogger().error("Cannot access CxWebserviceResolver "+wsdlURL+": "+e.getMessage());
					return null;
				}
			}
			
			QName serviceName = new QName(WS_NAMESPACE, WS_NAME);

			// Temporary solution
			SSLUtilities.trustAllHostnames();
			SSLUtilities.trustAllHttpsCertificates();

			CxWSResolver ws = new CxWSResolver(wsdlLocation, serviceName);
			wService=ws.getCxWSResolverSoap();
		}
		return wService;
	}
}
