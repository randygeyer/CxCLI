package com.checkmarx.cxconsole.commands.exceptions;

/**
 * Created by nirli on 31/10/2017.
 */
public class CLICommandException extends Exception {
    public CLICommandException() {
    }

    public CLICommandException(String message) {
        super(message);
    }

    public CLICommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLICommandException(Throwable cause) {
        super(cause);
    }

    public CLICommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
