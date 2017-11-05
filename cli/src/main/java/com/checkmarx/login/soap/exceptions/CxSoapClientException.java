package com.checkmarx.login.soap.exceptions;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapClientException extends Exception{
    public CxSoapClientException() {
    }

    public CxSoapClientException(String message) {
        super(message);
    }

    public CxSoapClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxSoapClientException(Throwable cause) {
        super(cause);
    }

    public CxSoapClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
