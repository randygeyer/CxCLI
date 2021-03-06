package com.checkmarx.clients.soap.utils;

import com.checkmarx.clients.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.clients.soap.login.exceptions.CxSoapLoginClientException;
import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nirli on 29/10/2017.
 */
public class SoapClientUtils {

    private static final String CX_CLI_WEB_SERVICE_URL = "/cxwebinterface/CLI/CxCLIWebServiceV1.asmx";
    private static final int TIMEOUT_FOR_CX_SERVER_AVAILABILITY = 250;

    private SoapClientUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateResponse(CxWSBasicRepsonse responseObject) throws CxSoapClientValidatorException {
        if (responseObject == null || !responseObject.isIsSuccesfull()) {
            if (responseObject != null && responseObject.getErrorMessage() != null) {
                throw new CxSoapClientValidatorException("Error validate response: " + responseObject.getErrorMessage());
            } else if (responseObject == null) {
                throw new CxSoapClientValidatorException("Error validate response: no response was recieved from the server.");
            }
        }
    }

    public static String resolveServerProtocol(String originalHost) throws CxSoapLoginClientException {
        if (!originalHost.startsWith("http") && !originalHost.startsWith("https")) {
            String httpsProtocol = "https://" + originalHost;
            if (isCxWebServiceAvailable(httpsProtocol)) {
                return httpsProtocol;
            }

            String httpProtocol = "http://" + originalHost;
            if (isCxWebServiceAvailable(httpProtocol)) {
                return httpProtocol;
            }

            throw new CxSoapLoginClientException("Cx web service is not available in server: " + originalHost);
        } else {
            return originalHost;
        }
    }

    public static String buildHostWithWSDL(String host) {
        return host + CX_CLI_WEB_SERVICE_URL;
    }

    private static boolean isCxWebServiceAvailable(String host) {
        int responseCode;
        try {
            URL urlAddress = new URL(buildHostWithWSDL(host));
            HttpURLConnection httpConnection = (HttpURLConnection) urlAddress.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpConnection.setConnectTimeout(TIMEOUT_FOR_CX_SERVER_AVAILABILITY);
            responseCode = httpConnection.getResponseCode();
        } catch (Exception e) {
            return false;
        }

        return (responseCode != 404);
    }

}