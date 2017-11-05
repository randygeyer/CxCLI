package com.checkmarx.cxconsole.commands.exceptions;

/**
 * Created by nirli on 31/10/2017.
 */
public class CLICommandMissingParametersException extends CLICommandException {

    public CLICommandMissingParametersException() {
    }

    public CLICommandMissingParametersException(String message) {
        super(message);
    }

    public CLICommandMissingParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLICommandMissingParametersException(Throwable cause) {
        super(cause);
    }

    public CLICommandMissingParametersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
