package com.checkmarx.login.soap;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.Credentials;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.exceptions.CxSoapLoginClientException;
import com.checkmarx.login.soap.utils.CXFConfigurationUtils;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapLoginClient {

    private Logger log = Logger.getLogger(LOG_NAME);

    private CxCLIWebServiceV1Soap cxSoapClient;

    private String sessionId;

    public void initSoapClient(URL wsdlLocation) throws CxSoapLoginClientException {
        try {
            final URL wsdlLocationWithWSDL = new URL(wsdlLocation.toString() + "?WSDL");
            CxCLIWebServiceV1 ws = new CxCLIWebServiceV1(wsdlLocationWithWSDL);
            cxSoapClient = ws.getCxCLIWebServiceV1Soap();

            CXFConfigurationUtils.disableSchemaValidation(cxSoapClient);

            if ("false".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                CXFConfigurationUtils.setNTLMAuthentication(cxSoapClient);
            }

            if ("true".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                SoapClientUtils.initKerberos();
            }

            log.trace("SOAP client was initiated successfully");
        } catch (MalformedURLException e) {
            log.fatal("Error initiate the SOAP client: " + e.getMessage());
            throw new CxSoapLoginClientException("Error initiate the SOAP client: " + e.getMessage());
        }
    }

    public CxWSResponseLoginData login(String userName, String password) throws CxSoapLoginClientException {
        Credentials credentials = new Credentials();
        credentials.setUser(userName);
        credentials.setPass(password);

        CxWSResponseLoginData response = cxSoapClient.login(credentials, 1033);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.fatal("Failed to login :" + response.getErrorMessage());
            throw new CxSoapLoginClientException("Failed to login: " + response.getErrorMessage());
        }

        sessionId = response.getSessionId();
        return response;
    }

    public CxWSResponseLoginData ssoLogin(String userName, String sid) throws CxSoapLoginClientException {
        Credentials credentials = new Credentials();
        credentials.setUser(userName);
        credentials.setPass(sid);

        CxWSResponseLoginData response = cxSoapClient.ssoLogin(credentials, 1033);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.fatal("Failed to login :" + response.getErrorMessage());
            throw new CxSoapLoginClientException("Failed to login: " + response.getErrorMessage());
        }

        sessionId = response.getSessionId();
        return response;
    }

    public CxCLIWebServiceV1Soap getCxSoapClient() {
        return cxSoapClient;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}