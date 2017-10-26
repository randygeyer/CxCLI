package com.checkmarx.login.rest.exception;

/**
 * Created by: Dorg.
 * Date: 15/09/2016.
 */
public class CxLoginClientException extends CxRestClientException {

    public CxLoginClientException() {
        super();
    }

    public CxLoginClientException(String message) {
        super(message);
    }

    public CxLoginClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxLoginClientException(Throwable cause) {
        super(cause);
    }


}
