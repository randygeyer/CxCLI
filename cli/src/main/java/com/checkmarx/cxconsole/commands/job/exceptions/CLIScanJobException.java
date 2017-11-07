package com.checkmarx.cxconsole.commands.job.exceptions;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIScanJobException extends CLIJobException {
    public CLIScanJobException() {
    }

    public CLIScanJobException(String message) {
        super(message);
    }

    public CLIScanJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLIScanJobException(Throwable cause) {
        super(cause);
    }

    public CLIScanJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
