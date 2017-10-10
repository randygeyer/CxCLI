package com.checkmarx.cxosa;


import com.checkmarx.login.rest.exception.CxClientException;
import org.apache.log4j.Logger;

/**
 * Created by: Dorg.
 * Date: 28/09/2016.
 */
public interface ScanWaitHandler<T> {

    void onStart(long startTime, long scanTimeoutInMin);

    void onIdle(T scanStatus) throws CxClientException;

    void onSuccess(T scanStatus);

    void onQueued(T scanStatus);

    void onFail(T scanStatus) throws CxClientException;

    void onTimeout(T scanStatus) throws CxClientException;

    void setLogger(Logger log);

}
