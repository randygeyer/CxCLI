package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.constants.Commands;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.commands.job.CxGenerateTokenJob;
import com.checkmarx.cxconsole.commands.utils.CommandParametersValidator;
import com.checkmarx.parameters.CLIScanParametersSingleton;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by nirli on 31/10/2017.
 */
class GenerateTokenCommand extends CLICommand {

    private static final String SERVER_STACK_TRACE_ERROR_MESSAGE = "com.checkmarx.cxconsole.commands.job.exceptions.CLITokenJobException: Fail to generate login token: Fail to authenticate: status code: HTTP/1.1 400 Bad Request. error:\"error\":\"invalid_grant\"";
    private static final String USER_AUTHENTICATION_ERROR = "User authentication failed, unable to generate token";

            GenerateTokenCommand(CLIScanParametersSingleton params) {
        super(params);
        this.commandName = Commands.GENERATE_TOKEN.value();
    }

    @Override
    protected int executeCommand() throws CLICommandException {
        CxGenerateTokenJob job = new CxGenerateTokenJob(params);

        Future<Integer> future = executor.submit(job);
        try {
            if (timeoutInSeconds != null) {
                exitCode = future.get(timeoutInSeconds, TimeUnit.SECONDS);
            } else {
                exitCode = future.get();
            }
        } catch (Exception e) {
            String errorMessage = e.getCause().getMessage();
            if (e.getMessage().contains(SERVER_STACK_TRACE_ERROR_MESSAGE)){
                errorMessage = e.getMessage().replace(SERVER_STACK_TRACE_ERROR_MESSAGE, USER_AUTHENTICATION_ERROR);
            }
            log.error("Error executing GenerateToken command, due to: " + errorMessage);
            throw new CLICommandException("Error executing GenerateToken command, due to: " + errorMessage);
        }
        return exitCode;
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
    public String getUsageExamples() {
        return "runCxConsole.cmd GenerateToken -CxServer http://localhost -cxuser admin@company -cxpassword admin -v";
    }

    @Override
    public void printHelp() {
        String helpHeader = "\nThe \"GenerateToken\" command allows to generate login token, to be used instead of username and password.";
        String helpFooter = "\nUsage example: " + getUsageExamples() + "\n\n(c) 2017 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.printHelp(120, getCommandName(), helpHeader, params.getCliMandatoryParameters().getGenerateTokenMandatoryParamsOptionGroup(), helpFooter, true);
    }
}