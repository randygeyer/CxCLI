package com.checkmarx.clients.soap.exceptions;

/**
 * Created by nirli on 29/10/2017.
 */
public class CxSoapClientValidatorException extends CxSoapClientException {
    public CxSoapClientValidatorException() {
    }

    public CxSoapClientValidatorException(String message) {
        super(message);
    }

    public CxSoapClientValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxSoapClientValidatorException(Throwable cause) {
        super(cause);
    }

    public CxSoapClientValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
