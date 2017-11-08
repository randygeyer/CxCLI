package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.commands.job.exceptions.CLITokenJobException;
import com.checkmarx.login.rest.CxRestTokenClient;
import com.checkmarx.parameters.CLIScanParameters;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 05/11/2017.
 */
abstract class CLITokenJob implements Callable<Integer> {

    protected CxRestTokenClient cxRestTokenClient;

    protected Logger log = Logger.getLogger(LOG_NAME);

    protected CLIScanParameters params;

    CLITokenJob(CLIScanParameters params) {
        this.params = params;
        cxRestTokenClient = new CxRestTokenClient();
    }

    @Override
    public abstract Integer call() throws CLITokenJobException;

}
