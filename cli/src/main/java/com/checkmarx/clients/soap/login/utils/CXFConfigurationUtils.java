package com.checkmarx.clients.soap.login.utils;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class CXFConfigurationUtils {

    /**
     * Dynamically sets off the CXF client WSDL schema validation - needed for schema backward compatibility.
     * @param sevice the SOAP web service to configure
     */
    public static void disableSchemaValidation(Object sevice) {
        org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(sevice);
        org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
        cxfEndpoint.getEndpointInfo().setProperty("set-jaxb-validation-event-handler", "false");
    }

    /**
     * Dynamically sets the correct parameters of CXF client for NTLM Authentication
     * Connection is Keep-Alive and Turn off chunking
     * @param sevice the SOAP web service to configure
     */
    public static void setNTLMAuthentication(Object service){

        final HTTPConduit conduit = getHttpConduit(service);
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(36000);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        conduit.setClient(httpClientPolicy);
    }
    
    public static void trustAllCertificates(Object service) {
        final HTTPConduit conduit = getHttpConduit(service);

        // RJG: not sure why cxf.xml is not being applied, so applied in code
        TLSClientParameters params = conduit.getTlsClientParameters();
        if (params == null) {
            params = new TLSClientParameters();
            conduit.setTlsClientParameters(params);
        }

        params.setUseHttpsURLConnectionDefaultHostnameVerifier(true);
        params.setUseHttpsURLConnectionDefaultSslSocketFactory(true);
        params.setDisableCNCheck(true);
    }

    private static HTTPConduit getHttpConduit(Object service) {
        final Client client = ClientProxy.getClient(service);
        return (HTTPConduit) client.getConduit();
    }
    
}
