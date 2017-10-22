package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.login.rest.CxTokenizeLogin;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;


public class CxRevokeTokenJob implements Callable<Integer> {

    private Logger log;

    private ScanParams params;

    private CxTokenizeLogin cxTokenizeLogin;

    public CxRevokeTokenJob(ScanParams params, Logger log) {
        this.params = params;
        this.log = log;
        cxTokenizeLogin = new CxTokenizeLogin();
    }

    @Override
    public Integer call() throws Exception {
        log.info("Trying to login to server: " + params.getOriginHost());
        cxTokenizeLogin.revokeToken(new URL(params.getOriginHost()), params.getToken());
        log.info("The request to revoke token: " + params.getToken() + " , was completed successfully");

        return SCAN_SUCCEEDED_EXIT_CODE;
    }
}