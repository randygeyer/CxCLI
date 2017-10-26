package com.checkmarx.login.rest.exception;

/**
 * Created by nirli on 25/10/2017.
 */
public class CxOSAClientException extends CxRestClientException {
    public CxOSAClientException() {
    }

    public CxOSAClientException(String message) {
        super(message);
    }

    public CxOSAClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxOSAClientException(Throwable cause) {
        super(cause);
    }

    public CxOSAClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
