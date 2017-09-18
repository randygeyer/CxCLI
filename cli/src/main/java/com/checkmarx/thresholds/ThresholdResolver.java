package com.checkmarx.thresholds;

import com.checkmarx.thresholds.dto.ThresholdDto;
import org.apache.log4j.Logger;

import static com.checkmarx.exitcodes.Constants.ErrorMassages.*;
import static com.checkmarx.exitcodes.Constants.ExitCodes.*;

public class ThresholdResolver {

    private static final int NO_THRESHOLD_EXCEEDED = 0;

    public static int resolveThresholdExitCode(ThresholdDto thresholdDto, Logger logger) {
        int thresholdScore = NO_THRESHOLD_EXCEEDED;

        if (thresholdDto.getHighSeverityScanResult() > thresholdDto.getHighSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                logger.info(SAST_HIGH_THRESHOLD_ERROR_MSG);
                thresholdScore = SAST_HIGH_THRESHOLD_ERROR_CODE;
            } else {
                logger.info(OSA_HIGH_THRESHOLD_ERROR_MSG);
                thresholdScore = OSA_HIGH_THRESHOLD_ERROR_CODE;
            }
        }

        if (thresholdDto.getMediumSeverityScanResult() > thresholdDto.getMediumSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                logger.info(SAST_MEDIUM_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = SAST_MEDIUM_THRESHOLD_ERROR_CODE;
                }
            } else {
                logger.info(OSA_MEDIUM_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = OSA_MEDIUM_THRESHOLD_ERROR_CODE;
                }
            }
        }

        if (thresholdDto.getLowSeverityScanResult() > thresholdDto.getLowSeverityThreshold()) {
            if (thresholdDto.getScanType() == ThresholdDto.ScanType.SAST_SCAN) {
                logger.info(SAST_LOW_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = SAST_LOW_THRESHOLD_ERROR_CODE;
                }
            } else {
                logger.info(OSA_LOW_THRESHOLD_ERROR_MSG);
                if (thresholdScore == NO_THRESHOLD_EXCEEDED) {
                    thresholdScore = OSA_LOW_THRESHOLD_ERROR_CODE;
                }
            }
        }
        return thresholdScore;
    }

}