package com.checkmarx.parameters;

import com.checkmarx.parameters.exceptions.CLIParameterParsingException;
import com.checkmarx.parameters.utils.ParametersUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by nirli on 30/10/2017.
 */
public class CLIScanParameters {

    private CLIMandatoryParameters cliMandatoryParameters;
    private CLISharedParameters cliSharedParameters;
    private CLISASTParameters cliSastParameters;
    private CLIOSAParameters cliOsaParameters;

    private CommandLine parsedCommandLineArguments;

    public CLIScanParameters(String[] args) throws CLIParameterParsingException {
        cliMandatoryParameters = new CLIMandatoryParameters();
        cliSharedParameters = new CLISharedParameters();
        cliSastParameters = new CLISASTParameters();
        cliOsaParameters = new CLIOSAParameters();

        parsedCommandLineArguments = ParametersUtils.parseArguments(args, getAllCLIOptions());
        cliMandatoryParameters.initMandatoryParams(parsedCommandLineArguments);
        cliSharedParameters.initSharedParams(parsedCommandLineArguments);
        cliSastParameters.initSastParams(parsedCommandLineArguments, cliSharedParameters.getLocationType());
        cliOsaParameters.initOsaParams(parsedCommandLineArguments);
        if (parsedCommandLineArguments.getArgs().length > 0) {
            throw new CLIParameterParsingException("Error complete parse all parameters from the command.");
        }

    }

    public CLIMandatoryParameters getCliMandatoryParameters() {
        return cliMandatoryParameters;
    }

    public CLISharedParameters getCliSharedParameters() {
        return cliSharedParameters;
    }

    public CLISASTParameters getCliSastParameters() {
        return cliSastParameters;
    }

    public CLIOSAParameters getCliOsaParameters() {
        return cliOsaParameters;
    }

    public CommandLine getParsedCommandLineArguments() {
        return parsedCommandLineArguments;
    }

    public Options getAllCLIOptions() {
        Options allParamsOption = new Options();
        for (Option opt : cliMandatoryParameters.getMandatoryParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliSharedParameters.getSharedParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliSastParameters.getSASTScanParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliOsaParameters.getOSAScanParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }

        return allParamsOption;
    }

}