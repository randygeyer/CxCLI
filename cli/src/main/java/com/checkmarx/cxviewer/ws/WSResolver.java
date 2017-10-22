package com.checkmarx.cxviewer.ws;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.utils.CXFConfigurationUtils;
import com.checkmarx.cxviewer.ws.resolver.CxClientType;
import com.checkmarx.cxviewer.ws.resolver.CxWSResolver;
import com.checkmarx.cxviewer.ws.resolver.CxWSResolverSoap;
import com.checkmarx.cxviewer.ws.resolver.CxWSResponseDiscovery;


public class WSResolver {
    private static final Logger logger = Logger.getLogger(WSResolver.class);
    protected static final String WS_NAME="CxWSResolver";
	protected static String WS_NAMESPACE = "http://Checkmarx.com";
	protected static String WS_URL="/cxwebinterface/cxWSResolver.asmx?WSDL";
    private final static int CLI_WEBSERVICE_VERSION = 1;

    protected static CxWSResolverSoap wService;

	
	public static String getServiceURL(String server) throws Exception {

		wService=getWService(server);
		if (wService!=null) {
			CxWSResponseDiscovery resp=wService.getWebServiceUrl(CxClientType.CLI, CLI_WEBSERVICE_VERSION);
			if (!resp.isIsSuccesfull()) {
				logger.error("CxWebserviceResolver error: "+resp.getErrorMessage());
				throw new Exception("CxWebserviceResolver error: "+resp.getErrorMessage());
			} else {
			    return resp.getServiceURL();
            }
		}
		else {
			logger.error("No CxWebserviceResolver is available");
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
					logger.error("Cannot access CxWebserviceResolver " + wsdlURL + ": " + e.getMessage());
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
					logger.error("Cannot access CxWebserviceResolver " + wsdlURL + ": " + e.getMessage());
					return null;
				}
			}
			

			// Temporary solution
			SSLUtilities.trustAllHostnames();
			SSLUtilities.trustAllHttpsCertificates();

			CxWSResolver ws = new CxWSResolver(wsdlLocation);
			wService=ws.getCxWSResolverSoap();

            CXFConfigurationUtils.disableSchemaValidation(wService);

            if("false".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                CXFConfigurationUtils.setNTLMAuthentication(wService);
            }
		}
		return wService;
	}
}
