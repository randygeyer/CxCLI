package com.checkmarx.cxosa;


import com.checkmarx.login.rest.exceptions.CxRestOSAClientException;
import org.apache.log4j.Logger;

/**
 * Created by: Dorg.
 * Date: 28/09/2016.
 */
public interface ScanWaitHandler<T> {

    void onStart(long startTime, long scanTimeoutInMin);

    void onIdle(T scanStatus) throws CxRestOSAClientException;

    void onSuccess(T scanStatus);

    void onQueued(T scanStatus);

    void onFail(T scanStatus) throws CxRestOSAClientException;

    void onTimeout(T scanStatus) throws CxRestOSAClientException;

    void setLogger(Logger log);

}
