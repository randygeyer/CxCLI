package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.utils.ScanParams;
import com.checkmarx.login.rest.CxTokenizeLogin;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.checkmarx.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

public class CxGenerateTokenJob implements Callable<Integer> {

    private Logger log;

    private ScanParams params;

    private CxTokenizeLogin cxTokenizeLogin;

    public CxGenerateTokenJob(ScanParams params, Logger log) {
        this.params = params;
        this.log = log;
        cxTokenizeLogin = new CxTokenizeLogin();
    }

    @Override
    public Integer call() throws Exception {
        log.info("Trying to login to server: " + params.getOriginHost());
        String token = cxTokenizeLogin.generateToken(new URL(params.getOriginHost()), params.getUser(), params.getPassword());
        log.info("The requested token is: " + token);

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

}