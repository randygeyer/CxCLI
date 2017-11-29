package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.constants.Commands;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.commands.job.CLISASTScanJob;
import com.checkmarx.cxconsole.commands.job.CLIScanJob;
import com.checkmarx.cxconsole.commands.utils.CommandParametersValidator;
import com.checkmarx.parameters.CLIScanParametersSingleton;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.checkmarx.cxconsole.commands.constants.Commands.ASYNC_OSA_SCAN;
import static com.checkmarx.cxconsole.commands.constants.Commands.OSA_SCAN;
import static com.checkmarx.exitcodes.Constants.ExitCodes.*;

/**
 * Created by nirli on 31/10/2017.
 */
class SASTScanCommand extends CLICommand {

    SASTScanCommand(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params);
        this.isAsyncScan = isAsyncScan;
        if (isAsyncScan) {
            commandName = Commands.ASYNC_SCAN.value();
        } else {
            commandName = Commands.SCAN.value();
        }
    }

    @Override
    protected int executeCommand() throws CLICommandException {
        CLIScanJob job = null;
        if (!isAsyncScan) {
            job = new CLISASTScanJob(params, false);
        } else {
            job = new CLISASTScanJob(params, true);
        }

        Future<Integer> future = executor.submit(job);
        try {
            if (timeoutInSeconds != null) {
                exitCode = future.get(timeoutInSeconds, TimeUnit.SECONDS);
            } else {
                exitCode = future.get();
            }
        } catch (Exception e) {
            log.error("Error executing SAST scan command: " + e.getCause().getMessage());
            throw new CLICommandException("Error executing SAST scan command: " + e.getCause().getMessage());
        }
        if (params.getCliSastParameters().isOsaEnabled()) {
            CLICommand osaCommand;
            if (isAsyncScan) {
                osaCommand = CommandFactory.getCommand(ASYNC_OSA_SCAN.value(), params);
            } else {
                osaCommand = CommandFactory.getCommand(OSA_SCAN.value(), params);
            }
            int osaScanExitCode = osaCommand.execute();
            if (osaScanExitCode >= OSA_HIGH_THRESHOLD_ERROR_EXIT_CODE && exitCode >= SAST_HIGH_THRESHOLD_ERROR_EXIT_CODE) {
                return GENERIC_THRESHOLD_FAILURE_ERROR_EXIT_CODE;
            } else if (osaScanExitCode != SCAN_SUCCEEDED_EXIT_CODE) {
                return osaScanExitCode;
            }
        }
        return exitCode;
    }

    @Override
    public void checkParameters() throws CLICommandParameterValidatorException {
        CommandParametersValidator.validateScanMandatoryParams(params);
        CommandParametersValidator.validateSASTLocationType(params);
        CommandParametersValidator.validateSASTExcludedFilesFolder(params);
        CommandParametersValidator.validatePrivateKeyLocationGITSVN(params);
        CommandParametersValidator.validateServiceProviderFolder(params);
        CommandParametersValidator.validateEnableOSA(params);
        if (isAsyncScan) {
            CommandParametersValidator.validateSASTAsyncScanParams(params);
        }
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getUsageExamples() {
        return "\n\nCxConsole Scan -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin@cx -cxpassword admin -locationtype folder -locationpath C:\\cx -preset All -incremental -reportpdf a.pdf\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype tfs -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ -preset default -reportxml a.xml -reportpdf b.pdf -incremental -forcescan\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype share -locationpath '\\\\storage\\path1;\\\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ -preset \"Sans 25\" -reportxls a.xls -reportpdf b.pdf -private -verbose -log a.log\n -LocationPathExclude test*, *log* -LocationFilesExclude web.config , *.class\n";
    }

    @Override
    public void printHelp() {
        String helpHeader = "\nThe \"Scan\" command allows to scan new and existing projects. It accepts all project settings as an arguments, similar to Web interface.";
        String helpFooter = "\nUsage example: " + getUsageExamples() + "\n\n(c) 2017 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.printHelp(120, getCommandName(), helpHeader, params.getAllCLIOptions(), helpFooter, true);
    }
}