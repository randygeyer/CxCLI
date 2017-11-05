package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.constants.Commands;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.commands.job.CxGenerateTokenJob;
import com.checkmarx.cxconsole.utils.CommandParametersValidator;
import com.checkmarx.parameters.CLIScanParameters;
import org.apache.commons.cli.Options;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by nirli on 31/10/2017.
 */
public class GenerateTokenCommand extends CLICommand {

    public GenerateTokenCommand(CLIScanParameters params) {
        super(params);
        this.commandName = Commands.GENERATE_TOKEN.value();
    }

    @Override
    protected void executeCommand() throws CLICommandException {
        CxGenerateTokenJob job = new CxGenerateTokenJob(params, log);

        Future<Integer> future = executor.submit(job);
        try {
            if (timeoutInSeconds != null) {
                exitCode = future.get(timeoutInSeconds, TimeUnit.SECONDS);
            } else {
                exitCode = future.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new CLICommandException("Error executing GenerateToken command, due to: " + e.getMessage());
        }
    }

    @Override
    public void checkParameters() throws CLICommandParameterValidatorException {
        CommandParametersValidator.validateGenerateTokenParams(params);
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getMandatoryParams() {
        return params.getCliMandatoryParameters().getMandatoryParamsGenerateToken();
    }

    @Override
    public String getUsageExamples() {
        return "CxConsole GenerateToken -CxServer http://localhost -cxuser admin@company -cxpassword admin -v";
    }

    @Override
    public void printHelp() {
        String helpFooter = "\nUsage example: " + getUsageExamples() + "\n\n(c) 2014 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.printHelp(120, getCommandName(), HELP_HEADER, (Options) params.getCliMandatoryParameters().getGenerateTokenMandatoryParamsOptionGroup().getOptions(), helpFooter, true);
    }

    @Override
    public String getKeyDescriptions() {
        return params.getCliMandatoryParameters().getKeyDescriptionsGenerateToken();
    }

    @Override
    public String getOptionalParams() {
        return params.getCliSharedParameters().getParamLogFile() + "logFile";
    }

    @Override
    public String getOptionalKeyDescriptions() {
        return "[ " + params.getCliSharedParameters().getParamLogFile() + " logFile ]";
    }
}
