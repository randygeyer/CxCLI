package com.checkmarx.cxconsole.commands.exceptions;

/**
 * Created by nirli on 31/10/2017.
 */
public class CLICommandParameterValidatorException extends CLICommandException {

    public CLICommandParameterValidatorException() {
    }

    public CLICommandParameterValidatorException(String message) {
        super(message);
    }

    public CLICommandParameterValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLICommandParameterValidatorException(Throwable cause) {
        super(cause);
    }

    public CLICommandParameterValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
