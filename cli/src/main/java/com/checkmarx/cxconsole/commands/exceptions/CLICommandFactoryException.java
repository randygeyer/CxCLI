package com.checkmarx.cxconsole.commands.exceptions;

/**
 * Created by nirli on 01/11/2017.
 */
public class CLICommandFactoryException extends CLICommandException {
    public CLICommandFactoryException() {
    }

    public CLICommandFactoryException(String message) {
        super(message);
    }

    public CLICommandFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLICommandFactoryException(Throwable cause) {
        super(cause);
    }

    public CLICommandFactoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
