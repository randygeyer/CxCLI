package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.clients.rest.exceptions.CxRestClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLITokenJobException;
import com.checkmarx.parameters.CLIMandatoryParameters;
import com.checkmarx.parameters.CLIScanParametersSingleton;

import java.net.MalformedURLException;
import java.net.URL;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

public class CxRevokeTokenJob extends CLITokenJob {

    private CLIMandatoryParameters mandatoryParamsContainer;

    public CxRevokeTokenJob(CLIScanParametersSingleton params) {
        super(params);
        mandatoryParamsContainer = params.getCliMandatoryParameters();
    }

    @Override
    public Integer call() throws CLITokenJobException {
        log.info("Trying to login to server: " + params.getCliMandatoryParameters().getOriginalHost());
        try {
            cxRestTokenClient.revokeToken(new URL(mandatoryParamsContainer.getOriginalHost()), mandatoryParamsContainer.getToken());
        } catch (CxRestClientException | MalformedURLException e) {
            throw new CLITokenJobException("Fail to revoke login token(" + mandatoryParamsContainer.getToken() + "): " + e.getMessage());
        }
        log.info("The request to revoke token: " + mandatoryParamsContainer.getToken() + " , was completed successfully");

        return SCAN_SUCCEEDED_EXIT_CODE;
    }
}