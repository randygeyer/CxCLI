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
        return "\n\nrunCxConsole.cmd OsaScan -v -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin -cxpassword admin -osaLocationPath C:\\cx  -OsaFilesExclude *.class OsaPathExclude src,temp  \n"
                + "runCxConsole.cmd  OsaScan -v -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin -cxpassword admin -locationtype folder -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ  -OsaReportPDF -\n"
                + "runCxConsole.cmd  OsaScan -v -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin -cxpassword admin -locationtype shared -locationpath '\\storage\\path1;\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ  -OsaReportPDF -OsaReportHTML -log a.log\n \n"
                + "runCxConsole.cmd  OsaScan -v -Projectname CxServer\\SP\\Company\\my project -CxServer http://localhost -cxuser admin -cxpassword admin -locationtype folder -locationpath C:\\Users\\some_project -OsaFilesExclude *.bat -OsaReportPDF";
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
        if (scParams.getOsaLocationPath() == null || (scParams.getLocationType() != LocationType.folder && scParams.getLocationType() != LocationType.shared)) {
            throw new CommandLineArgumentException("For OSA Scan ("+Commands.OSASCAN.value()+"), provide  "+PARAM_OSA_LOCATION_PATH.getOpt()+"  or "+ PARAM_LOCATION_TYPE.getOpt() +" ( values: folder/shared)");
        }
    }

    private Options getOsaOptionsOnly(Options all) {
        Options osaOnly = new Options();
        osaOnly.addOption(all.getOption(PARAM_HOST.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_USER.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_PASSWORD.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_PRJ_NAME.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_LOCATION_PATH.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOCATION_TYPE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOCATION_PATH.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_LOG_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_EXCLUDE_FILES.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_EXCLUDE_FOLDERS.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_HTML_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_PDF_FILE.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_OSA_JSON.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_USE_SSO.getOpt()));
        osaOnly.addOption(all.getOption(PARAM_VERBOSE.getOpt()));

        return osaOnly;
    }
}