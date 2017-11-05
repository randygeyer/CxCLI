package com.checkmarx.cxosa;


import com.checkmarx.cxosa.dto.OSAScanStatus;
import com.checkmarx.cxosa.dto.OSAScanStatusEnum;
import com.checkmarx.login.rest.exceptions.CxRestOSAClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import static com.checkmarx.cxosa.dto.OSAScanStatusEnum.QUEUED;


/**
 * Created by: Dorg.
 * Date: 28/09/2016.
 */
public class OSAConsoleScanWaitHandler implements ScanWaitHandler<OSAScanStatus> {

    private static Logger log;
    private long startTime;
    private long scanTimeoutInMin;

    public void onTimeout(OSAScanStatus scanStatus) throws CxRestOSAClientException {

        String status = scanStatus.getStatus() == null ? OSAScanStatusEnum.NONE.uiValue() : scanStatus.getStatus().uiValue();
        throw new CxRestOSAClientException("OSA scan has reached the time limit (" + scanTimeoutInMin + " minutes). status: [" + status + "]");

    }

    public void onFail(OSAScanStatus scanStatus) throws CxRestOSAClientException {
        throw new CxRestOSAClientException("OSA scan cannot be completed. status [" + scanStatus.getStatus().uiValue() + "]. message: [" + StringUtils.defaultString(scanStatus.getMessage()) + "]");

    }

    public void onIdle(OSAScanStatus scanStatus) {

        long hours = (System.currentTimeMillis() - startTime) / 3600000;
        long minutes = ((System.currentTimeMillis() - startTime) % 3600000) / 60000;
        long seconds = ((System.currentTimeMillis() - startTime) % 60000) / 1000;

        String hoursStr = (hours < 10) ? ("0" + Long.toString(hours)) : (Long.toString(hours));
        String minutesStr = (minutes < 10) ? ("0" + Long.toString(minutes)) : (Long.toString(minutes));
        String secondsStr = (seconds < 10) ? ("0" + Long.toString(seconds)) : (Long.toString(seconds));

        log.info("Waiting for OSA Scan Results. " +
                "Time Elapsed: " + hoursStr + ":" + minutesStr + ":" + secondsStr + ". " +
                "Status: " + scanStatus.getStatus().uiValue());

    }

    public void onSuccess(OSAScanStatus scanStatus) {
        log.debug("OSA Scan Finished.");
    }

    public void onQueued(OSAScanStatus scanStatus) {
        log.debug("OSA Scan Queued.");
        scanStatus.setStatus(QUEUED);
        scanStatus.setLink(null);
        scanStatus.setMessage("Osa scan queued");
    }

    public void onStart(long startTime, long scanTimeoutInMin) {
        this.startTime = startTime;
        this.scanTimeoutInMin = scanTimeoutInMin;
    }

    public void setLogger(Logger log) {
        OSAConsoleScanWaitHandler.log = log;
    }


}
