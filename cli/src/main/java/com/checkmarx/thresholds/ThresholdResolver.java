package com.checkmarx.thresholds;

import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.log4j.Logger;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;
import static com.checkmarx.exitcodes.Constants.ErrorMassages.*;
import static com.checkmarx.exitcodes.Constants.ExitCodes.*;

public class ThresholdResolver {

    protected static Logger log = Logger.getLogger(LOG_NAME);
    private static final int NO_THRESHOLD_EXCEEDED = 0;

    private ThresholdResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static int resolveThresholdExitCode(ThresholdDto thresholdDto) {
        int thresholdScore = NO_THRESHOLD_EXCEEDED;

        if (thresholdDto.getHighSeverityScanResult() > thresholdDto.getHighSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                log.info(SAST_HIGH_THRESHOLD_ERROR_MSG);
                thresholdScore = SAST_HIGH_THRESHOLD_ERROR_EXIT_CODE;
            } else {
                log.info(OSA_HIGH_THRESHOLD_ERROR_MSG);
                thresholdScore = OSA_HIGH_THRESHOLD_ERROR_EXIT_CODE;
            }
        }

        if (thresholdDto.getMediumSeverityScanResult() > thresholdDto.getMediumSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                log.info(SAST_MEDIUM_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = SAST_MEDIUM_THRESHOLD_ERROR_EXIT_CODE;
                }
            } else {
                log.info(OSA_MEDIUM_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = OSA_MEDIUM_THRESHOLD_ERROR_EXIT_CODE;
                }
            }
        }

        if (thresholdDto.getLowSeverityScanResult() > thresholdDto.getLowSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                log.info(SAST_LOW_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = SAST_LOW_THRESHOLD_ERROR_EXIT_CODE;
                }
            } else {
                log.info(OSA_LOW_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = OSA_LOW_THRESHOLD_ERROR_EXIT_CODE;
                }
            }
        }

        log.info("");
        return thresholdScore;
    }

}