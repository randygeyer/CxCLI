package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.clients.rest.login.CxRestLoginClient;
import com.checkmarx.clients.rest.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.parameters.CLIScanParametersSingleton;

/**
 * Created by nirli on 06/11/2017.
 */
public class RetryableRESTLogin extends RetryableOperation {

    private CxRestLoginClient cxRestLoginClient;
    private CLIScanParametersSingleton params;

    public RetryableRESTLogin(CLIScanParametersSingleton parameters, CxRestLoginClient cxRestLoginClient) {
        this.cxRestLoginClient = cxRestLoginClient;
        this.params = parameters;
    }


    @Override
    protected void operation() throws CLIJobException {
        log.trace("");
        log.info("Logging into the Checkmarx service.");

        // Login
        try {
            if (params.getCliMandatoryParameters().isHasUserParam() && params.getCliMandatoryParameters().isHasPasswordParam()) {
                cxRestLoginClient.credentialsLogin();
            } else if (params.getCliMandatoryParameters().isHasTokenParam()) {
                cxRestLoginClient.tokenLogin();
            }

            if (cxRestLoginClient.getRestLoginResponseDTO() == null) {
                throw new CLIJobException("Unsuccessful login.");
            }
        } catch (CxRestLoginClientException e) {
            throw new CLIJobException("Unsuccessful login.");
        }

        log.info("REST login was completed successfully");
        finished = true;
    }

    @Override
    public String getOperationName() {
        return "REST login";
    }
}
