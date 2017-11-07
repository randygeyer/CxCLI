package com.checkmarx.exitcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.checkmarx.exitcodes.Constants.ErrorMassages.*;
import static com.checkmarx.exitcodes.Constants.ExitCodes.*;

public class ErrorHandler {

    private static Map<String, Integer> errorMsgToCodeMap = createMessageToCodeMap();
    private static Map<Integer, String> errorCodeToMsgMap = createCodeToMessageMap();

    public static int errorCodeResolver(String errorMsg) {
        if (errorMsg == null) {
            return GENERAL_ERROR_EXIT_CODE;
        }

        Integer retVal = null;
        for (Map.Entry<String, Integer> entry : errorMsgToCodeMap.entrySet()) {
            if (errorMsg.toLowerCase().contains(entry.getKey().toLowerCase())) {
                retVal = entry.getValue();
            }
        }
        return (retVal != null) ? retVal : GENERAL_ERROR_EXIT_CODE;
    }

    public static String errorMsgResolver(Integer errorCode) {
        String retVal = null;
        for (Map.Entry<Integer, String> entry : errorCodeToMsgMap.entrySet()) {
            if (Objects.equals(errorCode, entry.getKey())) {
                retVal = entry.getValue();
            }
        }
        return (retVal != null) ? retVal : GENERAL_ERROR_MSG;
    }

    private static Map<Integer, String> createCodeToMessageMap() {
        Map<Integer, String> codeToMessageMap = new HashMap<>();

        codeToMessageMap.put(LOGIN_FAILED_ERROR_EXIT_CODE, LOGIN_ERROR_MSG);
        codeToMessageMap.put(SDLC_ERROR_EXIT_CODE, SDLC_ERROR_MSG);
        codeToMessageMap.put(NO_OSA_LICENSE_ERROR_EXIT_CODE, NO_OSA_LICENSE_ERROR_MSG);
        codeToMessageMap.put(NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_EXIT_CODE, NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG);

        //Generic threshold
        codeToMessageMap.put(GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE, GENERIC_THRESHOLD_FAILURE_ERROR_MSG);
        //OSA thresholds
        codeToMessageMap.put(OSA_HIGH_THRESHOLD_ERROR_EXIT_CODE, OSA_HIGH_THRESHOLD_ERROR_MSG);
        codeToMessageMap.put(OSA_MEDIUM_THRESHOLD_ERROR_EXIT_CODE, OSA_MEDIUM_THRESHOLD_ERROR_MSG);
        codeToMessageMap.put(OSA_LOW_THRESHOLD_ERROR_EXIT_CODE, OSA_LOW_THRESHOLD_ERROR_MSG);
        //SAST thresholds
        codeToMessageMap.put(SAST_HIGH_THRESHOLD_ERROR_EXIT_CODE, SAST_HIGH_THRESHOLD_ERROR_MSG);
        codeToMessageMap.put(SAST_MEDIUM_THRESHOLD_ERROR_EXIT_CODE, SAST_MEDIUM_THRESHOLD_ERROR_MSG);
        codeToMessageMap.put(SAST_LOW_THRESHOLD_ERROR_EXIT_CODE, SAST_LOW_THRESHOLD_ERROR_MSG);

        return codeToMessageMap;
    }

    private static Map<String, Integer> createMessageToCodeMap() {
        Map<String, Integer> messageToCodeMap = new HashMap<>();

        messageToCodeMap.put(LOGIN_ERROR_MSG, LOGIN_FAILED_ERROR_EXIT_CODE);
        messageToCodeMap.put(UNSUCCESSFUL_LOGIN_ERROR_MSG, LOGIN_FAILED_ERROR_EXIT_CODE);
        messageToCodeMap.put(SDLC_ERROR_MSG, SDLC_ERROR_EXIT_CODE);
        messageToCodeMap.put(NO_OSA_LICENSE_ERROR_MSG, NO_OSA_LICENSE_ERROR_EXIT_CODE);
        messageToCodeMap.put(NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG, NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_EXIT_CODE);
        messageToCodeMap.put(REPORT_PARAMETER_IN_ASYNC_SCAN, GENERAL_ERROR_EXIT_CODE);
        messageToCodeMap.put(THRESHOLD_PARAMETER_IN_ASYNC_SCAN, GENERAL_ERROR_EXIT_CODE);

        //Generic threshold
        messageToCodeMap.put(GENERIC_THRESHOLD_FAILURE_ERROR_MSG, GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE);
        //OSA thresholds
        messageToCodeMap.put(OSA_HIGH_THRESHOLD_ERROR_MSG, OSA_HIGH_THRESHOLD_ERROR_EXIT_CODE);
        messageToCodeMap.put(OSA_MEDIUM_THRESHOLD_ERROR_MSG, OSA_MEDIUM_THRESHOLD_ERROR_EXIT_CODE);
        messageToCodeMap.put(OSA_LOW_THRESHOLD_ERROR_MSG, OSA_LOW_THRESHOLD_ERROR_EXIT_CODE);
        //SAST thresholds
        messageToCodeMap.put(SAST_HIGH_THRESHOLD_ERROR_MSG, SAST_HIGH_THRESHOLD_ERROR_EXIT_CODE);
        messageToCodeMap.put(SAST_MEDIUM_THRESHOLD_ERROR_MSG, SAST_MEDIUM_THRESHOLD_ERROR_EXIT_CODE);
        messageToCodeMap.put(SAST_LOW_THRESHOLD_ERROR_MSG, SAST_LOW_THRESHOLD_ERROR_EXIT_CODE);

        return messageToCodeMap;
    }
}