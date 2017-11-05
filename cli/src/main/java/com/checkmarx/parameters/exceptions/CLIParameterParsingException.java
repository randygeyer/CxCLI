package com.checkmarx.parameters.exceptions;

/**
 * Created by nirli on 30/10/2017.
 */
public class CLIParameterParsingException extends Exception {
    public CLIParameterParsingException() {
    }

    public CLIParameterParsingException(String message) {
        super(message);
    }

    public CLIParameterParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLIParameterParsingException(Throwable cause) {
        super(cause);
    }

    public CLIParameterParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
