package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.clients.soap.login.CxSoapLoginClient;
import com.checkmarx.clients.soap.login.exceptions.CxSoapLoginClientException;
import com.checkmarx.clients.soap.utils.SoapClientUtils;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
import com.checkmarx.parameters.CLIScanParametersSingleton;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 05/11/2017.
 */
public class RetryableSOAPLogin extends RetryableOperation {

    private CxSoapLoginClient cxSoapLoginClient;
    private CLIScanParametersSingleton params;

    public RetryableSOAPLogin(CLIScanParametersSingleton parameters, CxSoapLoginClient cxSoapLoginClient) {
        this.cxSoapLoginClient = cxSoapLoginClient;
        this.params = parameters;
    }

    @Override
    protected void operation() throws CLIJobException {
        try {
            URL wsdlLocation = new URL(SoapClientUtils.buildHostWithWSDL(params.getCliMandatoryParameters().getOriginalHost()));
            cxSoapLoginClient.initSoapClient(wsdlLocation);
        } catch (CxSoapLoginClientException | MalformedURLException e) {
            throw new CLIJobException(e.getMessage());
        }

        log.trace("");
        log.info("Logging into the Checkmarx service.");

        // Login
        CxWSResponseLoginData responseLoginData = null;
        String sessionId = null;
        try {
            if (JobUtils.isWindows() && params.getCliSharedParameters().isSsoLoginUsed()) {
                //SSO login
                responseLoginData = cxSoapLoginClient.ssoLogin("", "");
                sessionId = responseLoginData.getSessionId();
            } else if (params.getCliMandatoryParameters().isHasUserParam() && params.getCliMandatoryParameters().isHasPasswordParam()) {
                //Login with user name and password
                responseLoginData = cxSoapLoginClient.login(params.getCliMandatoryParameters().getUsername(), params.getCliMandatoryParameters().getPassword());
                sessionId = responseLoginData.getSessionId();
            }
        } catch (CxSoapLoginClientException e) {
            error = "Unsuccessful login: " + e.getMessage();
            log.trace(error);
            throw new CLIJobException(error);
        }

        if (sessionId == null) {
            String message = "Unsuccessful login.";
            if (responseLoginData != null) {
                message += ((responseLoginData.getErrorMessage() != null && !responseLoginData.getErrorMessage().isEmpty()) ? " Error message:" + responseLoginData.getErrorMessage() : "Login or password might be incorrect.");
            }
            throw new CLIJobException(message);
        }

        log.info("SOAP login was completed successfully");
        finished = true;
    }

    @Override
    public String getOperationName() {
        return "SOAP login";
    }
}