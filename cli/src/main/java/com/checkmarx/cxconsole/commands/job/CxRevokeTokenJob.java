package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.login.rest.CxRestTokenClient;
import com.checkmarx.parameters.CLIScanParameters;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;


public class CxRevokeTokenJob implements Callable<Integer> {

    private Logger log;

    private CLIScanParameters params;

    private CxRestTokenClient cxRestTokenClient;

    public CxRevokeTokenJob(CLIScanParameters params, Logger log) {
        this.params = params;
        this.log = log;
        cxRestTokenClient = new CxRestTokenClient();
    }

    @Override
    public Integer call() throws Exception {
        log.info("Trying to login to server: " + params.getCliMandatoryParameters().getOriginalHost());
        cxRestTokenClient.revokeToken(new URL(params.getCliMandatoryParameters().getOriginalHost()), params.getCliMandatoryParameters().getToken());
        log.info("The request to revoke token: " + params.getCliMandatoryParameters().getToken() + " , was completed successfully");

        return SCAN_SUCCEEDED_EXIT_CODE;
    }
}