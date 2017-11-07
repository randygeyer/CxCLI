package com.checkmarx.cxconsole.commands.job.exceptions;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIJobUtilException extends CLIJobException {
    public CLIJobUtilException() {
    }

    public CLIJobUtilException(String message) {
        super(message);
    }

    public CLIJobUtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLIJobUtilException(Throwable cause) {
        super(cause);
    }

    public CLIJobUtilException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
