package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.LocationType;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class OsaScanCommand extends ScanCommand {

    public static String COMMAND_SCAN = Commands.OSASCAN.value();


    public OsaScanCommand() {
        super();
    }

    @Override
    public String getCommandName() {
        return COMMAND_SCAN;
    }

    @Override
    public String getUsageExamples() {
        return "\n\nrunCxConsole.cmd OsaScan -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin -cxpassword admin -locationType folder -locationpath C:\\cx  -OsaFilesExclude *.class OsaPathExclude src,temp -verbose \n"
                + "runCxConsole.cmd  OsaScan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin -cxpassword admin -locationtype folder -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ  -OsaReportPDF -\n"
                + "runCxConsole.cmd  OsaScan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin -cxpassword admin -locationtype shared -locationpath '\\storage\\path1;\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ  -OsaReportPDF -OsaReportHTML -verbose -log a.log\n \n"
                + "runCxConsole.cmd  OsaScan -Projectname CxServer\\SP\\Company\\my project -CxServer http://localhost -cxuser admin -cxpassword admin -locationtype folder -locationpath C:\\Users\\some_project -OsaFilesExclude *.bat -OsaReportPDF -v";
    }

    @Override
    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        String header = "\nThe \"OsaScan\" command allows to scan existing projects for OSA. It accepts all project settings as an arguments, similar to the Web interface.";
        String footer = "\n(c) 2017 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.setLeftPadding(4);
        Options osaOnly = getOsaOptionsOnly(commandLineOptions);
        helpFormatter.printHelp(120, getCommandName(), header, osaOnly, footer, true);

    }

    @Override
    public void checkParameters() throws CommandLineArgumentException {
        super.checkParameters();
        if (scParams.getLocationType() != LocationType.folder && scParams.getLocationType() != LocationType.shared) {
            throw new CommandLineArgumentException("When running " + getCommandName() + ", the " + PARAM_LOCATION_TYPE.getOpt() + " should be folder/shared and the " + PARAM_LOCATION_PATH.getOpt() + " should be specified as well");
        }
    }

    private Options getOsaOptionsOnly(Options all) {
        Options osaOnly = new Options();
        osaOnly.addOption(all.getOption(PARAM_HOST.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_USER.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_PASSWORD.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_PRJ_NAME.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOCATION_TYPE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOCATION_PATH.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOG_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_EXCLUDE_FILES.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_EXCLUDE_FOLDERS.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_HTML_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_PDF_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_USE_SSO.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_VERBOSE.getOpt()));

        return osaOnly;
    }
}