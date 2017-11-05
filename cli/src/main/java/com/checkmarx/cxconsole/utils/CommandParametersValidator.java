package com.checkmarx.cxconsole.utils;

import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.parameters.CLIScanParameters;

import java.io.File;

/**
 * Created by nirli on 31/10/2017.
 */
public class CommandParametersValidator {

    private CommandParametersValidator() {
        throw new IllegalStateException("Utility class");
    }

    private static final String MSG_ERR_SSO_WINDOWS_SUPPORT = "SSO login method is available only on Windows";
    private static final String MSG_ERR_MISSING_AUTHENTICATION_PARAMETERS = "Missing authentication parameters, please provide user name and password or token";
    private static final String MSG_ERR_2_AUTHENTICATION_METHODS = "Please provide only one authentication type: user name and password or token";
    private static final String MSG_ERR_MISSING_LOCATION_TYPE = "Missing locationType parameter";

    private static final String MSG_ERR_FOLDER_NOT_EXIST = "Specified source folder does not exist.";

    private static final String MSG_ERR_EXCLUDED_DIR = "Excluded folders list is invalid.";
    private static final String MSG_ERR_EXCLUDED_FILES = "Excluded files list is invalid.";


    public static void validateGenerateTokenParams(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().isSsoLoginUsed() && !isWindows()) {
            throw new CLICommandParameterValidatorException(MSG_ERR_SSO_WINDOWS_SUPPORT);
        }

