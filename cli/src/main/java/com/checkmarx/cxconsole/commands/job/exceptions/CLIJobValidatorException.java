package com.checkmarx.cxconsole.commands.job.exceptions;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIJobValidatorException extends CLIJobException {

    public CLIJobValidatorException() {
    }

    public CLIJobValidatorException(String message) {
        super(message);
    }

    public CLIJobValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLIJobValidatorException(Throwable cause) {
        super(cause);
    }

    public CLIJobValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
