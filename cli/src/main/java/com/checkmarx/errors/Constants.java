package com.checkmarx.errors;

public class Constants {




    public class ErrorCodes {

        public static final int GENERAL_ERROR_CODE = 1;
        static final int SDLC_ERROR_CODE = 2;
        static final int NO_OSA_LICENSE_ERROR_CODE = 3;
        static final int LOGIN_ERROR_CODE = 4;
        static final int NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_CODE = 5;

    }

    public class ErrorMassages {

        public static final String GENERAL_ERROR_MSG = "General error occurred";
        static final String SDLC_ERROR_MSG = "This feature is available only on SDLC edition";
        static final String NO_OSA_LICENSE_ERROR_MSG = "Open Source Analysis License is not enabled for this project.Please contact your CxSAST Administrator";
        static final String LOGIN_ERROR_MSG = "Login Failed";
        static final String NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG = "OSA scan requires an existing project on the server";
    }
}