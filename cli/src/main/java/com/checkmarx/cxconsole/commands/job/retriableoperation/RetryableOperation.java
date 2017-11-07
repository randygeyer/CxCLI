package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.cxconsole.commands.job.exceptions.CLIScanJobException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.Logger;

/**
 * Created by nirli on 05/11/2017.
 */
public abstract class RetryableOperation {

    protected Logger log = org.apache.log4j.Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");

    protected boolean finished = false;
    protected String error;

    protected RetryableOperation() {
    }

    public void run() throws CLIScanJobException {
        int retries = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        int count = 0;
        while (!finished) {
            try {
                operation();
            } catch (Exception e) {
                if (count >= retries) {
                    throw e;
                }
                count++;
                log.trace("Error occurred during Retryable operation", e);
                log.info("Error occurred during " + getOperationName() + ". Operation retry " + count);
            }
        }
    }

    protected abstract void operation() throws CLIScanJobException;

    public abstract String getOperationName();

    public String getError() {
        return error;
    }
}