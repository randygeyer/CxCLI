package com.checkmarx.parameters.utils;

import com.checkmarx.parameters.exceptions.CLIParameterParsingException;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by nirli on 30/10/2017.
 */
public class ParametersUtils {

    private ParametersUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getOptionalValue(CommandLine commandLine, String opt) {
        String ret = commandLine.getOptionValue(opt);
        if (ret == null && commandLine.hasOption(opt)) {
            ret = "";
        }
        return ret;
    }

    public static CommandLine parseArguments(String[] args, Options commandLineOptions) throws CLIParameterParsingException {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(commandLineOptions, args, true);
        } catch (ParseException e) {
            throw new CLIParameterParsingException("Error parsing the command line: " + e.getMessage());
        }
    }

    public static String setPrivateKeyFromLocation(String privateKeyLocation) throws CLIParameterParsingException {
        File keyFile = new File(privateKeyLocation);
        try (FileReader fileReader = new FileReader(keyFile);
             BufferedReader in = new BufferedReader(fileReader)) {
            String line;
            StringBuilder keyData = new StringBuilder();
            while ((line = in.readLine()) != null) {
                keyData.append(line);
                keyData.append("\n");
            }
            return keyData.toString();
        } catch (IOException ex) {
            throw new CLIParameterParsingException("Error set private key from private key location: " + ex.getMessage());
        }
    }


}
