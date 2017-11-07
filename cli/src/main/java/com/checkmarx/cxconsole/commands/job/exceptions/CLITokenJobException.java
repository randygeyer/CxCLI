package com.checkmarx.cxconsole.commands.job.exceptions;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLITokenJobException extends CLIJobException {

    public CLITokenJobException() {
    }

    public CLITokenJobException(String message) {
        super(message);
    }

    public CLITokenJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLITokenJobException(Throwable cause) {
        super(cause);
    }

    public CLITokenJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
