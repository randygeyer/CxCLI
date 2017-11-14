package com.checkmarx.clients.soap.login.exceptions;

import com.checkmarx.clients.soap.exceptions.CxSoapClientException;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapLoginClientException extends CxSoapClientException {
    public CxSoapLoginClientException() {
    }

    public CxSoapLoginClientException(String message) {
        super(message);
    }

    public CxSoapLoginClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxSoapLoginClientException(Throwable cause) {
        super(cause);
    }

    public CxSoapLoginClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
