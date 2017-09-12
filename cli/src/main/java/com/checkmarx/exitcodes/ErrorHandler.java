package com.checkmarx.exitcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.checkmarx.exitcodes.Constants.ErrorMassages.*;
import static com.checkmarx.exitcodes.Constants.ExitCodes.*;

public class ErrorHandler {

    private static Map<String, Integer> errorMsgToCodeMap = new HashMap();
    private static Map<Integer, String> errorCodeToMsgMap = new HashMap();
    private static boolean isInitiated = false;

    public static int errorCodeResolver(String errorMsg) {
        if (!isInitiated) {
            initMaps();
            isInitiated = true;
        }

        if (errorMsg == null) {
            return GENERAL_ERROR_CODE;
        }

        Integer retVal = null;
        for (Map.Entry<String, Integer> entry : errorMsgToCodeMap.entrySet()) {
            if (errorMsg.toLowerCase().contains(entry.getKey().toLowerCase())) {
                retVal = entry.getValue();
            }
        }
        return (retVal != null) ? retVal : GENERAL_ERROR_CODE;
    }

    public static String errorMsgResolver(Integer errorCode) {
        if (!isInitiated) {
            initMaps();
            isInitiated = true;
        }

        String retVal = null;
        for (Map.Entry<Integer, String> entry : errorCodeToMsgMap.entrySet()) {
            if (Objects.equals(errorCode, entry.getKey())) {
                retVal = entry.getValue();
            }
        }
        return (retVal != null) ? retVal : GENERAL_ERROR_MSG;
    }

    private static void initMaps() {
        errorMsgToCodeMap.put(LOGIN_ERROR_MSG, LOGIN_FAILED_ERROR_CODE);
        errorCodeToMsgMap.put(LOGIN_FAILED_ERROR_CODE, LOGIN_ERROR_MSG);

        errorMsgToCodeMap.put(SDLC_ERROR_MSG, SDLC_ERROR_CODE);
        errorCodeToMsgMap.put(SDLC_ERROR_CODE, SDLC_ERROR_MSG);

        errorMsgToCodeMap.put(NO_OSA_LICENSE_ERROR_MSG, NO_OSA_LICENSE_ERROR_CODE);
        errorCodeToMsgMap.put(NO_OSA_LICENSE_ERROR_CODE, NO_OSA_LICENSE_ERROR_MSG);

        errorMsgToCodeMap.put(NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG, NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_CODE);
        errorCodeToMsgMap.put(NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_CODE, NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG);

        //Generic threshold
        errorMsgToCodeMap.put(GENERIC_THRESHOLD_FAILURE_ERROR_MSG, GENERIC_THRESHOLD_FAILURE_ERROR_CODE);
        errorCodeToMsgMap.put(GENERIC_THRESHOLD_FAILURE_ERROR_CODE, GENERIC_THRESHOLD_FAILURE_ERROR_MSG);

        //OSA thresholds
        errorMsgToCodeMap.put(OSA_HIGH_THRESHOLD_ERROR_MSG, OSA_HIGH_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(OSA_HIGH_THRESHOLD_ERROR_CODE, OSA_HIGH_THRESHOLD_ERROR_MSG);

        errorMsgToCodeMap.put(OSA_MEDIUM_THRESHOLD_ERROR_MSG, OSA_MEDIUM_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(OSA_MEDIUM_THRESHOLD_ERROR_CODE, OSA_MEDIUM_THRESHOLD_ERROR_MSG);

        errorMsgToCodeMap.put(OSA_LOW_THRESHOLD_ERROR_MSG, OSA_LOW_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(OSA_LOW_THRESHOLD_ERROR_CODE, OSA_LOW_THRESHOLD_ERROR_MSG);

        //SAST thresholds
        errorMsgToCodeMap.put(SAST_HIGH_THRESHOLD_ERROR_MSG, SAST_HIGH_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(SAST_HIGH_THRESHOLD_ERROR_CODE, SAST_HIGH_THRESHOLD_ERROR_MSG);

        errorMsgToCodeMap.put(SAST_MEDIUM_THRESHOLD_ERROR_MSG, SAST_MEDIUM_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(SAST_MEDIUM_THRESHOLD_ERROR_CODE, SAST_MEDIUM_THRESHOLD_ERROR_MSG);

        errorMsgToCodeMap.put(SAST_LOW_THRESHOLD_ERROR_MSG, SAST_LOW_THRESHOLD_ERROR_CODE);
        errorCodeToMsgMap.put(SAST_LOW_THRESHOLD_ERROR_CODE, SAST_LOW_THRESHOLD_ERROR_MSG);
    }
}