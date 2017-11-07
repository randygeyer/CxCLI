package com.checkmarx.cxconsole.commands.job.exceptions;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIJobException extends Exception {

    public CLIJobException() {
    }

    public CLIJobException(String message) {
        super(message);
    }

    public CLIJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLIJobException(Throwable cause) {
        super(cause);
    }

    public CLIJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
