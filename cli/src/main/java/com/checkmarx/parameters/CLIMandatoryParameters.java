package com.checkmarx.parameters;

import com.checkmarx.parameters.exceptions.CLIParameterParsingException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLIMandatoryParameters extends AbstractCLIScanParameters {

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private String host;
    private String originalHost;
    private String username;
    private String password;
    private String token;
    private String projectName;
    private String projectNameWithPath;
    private String srcPath;
    private String folderProjectName;
    private boolean hasPasswordParam = false;
    private boolean hasUserParam = false;
    private boolean hasTokenParam = false;

    private static final Option PARAM_HOST = Option.builder("cxserver").hasArg(true).argName("server").desc("IP address or resolvable name of CxSuite web server").build();
    private static final Option PARAM_USER = Option.builder("cxuser").hasArg(true).argName("username").desc("Login username. Mandatory, Unless token is used or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_PASSWORD = Option.builder("cxpassword").hasArg(true).argName("password").desc("Login password. Mandatory, Unless token is used or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_TOKEN = Option.builder("cxtoken").hasArg(true).argName("token").desc("Login token. Mandatory, Unless use rname and password are provided or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_PRJ_NAME = Option.builder("projectname").argName("project name").hasArg(true).desc("A full absolute name of a project. " +
            "The full Project name includes the whole path to the project, including Server, service provider, company, and team. " +
            "Example:  -ProjectName \"CxServer\\SP\\Company\\Users\\bs java\" " +
            "If project with such a name doesn't exist in the system, new project will be created.").build();


    CLIMandatoryParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initMandatoryParams(CommandLine parsedCommandLineArguments) {
        host = parsedCommandLineArguments.getOptionValue(PARAM_HOST.getOpt());
        originalHost = parsedCommandLineArguments.getOptionValue(PARAM_HOST.getOpt());
        username = parsedCommandLineArguments.getOptionValue(PARAM_USER.getOpt());
        password = parsedCommandLineArguments.getOptionValue(PARAM_PASSWORD.getOpt());
        token = parsedCommandLineArguments.getOptionValue(PARAM_TOKEN.getOpt());

        hasUserParam = parsedCommandLineArguments.hasOption(PARAM_USER.getOpt());
        hasPasswordParam = parsedCommandLineArguments.hasOption(PARAM_PASSWORD.getOpt());
        hasTokenParam = parsedCommandLineArguments.hasOption(PARAM_TOKEN.getOpt());

        projectNameWithPath = parsedCommandLineArguments.getOptionValue(PARAM_PRJ_NAME.getOpt());
        projectName = extractProjectName(projectNameWithPath);
    }

    private String extractProjectName(String projectNameWithFullPath) {
        if (projectNameWithFullPath != null) {
            projectNameWithFullPath = projectNameWithFullPath.replaceAll("/", "\\\\");
            String[] pathParts = projectNameWithFullPath.split("\\\\");
            if ((pathParts.length <= 0)) {
                return projectNameWithFullPath;
            } else {
                return pathParts[pathParts.length - 1];
            }
        }
        return null;
    }


    public String getHost() {
        return host;
    }

    public String getOriginalHost() {
        return originalHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectNameWithPath() {
        return projectNameWithPath;
    }

    public String getFolderProjectName() {
        return folderProjectName;
    }

    public boolean isHasPasswordParam() {
        return hasPasswordParam;
    }

    public boolean isHasUserParam() {
        return hasUserParam;
    }

    public boolean isHasTokenParam() {
        return hasTokenParam;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setOriginalHost(String originalHost) {
        this.originalHost = originalHost;
    }

    public String getSrcPath() {
        return srcPath;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_HOST);
        commandLineOptions.addOption(PARAM_PASSWORD);
        commandLineOptions.addOption(PARAM_PRJ_NAME);
        commandLineOptions.addOption(PARAM_TOKEN);
        commandLineOptions.addOption(PARAM_USER);
    }

    public OptionGroup getMandatoryParamsOptionGroup() {
        OptionGroup mandatoryParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            mandatoryParamsOptionGroup.addOption(opt);
        }

        return mandatoryParamsOptionGroup;
    }

    public Options getGenerateTokenMandatoryParamsOptionGroup() {
        Options mandatoryParamsOptions = new Options();
        mandatoryParamsOptions.addOption(PARAM_HOST);
        mandatoryParamsOptions.addOption(PARAM_USER);
        mandatoryParamsOptions.addOption(PARAM_PASSWORD);

        return mandatoryParamsOptions;
    }

    public Options getRevokeTokenMandatoryParamsOptions() {
        Options mandatoryParamsOptions = new Options();
        mandatoryParamsOptions.addOption(PARAM_HOST);
        mandatoryParamsOptions.addOption(PARAM_TOKEN);

        return mandatoryParamsOptions;
    }

    public String getKeyDescriptions() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(PARAM_HOST);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Host name of web-service. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_USER);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- User login name. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_PASSWORD);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Login password. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_TOKEN);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Login token. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_PRJ_NAME);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Full Project name. Mandatory\n");

        return keys.toString();
    }

    public String getKeyDescriptionsRevokeToken() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(PARAM_HOST);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Host name of web-service. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_TOKEN);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Login token. Mandatory\n");

        return keys.toString();
    }

    public String getKeyDescriptionsGenerateToken() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(PARAM_HOST);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Host name of web-service. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_USER);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- User login name. Mandatory\n");

        keys.append(leftSpacing);
        keys.append(PARAM_PASSWORD);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Login password. Mandatory\n");

        return keys.toString();
    }

    public String getMandatoryParams() {
        return PARAM_HOST + " hostName " + PARAM_USER + " login "
                + PARAM_PASSWORD + " password, or" + PARAM_TOKEN + " token. " + PARAM_PRJ_NAME + " fullProjectName ";
    }

    public String getMandatoryParamsGenerateToken() {
        return PARAM_HOST + " hostName " + PARAM_USER + " login "
                + PARAM_PASSWORD + " password ";
    }

    public String getMandatoryParamsRevokeToken() {
        return PARAM_HOST + " hostName " + PARAM_TOKEN + " token";
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }
}
