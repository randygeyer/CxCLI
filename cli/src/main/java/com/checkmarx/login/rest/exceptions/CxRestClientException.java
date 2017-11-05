package com.checkmarx.login.rest.exceptions;

/**
 * Created by nirli on 25/10/2017.
 */
public class CxRestClientException extends Exception {
    public CxRestClientException() {
    }

    public CxRestClientException(String message) {
        super(message);
    }

    public CxRestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestClientException(Throwable cause) {
        super(cause);
    }

    public CxRestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
