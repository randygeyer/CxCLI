package com.checkmarx.clients.soap.sast.exceptions;

import com.checkmarx.clients.soap.exceptions.CxSoapClientException;

/**
 * Created by nirli on 29/10/2017.
 */
public class CxSoapSASTClientException extends CxSoapClientException {
    public CxSoapSASTClientException() {
    }

    public CxSoapSASTClientException(String message) {
        super(message);
    }

    public CxSoapSASTClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxSoapSASTClientException(Throwable cause) {
        super(cause);
    }

    public CxSoapSASTClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
