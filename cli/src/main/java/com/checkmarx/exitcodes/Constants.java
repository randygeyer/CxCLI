package com.checkmarx.exitcodes;

public class Constants {

    public class ExitCodes {

        public static final int SCAN_SUCCEEDED_EXIT_CODE = 0;
        public static final int GENERAL_ERROR_EXIT_CODE = 1;
        public static final int SDLC_ERROR_EXIT_CODE = 2;
        public static final int NO_OSA_LICENSE_ERROR_EXIT_CODE = 3;
        public static final int LOGIN_FAILED_ERROR_EXIT_CODE = 4;
        public static final int NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_EXIT_CODE = 5;

        public static final int SAST_HIGH_THRESHOLD_ERROR_EXIT_CODE = 10;
        public static final int SAST_MEDIUM_THRESHOLD_ERROR_EXIT_CODE = 11;
        public static final int SAST_LOW_THRESHOLD_ERROR_EXIT_CODE = 12;
        public static final int OSA_HIGH_THRESHOLD_ERROR_EXIT_CODE = 13;
        public static final int OSA_MEDIUM_THRESHOLD_ERROR_EXIT_CODE = 14;
        public static final int OSA_LOW_THRESHOLD_ERROR_EXIT_CODE = 15;
        public static final int GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE = 19;
    }

    public class ErrorMassages {

        public static final String GENERAL_ERROR_MSG = "General error occurred";
        public static final String SDLC_ERROR_MSG = "This feature is available only on SDLC edition";
        public static final String NO_OSA_LICENSE_ERROR_MSG = "Open Source Analysis License is not enabled for this project.Please contact your CxSAST Administrator";
        public static final String LOGIN_ERROR_MSG = "Login Failed";
        public static final String NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG = "OSA scan requires an existing project on the server";
        public static final String REPORT_PARAMETER_IN_ASYNC_SCAN = "Asynchronous run does not allow report creation. Please remove the report parameters and run again";
        public static final String THRESHOLD_PARAMETER_IN_ASYNC_SCAN = "Asynchronous run does not support threshold. Please remove the threshold parameters and run again";

        public static final String SAST_HIGH_THRESHOLD_ERROR_MSG = "SAST high vulnerability threshold exceeded";
        public static final String SAST_MEDIUM_THRESHOLD_ERROR_MSG = "SAST medium vulnerability threshold exceeded";
        public static final String SAST_LOW_THRESHOLD_ERROR_MSG = "SAST low vulnerability threshold exceeded";
        public static final String OSA_HIGH_THRESHOLD_ERROR_MSG = "OSA high vulnerability threshold exceeded";
        public static final String OSA_MEDIUM_THRESHOLD_ERROR_MSG = "OSA medium vulnerability threshold exceeded";
        public static final String OSA_LOW_THRESHOLD_ERROR_MSG = "OSA low vulnerability threshold exceeded";
        public static final String GENERIC_THRESHOLD_FAILURE_ERROR_MSG = "SAST and OSA vulnerabilities threshold exceeded";
    }
}