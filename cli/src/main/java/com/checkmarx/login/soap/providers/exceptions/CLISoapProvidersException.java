package com.checkmarx.login.soap.providers.exceptions;

import com.checkmarx.login.soap.exceptions.CxSoapClientException;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLISoapProvidersException extends CxSoapClientException {

    public CLISoapProvidersException() {
    }

    public CLISoapProvidersException(String message) {
        super(message);
    }

    public CLISoapProvidersException(String message, Throwable cause) {
        super(message, cause);
    }

    public CLISoapProvidersException(Throwable cause) {
        super(cause);
    }

    public CLISoapProvidersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
