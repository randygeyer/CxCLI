package com.checkmarx.login.rest.exceptions;

/**
 * Created by nirli on 25/10/2017.
 */
public class CxRestClientValidatorException extends CxRestClientException {
    public CxRestClientValidatorException() {
    }

    public CxRestClientValidatorException(String message) {
        super(message);
    }

    public CxRestClientValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestClientValidatorException(Throwable cause) {
        super(cause);
    }

    public CxRestClientValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
