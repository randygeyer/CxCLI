package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.cxconsole.commands.job.exceptions.CLIScanJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
import com.checkmarx.login.rest.CxRestLoginClient;
import com.checkmarx.login.rest.exceptions.CxRestLoginClientException;
import com.checkmarx.login.soap.CxSoapLoginClient;
import com.checkmarx.login.soap.exceptions.CxSoapLoginClientException;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import com.checkmarx.parameters.CLIScanParameters;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 05/11/2017.
 */
public class RetryableSOAPLogin extends RetryableOperation {

    private CxSoapLoginClient cxSoapLoginClient;
    private CLIScanParameters params;

    public RetryableSOAPLogin(CLIScanParameters parameters, CxSoapLoginClient cxSoapLoginClient) {
        this.cxSoapLoginClient = cxSoapLoginClient;
        this.params = parameters;
    }

    @Override
    protected void operation() throws CLIScanJobException {
        try {
            URL wsdlLocation = new URL(SoapClientUtils.buildHostWithWSDL(params.getCliMandatoryParameters().getOriginalHost()));
            cxSoapLoginClient.initSoapClient(wsdlLocation);
        } catch (CxSoapLoginClientException | MalformedURLException e) {
            throw new CLIScanJobException(e.getMessage());
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
            } else if (params.getCliMandatoryParameters().isHasUserParam() && params.getCliMandatoryParameters().isHasPasswordParam()) {
                //Login with user name and password
                responseLoginData = cxSoapLoginClient.login(params.getCliMandatoryParameters().getUsername(), params.getCliMandatoryParameters().getPassword());
                sessionId = responseLoginData.getSessionId();
            }
        } catch (CxSoapLoginClientException e) {
            error = "Unsuccessful login.\\n" + e.getMessage();
            log.trace(error);
            throw new CLIScanJobException(error);
        }

        if (sessionId == null) {
            String message = "Unsuccessful login.";
            if (responseLoginData != null) {
                message += ((responseLoginData.getErrorMessage() != null && !responseLoginData.getErrorMessage().isEmpty()) ? " Error message:" + responseLoginData.getErrorMessage() : "Login or password might be incorrect.");
            }
            throw new CLIScanJobException(message);
        }

        log.info("SOAP login was completed successfully");
        finished = true;
    }

    @Override
    public String getOperationName() {
        return "SOAP login";
    }
}