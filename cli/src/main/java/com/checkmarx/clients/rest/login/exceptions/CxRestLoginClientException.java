package com.checkmarx.clients.rest.login.exceptions;

import com.checkmarx.clients.rest.exceptions.CxRestClientException;

/**
 * Created by: Dorg.
 * Date: 15/09/2016.
 */
public class CxRestLoginClientException extends CxRestClientException {

    public CxRestLoginClientException() {
        super();
    }

    public CxRestLoginClientException(String message) {
        super(message);
    }

    public CxRestLoginClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestLoginClientException(Throwable cause) {
        super(cause);
    }


}
