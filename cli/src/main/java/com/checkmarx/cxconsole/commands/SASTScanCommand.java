package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.constants.Commands;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.commands.job.CxCLIScanJob;
import com.checkmarx.cxconsole.commands.job.CxScanJob;
import com.checkmarx.cxconsole.utils.CommandParametersValidator;
import com.checkmarx.parameters.CLIScanParameters;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by nirli on 31/10/2017.
 */
public class SASTScanCommand extends CLICommand {

    public SASTScanCommand(CLIScanParameters params, boolean isAsyncScan) {
        super(params);
        this.isAsyncScan = isAsyncScan;
        if (isAsyncScan) {
            commandName = Commands.ASYNC_SCAN.value();
        } else {
            commandName = Commands.SCAN.value();
        }
    }

    @Override
    protected void executeCommand() throws CLICommandException {
        CxScanJob job = null;
        if (!isAsyncScan) {
            job = new CxCLIScanJob(params, false);
        } else {
            job = new CxCLIScanJob(params, true);
        }
        job.setLog(log);

        Future<Integer> future = executor.submit(job);
        try {
            if (timeoutInSeconds != null) {
                exitCode = future.get(timeoutInSeconds, TimeUnit.SECONDS);
            } else {
                exitCode = future.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
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
    public String getMandatoryParams() {
        return params.getCliSastParameters().getMandatoryParams();
    }

    @Override
    public String getKeyDescriptions() {
        return params.getCliSastParameters().getKeyDescriptions();
    }

    @Override
    public String getOptionalParams() {
        return params.getCliSastParameters().getOptionalParams();
    }

    @Override
    public String getOptionalKeyDescriptions() {
        return params.getCliSastParameters().getOptionalKeyDescriptions();
    }

    @Override
    public String getUsageExamples() {
        return "\n\nCxConsole Scan -Projectname SP\\Cx\\Engine\\AST -CxServer http://localhost -cxuser admin@cx -cxpassword admin -locationtype folder -locationpath C:\\cx -preset All -incremental -reportpdf a.pdf\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype tfs -locationurl http://vsts2003:8080 -locationuser dm\\matys -locationpassword XYZ -preset default -reportxml a.xml -reportpdf b.pdf -incremental -forcescan\n"
                + "CxConsole Scan -projectname SP\\Cx\\Engine\\AST -cxserver http://localhost -cxuser admin@cx -cxpassword admin -locationtype share -locationpath '\\\\storage\\path1;\\\\storage\\path2' -locationuser dm\\matys -locationpassword XYZ -preset \"Sans 25\" -reportxls a.xls -reportpdf b.pdf -private -verbose -log a.log\n -LocationPathExclude test*, *log* -LocationFilesExclude web.config , *.class\n";
    }

    @Override
    public void printHelp() {
        String helpFooter = "\nUsage example: " + getUsageExamples() + "\n\n(c) 2017 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.printHelp(120, getCommandName(), HELP_HEADER, params.getAllCLIOptions(), helpFooter, true);
    }
}