        if ((parameters.getCliMandatoryParameters().getOriginalHost() == null) ||
                (parameters.getCliMandatoryParameters().getUsername() == null) ||
                (parameters.getCliMandatoryParameters().getPassword() == null)) {
            throw new CLICommandParameterValidatorException("For token generation please provide: server, username and password");
        }
    }

    public static void validateRevokeTokenParams(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().isSsoLoginUsed() && !isWindows()) {
            throw new CLICommandParameterValidatorException(MSG_ERR_SSO_WINDOWS_SUPPORT);
        }

        if ((parameters.getCliMandatoryParameters().getOriginalHost() == null) || (parameters.getCliMandatoryParameters().getToken() == null)) {
            throw new CLICommandParameterValidatorException("For token revocation please provide: server and token");
        }
    }

    public static void validateScanMandatoryParams(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().isSsoLoginUsed() && !isWindows()) {
            throw new CLICommandParameterValidatorException(MSG_ERR_SSO_WINDOWS_SUPPORT);
        } else if ((!parameters.getCliMandatoryParameters().isHasUserParam() || !parameters.getCliMandatoryParameters().isHasPasswordParam()) && !parameters.getCliMandatoryParameters().isHasTokenParam()) {
            throw new CLICommandParameterValidatorException(MSG_ERR_MISSING_AUTHENTICATION_PARAMETERS);
        } else if ((parameters.getCliMandatoryParameters().isHasUserParam() || parameters.getCliMandatoryParameters().isHasPasswordParam()) && parameters.getCliMandatoryParameters().isHasTokenParam()) {
            throw new CLICommandParameterValidatorException(MSG_ERR_2_AUTHENTICATION_METHODS);
        }

        if (parameters.getCliMandatoryParameters().getOriginalHost() == null || parameters.getCliMandatoryParameters().getHost() == null) {
            throw new CLICommandParameterValidatorException("Please provide server");
        }
    }

    public static void validateSASTExcludedFilesFolder(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().isHasExcludedFoldersParam()) {
            String[] excludedFolders = parameters.getCliSastParameters().getExcludedFolders();
            if (excludedFolders == null || excludedFolders.length == 0) {
                throw new CLICommandParameterValidatorException(MSG_ERR_EXCLUDED_DIR);
            }
        }

        if (parameters.getCliSastParameters().isHasExcludedFilesParam()) {
            String[] excludedFiles = parameters.getCliSastParameters().getExcludedFiles();
            if (excludedFiles == null || excludedFiles.length == 0) {
                throw new CLICommandParameterValidatorException(MSG_ERR_EXCLUDED_FILES);
            }
        }
    }

    public static void validateOSAExcludedFilesFolder(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliOsaParameters().isHasOsaExcludedFoldersParam()) {
            String[] osaExcludedFolders = parameters.getCliOsaParameters().getOsaExcludedFolders();
            if (osaExcludedFolders == null || osaExcludedFolders.length == 0) {
                throw new CLICommandParameterValidatorException(MSG_ERR_EXCLUDED_DIR);
            }
        }

        if (parameters.getCliOsaParameters().isHasOsaExcludedFilesParam()) {
            String[] osaExcludedFiles = parameters.getCliOsaParameters().getOsaExcludedFiles();
            if (osaExcludedFiles == null || osaExcludedFiles.length == 0) {
                throw new CLICommandParameterValidatorException(MSG_ERR_EXCLUDED_FILES);
            }
        }
    }

    public static void validatePrivateKeyLocationGITSVN(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationPrivateKey() != null
                && parameters.getCliSharedParameters().getLocationType() != null
                && (parameters.getCliSharedParameters().getLocationType() == LocationType.git || parameters.getCliSharedParameters().getLocationType() == LocationType.svn)) {
            File keyFile = new File(parameters.getCliSastParameters().getLocationPrivateKey().trim());
            if (!keyFile.exists()) {
                throw new CLICommandParameterValidatorException("Private key file is not found in: " +
                        parameters.getCliSastParameters().getLocationPrivateKey());
            }
            if (keyFile.isDirectory()) {
                throw new CLICommandParameterValidatorException("Private key file is a folder: " +
                        parameters.getCliSastParameters().getLocationPrivateKey());
            }
        }
    }

    public static void validateOSALocationType(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliOsaParameters().getOsaLocationPath() == null &&
                (parameters.getCliSharedParameters().getLocationType() != LocationType.folder &&
                        parameters.getCliSharedParameters().getLocationType() != LocationType.shared)) {
            throw new CLICommandParameterValidatorException("For OSA Scan (OsaScan), provide  OsaLocationPath  or locationType (values: folder/shared)");
        }
    }

    public static void validateSASTLocationType(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().getLocationType() == null) {
            throw new CLICommandParameterValidatorException(MSG_ERR_MISSING_LOCATION_TYPE);
        }

        switch (parameters.getCliSharedParameters().getLocationType().toString().toLowerCase()) {
            case ("folder"):
                validateFolder(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                break;
            case ("shared"):
                validateShared(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                break;
            case ("tfs"):
                validateTFS(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                validateLocationPort(parameters);
                break;
            case ("svn"):
                validateSVN(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                validateLocationPort(parameters);
                break;
            case ("perforce"):
                validatePerforce(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                break;
            case ("git"):
                validateGIT(parameters);
                validateWorkspaceParameterOnlyInPerforce(parameters);
                break;
        }
    }

    public static void validateServiceProviderFolder(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().getSpFolderName() != null) {
            File projectDir = new File(parameters.getCliSharedParameters().getSpFolderName().trim());
            if (!projectDir.exists()) {
                throw new CLICommandParameterValidatorException(MSG_ERR_FOLDER_NOT_EXIST + "["
                        + parameters.getCliSharedParameters().getSpFolderName() + "]");
            }

            if (!projectDir.isDirectory()) {
                throw new CLICommandParameterValidatorException(MSG_ERR_FOLDER_NOT_EXIST + "["
                        + parameters.getCliSharedParameters().getSpFolderName() + "]");
            }
        }
    }

    public static void validateEnableOSA(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().isOsaEnabled() &&
                (parameters.getCliSharedParameters().getLocationPath() == null ||
                        (parameters.getCliSharedParameters().getLocationType() != LocationType.folder && parameters.getCliSharedParameters().getLocationType() != LocationType.shared))) {
            throw new CLICommandParameterValidatorException("For OSA Scan with EnableOsa parameter, provide  locationPath  or locationType ( values: folder/shared)");
        }
    }

    public static void validateSASTAsyncScanParams(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getReportFile() != null ||
                parameters.getCliSastParameters().getXmlFile() != null ||
                parameters.getCliSastParameters().getReportType() != null) {
            throw new CLICommandParameterValidatorException("Asynchronous run does not allow report creation. Please remove the report parameters and run again");
        }
        if (parameters.getCliSastParameters().getSastHighThresholdValue() != Integer.MAX_VALUE ||
                parameters.getCliSastParameters().getSastMediumThresholdValue() != Integer.MAX_VALUE ||
                parameters.getCliSastParameters().getSastLowThresholdValue() != Integer.MAX_VALUE) {
            throw new CLICommandParameterValidatorException("Asynchronous run does not support threshold. Please remove the threshold parameters and run again");
        }
    }

    public static void validateOSAAsyncScanParams(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliOsaParameters().getOsaReportHTML() != null ||
                parameters.getCliOsaParameters().getOsaReportPDF() != null ||
                parameters.getCliOsaParameters().getOsaJson() != null) {
            throw new CLICommandParameterValidatorException("Asynchronous run does not allow report creation. Please remove the report parameters and run again");
        }

        if (parameters.getCliOsaParameters().getOsaHighThresholdValue() != Integer.MAX_VALUE ||
                parameters.getCliOsaParameters().getOsaMediumThresholdValue() != Integer.MAX_VALUE ||
                parameters.getCliOsaParameters().getOsaLowThresholdValue() != Integer.MAX_VALUE) {
            throw new CLICommandParameterValidatorException("Asynchronous run does not support threshold. Please remove the threshold parameters and run again");
        }
    }

    private static boolean isWindows() {
        return (System.getProperty("os.name").contains("Windows"));
    }

    private static void validateFolder(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().getLocationPath() == null) {
            throw new CLICommandParameterValidatorException("locationPath parameter is not specified. Required when locationType parameter is folder");
        }
    }

    private static void validateShared(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().getLocationPath() == null) {
            throw new CLICommandParameterValidatorException("locationPath is not specified. Required when locationType is shared");
        }

        if (parameters.getCliSastParameters().getLocationUser() == null) {
            throw new CLICommandParameterValidatorException("locationUser is not specified. Required when locationType is shared");
        }

        if (parameters.getCliSastParameters().getLocationPassword() == null) {
            throw new CLICommandParameterValidatorException("locationPassword is not specified. Required when locationType is shared");
        }
    }

    private static void validateTFS(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationPassword() == null) {
            throw new CLICommandParameterValidatorException("locationPassword is not specified. Required when locationType is TFS");
        }

        if (parameters.getCliSastParameters().getLocationURL() == null) {
            throw new CLICommandParameterValidatorException("locationURL is not specified. Required when locationType is TFS");
        }

        if (parameters.getCliSastParameters().getLocationUser() == null) {
            throw new CLICommandParameterValidatorException("locationUser is not specified. Required when locationType is TFS");
        }

        if (parameters.getCliSharedParameters().getLocationPath() == null) {
            throw new CLICommandParameterValidatorException("locationPath is not specified. Required when locationType is TFS");
        }
    }

    private static void validateSVN(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationURL() == null) {
            throw new CLICommandParameterValidatorException("locationURL is not specified. Required when locationType is SVN");
        }

        if (parameters.getCliSharedParameters().getLocationPath() == null) {
            throw new CLICommandParameterValidatorException("locationPath is not specified. Required when locationType is SVN");
        }
    }

    private static void validateGIT(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationURL() == null) {
            throw new CLICommandParameterValidatorException("locationURL is not specified. Required when locationType is GIT");
        }

        if (parameters.getCliSastParameters().getLocationBranch() == null) {
            throw new CLICommandParameterValidatorException("locationBranch is not specified. Required when locationType is GIT");
        }
    }

    private static void validatePerforce(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationURL() == null) {
            throw new CLICommandParameterValidatorException("locationURL is not specified. Required when locationType is Perforce");
        }

        if (parameters.getCliSastParameters().getLocationUser() == null) {
            throw new CLICommandParameterValidatorException("locationUser is not specified. Required when locationType is Perforce");
        }

        if (parameters.getCliSharedParameters().getLocationPath() == null) {
            throw new CLICommandParameterValidatorException("locationPath is not specified. Required when locationType is Perforce");
        }
    }

    private static void validateWorkspaceParameterOnlyInPerforce(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSharedParameters().getLocationType() != null &&
                parameters.getCliSharedParameters().getLocationType() != LocationType.perforce && parameters.getCliSastParameters().isPerforceWorkspaceMode()) {
            throw new CLICommandParameterValidatorException("WorkspaceMode parameter should be specified only when locationType is Perforce");
        }
    }

    private static void validateLocationPort(CLIScanParameters parameters) throws CLICommandParameterValidatorException {
        if (parameters.getCliSastParameters().getLocationPort() == null) {
            throw new CLICommandParameterValidatorException("Invalid location port");
        }
    }
}