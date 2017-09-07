package com.checkmarx.errors;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.checkmarx.errors.Constants.ErrorCodes.*;
import static com.checkmarx.errors.Constants.ErrorMassages.GENERAL_ERROR_MSG;

public class ErrorHandler {

    private static Map<String, Integer> errorMsgToCodeMap = new HashMap();
    private static Map<Integer, String> errorCodeToMsgMap = new HashMap();
    private static boolean isInitiated = false;

    public static int errorCodeResolver(String errorMsg) {
        if (!isInitiated) {
            initMaps();
            isInitiated = true;
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
        errorMsgToCodeMap.put(Constants.ErrorMassages.LOGIN_ERROR_MSG, LOGIN_ERROR_CODE);
        errorCodeToMsgMap.put(LOGIN_ERROR_CODE, Constants.ErrorMassages.LOGIN_ERROR_MSG);

        errorMsgToCodeMap.put(Constants.ErrorMassages.SDLC_ERROR_MSG, SDLC_ERROR_CODE);
        errorCodeToMsgMap.put(SDLC_ERROR_CODE, Constants.ErrorMassages.SDLC_ERROR_MSG);

        errorMsgToCodeMap.put(Constants.ErrorMassages.NO_OSA_LICENSE_ERROR_MSG, NO_OSA_LICENSE_ERROR_CODE);
        errorCodeToMsgMap.put(NO_OSA_LICENSE_ERROR_CODE, Constants.ErrorMassages.NO_OSA_LICENSE_ERROR_MSG);

        errorMsgToCodeMap.put(Constants.ErrorMassages.NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG, NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_CODE);
        errorCodeToMsgMap.put(NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_CODE, Constants.ErrorMassages.NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG);
    }
}