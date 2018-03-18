package com.checkmarx.clients.soap.login.utils;

import org.apache.cxf.transports.http.configuration.ConnectionType;

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
    public static void setNTLMAuthentication(Object sevice){

        org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(sevice);
        org.apache.cxf.transport.http.HTTPConduit conduit = (org.apache.cxf.transport.http.HTTPConduit) client.getConduit();
        org.apache.cxf.transports.http.configuration.HTTPClientPolicy httpClientPolicy = new org.apache.cxf.transports.http.configuration.HTTPClientPolicy();
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        conduit.setClient(httpClientPolicy);

    }
}
