package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.clients.soap.sast.CxSoapSASTClient;
import com.checkmarx.clients.soap.sast.exceptions.CxSoapSASTClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobUtilException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.CurrentStatusEnum;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseScanStatus;
import org.apache.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Callable;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

class WaitScanCompletionJob implements Callable<Boolean> {

    private CxSoapSASTClient cxSoapSASTClient;
    private String sessionId;
    private String runId;
    private long scanId;
    private boolean isAsyncScan = false;

    private Logger log = Logger.getLogger(LOG_NAME);

    WaitScanCompletionJob(CxSoapSASTClient cxSoapSASTClient, String sessionId, String scanId, boolean isAsyncScan) {
        super();
        this.cxSoapSASTClient = cxSoapSASTClient;
        this.sessionId = sessionId;
        this.runId = scanId;
        this.isAsyncScan = isAsyncScan;
    }

    @Override
    public Boolean call() throws CLIJobException {
        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);

        int getStatusInterval = ConfigMgr.getCfgMgr()
                .getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

        long currTime;
        long prevTime;
        long exceededTime;
        boolean scanComplete = false;
        int progressRequestAttempt = 0;

        try {
            do {
                currTime = System.currentTimeMillis();
                CxWSResponseScanStatus statusOfScanResult = cxSoapSASTClient.getStatusOfScan(runId, sessionId);
                log.trace("getScanStatus: " + statusOfScanResult);
                if (statusOfScanResult != null) {
                    // Update progress bar
                    int totalPercentScanned = statusOfScanResult.getTotalPercent();
                    CurrentStatusEnum currentStatusEnum = statusOfScanResult.getCurrentStatus();
                    log.info("Total scan worked: " + totalPercentScanned + "%");

                    if (currentStatusEnum.equals(CurrentStatusEnum.FAILED)) {
                        // Scan failed
                        log.error(statusOfScanResult.getErrorMessage());
                        throw new CLIJobUtilException(statusOfScanResult.getErrorMessage());
                    }

                    if (currentStatusEnum.equals(CurrentStatusEnum.CANCELED)) {
                        log.error("Project scan was cancelled on server side.");
                        throw new CLIJobUtilException("Project scan was cancelled on server side.");
                    }

                    if (currentStatusEnum.equals(CurrentStatusEnum.DELETED)) {
                        log.error("Project scan was deleted/postponed.");
                        throw new CLIJobUtilException("Project scan was deleted/postponed.");
                    }

                    String stageName = statusOfScanResult.getStageName();
                    String currentStage = "Current stage: ";
                    stageName = stageName.isEmpty() ? "" : " \"" + stageName + "\"";
                    if (!stageName.isEmpty() && !statusOfScanResult.getStageMessage().isEmpty()) {
                        log.info(currentStage + stageName + " - " + statusOfScanResult.getStageMessage());
                    } else if (!stageName.isEmpty()) {
                        log.info(currentStage + stageName);
                        if (isAsyncScan && Objects.equals(currentStatusEnum, CurrentStatusEnum.QUEUED)) {
                            return true;
                        }
                    } else if (!statusOfScanResult.getStageMessage().isEmpty()) {
                        log.info(currentStage + statusOfScanResult.getStageMessage());
                    } else {
                        log.info("Scan state: " + statusOfScanResult.getCurrentStatus());
                    }

                    log.trace(statusOfScanResult.getCurrentStatus() + stageName
                            + "\n" + statusOfScanResult.getStageMessage()
                            + " ("
                            + statusOfScanResult.getCurrentStagePercent()
                            + "%)" + " "
                            + statusOfScanResult.getStepMessage());
                    scanComplete = statusOfScanResult.getCurrentStatus().equals(CurrentStatusEnum.FINISHED);
                    scanId = statusOfScanResult.getScanId();
                    if (scanComplete && !statusOfScanResult.getStageMessage().isEmpty()) {
                        log.info(statusOfScanResult.getStageMessage());
                    }
                } else {
                    log.error("Scan status request failed. ");
                    progressRequestAttempt++;
                }

                if (progressRequestAttempt > retriesNum) {
                    if (statusOfScanResult != null && !statusOfScanResult.isIsSuccesfull()) {
                        String errorMsg = "Scan service error: progress request have not succeeded.";
                        String responseErrMsg = statusOfScanResult.getErrorMessage();
                        if (responseErrMsg != null && !responseErrMsg.isEmpty()) {
                            errorMsg += " " + responseErrMsg;
                        }
                        log.error(errorMsg);
                        throw new CLIJobException(errorMsg);
                    } else {
                        log.error("Scan progress request failure.");
                        throw new CLIJobException("Scan progress request failure.");
                    }
                } else {
                    if ((statusOfScanResult != null && !statusOfScanResult.isIsSuccesfull()) ||
                            (statusOfScanResult == null)) {
                        log.error("Performing another request. Attempt#" + progressRequestAttempt);
                    }
                }

                prevTime = currTime;
                currTime = System.currentTimeMillis();
                exceededTime = (currTime - prevTime) / 1000;
                //Check, maybe no need to wait, and another request should be sent
                while (exceededTime < getStatusInterval && !scanComplete) {
                    Thread.sleep(500);
                    currTime = System.currentTimeMillis();
                    exceededTime = (currTime - prevTime) / 1000;
                }
            } while (!scanComplete);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (CxSoapSASTClientException e) {
            log.error("Error occurred during retrieving scan status: " + e.getMessage());
            throw new CLIJobException("Error occurred during retrieving scan status: " + e.getMessage());
        }

        return scanComplete;
    }

    public long getScanId() {
        return scanId;
    }
}