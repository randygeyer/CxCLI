package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.login.rest.CxRestTokenClient;
import com.checkmarx.parameters.CLIScanParameters;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

public class CxGenerateTokenJob implements Callable<Integer> {

    private Logger log;

    private CLIScanParameters params;

    private CxRestTokenClient cxRestTokenClient;

    public CxGenerateTokenJob(CLIScanParameters params, Logger log) {
        this.params = params;
        this.log = log;
        cxRestTokenClient = new CxRestTokenClient();
    }

    @Override
    public Integer call() throws Exception {
        log.info("Trying to login to server: " + params.getCliMandatoryParameters().getOriginalHost());
        String token = cxRestTokenClient.generateToken(new URL(params.getCliMandatoryParameters().getOriginalHost()), params.getCliMandatoryParameters().getUsername(), params.getCliMandatoryParameters().getPassword());
        log.info("The requested token is: " + token);

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

}