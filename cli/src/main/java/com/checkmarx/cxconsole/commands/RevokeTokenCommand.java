package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.commands.job.CxRevokeTokenJob;
import com.checkmarx.cxconsole.logging.CxConsoleLoggerFactory;
import com.checkmarx.cxconsole.utils.CommandLineArgumentException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ScanParams;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.concurrent.*;

import static com.checkmarx.cxconsole.commands.GeneralScanCommand.PARAM_HOST;
import static com.checkmarx.cxconsole.commands.GeneralScanCommand.PARAM_TOKEN;
import static com.checkmarx.exitcodes.Constants.ErrorMassages.LOGIN_ERROR_MSG;

public class RevokeTokenCommand extends VerboseCommand {

    private final String command = Commands.REVOKE_TOKEN.value();
    private ScanParams scParams;
    private Integer timeout;

    public RevokeTokenCommand() {
        super();
        initCommandLineOptions();
    }

    private void initCommandLineOptions() {
        this.commandLineOptions.addOption(PARAM_HOST);
        this.commandLineOptions.addOption(PARAM_TOKEN);
        this.commandLineOptions.addOption(PARAM_LOG_FILE);
    }

    @Override
    protected void initLogging() throws IOException {
        String logPath = "";
//        if (commandLineArguments.hasOption(PARAM_LOG_FILE.getOpt())) {
//            logPath = getLogFileLocation();
//        }

        log = CxConsoleLoggerFactory.getLoggerFactory().getLogger(logPath);
    }

    @Override
    protected void releaseLog() {
        log.removeAllAppenders();
    }

    @Override
    protected String getLogFileLocation() {
        return null;
    }

    @Override
    public String getMandatoryParams() {
        return PARAM_HOST + " hostName " + PARAM_TOKEN + " token";
    }

    @Override
    public String getKeyDescriptions() {
        String leftSpacing = "  ";
        StringBuilder keys = new StringBuilder(leftSpacing);

        keys.append(PARAM_HOST);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Host name of web-service. Mandatory\n");

        keys.append(PARAM_TOKEN);
        keys.append(KEY_DESCR_INTEND_SMALL);
        keys.append("- Login token. Mandatory\n");

        return keys.toString();
    }

    @Override
    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        String header = "\nThe \"GenerateToken\" command allows to revoke and discard login token. It accepts only the server url, and login token.";
        String footer = "\n(c) 2014 CheckMarx.com LTD, All Rights Reserved\n";
        helpFormatter.setLeftPadding(4);
        helpFormatter.printHelp(120, getCommandName(), header, commandLineOptions, footer, true);
    }

    @Override
    public String getOptionalParams() {
        return "[ " + PARAM_LOG_FILE + " logFile.log ] ";
    }

    @Override
    public String getOptionalKeyDescriptions() {
        return null;
    }

    @Override
    public void resolveServerUrl() throws Exception {
        String generatedHost = null;
        try {
            generatedHost = ConfigMgr.getWSMgr().resolveServiceLocation(scParams.getHost());
        } catch (Exception e) {
            throw new Exception(LOGIN_ERROR_MSG + ": Cx server was not found on " + scParams.getHost());
        }
        if (!scParams.getOriginHost().contains("http")) {
            if (generatedHost.contains("https://")) {
                scParams.setOriginHost("https://" + scParams.getOriginHost());
            } else {
                scParams.setOriginHost("http://" + scParams.getOriginHost());
            }
        }
        scParams.setHost(generatedHost);
    }

    @Override
    public void parseArguments(String[] args) throws ParseException {
        super.parseArguments(args);  //  parseArguments initializes commandLineArguments
        scParams = new ScanParams(commandLineArguments);
    }

    @Override
    public String getCommandName() {
        return command;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void checkParameters() throws CommandLineArgumentException {
        if ((scParams.getOriginHost() == null) || (scParams.getToken() == null)) {
            throw new CommandLineArgumentException("For token revocation provide: " + PARAM_HOST.getOpt() + " and " + PARAM_TOKEN.getOpt());
        }
    }

    @Override
    protected boolean isKeyFlag(String key) {
        return false;
    }

    @Override
    public String getDescriptionString() {
        return null;
    }

    @Override
    public String getUsageExamples() {
        return null;
    }

    @Override
    protected void executeCommand() {
        try {
            initLogging();
        } catch (IOException e) {
            log.error("Error initiate the logger");
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CxRevokeTokenJob job = new CxRevokeTokenJob(scParams, log);

        Future<Integer> future = executor.submit(job);
        try {
            if (timeout != null) {
                errorCode = future.get(timeout, TimeUnit.SECONDS);
            } else {
                errorCode = future.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            releaseLog();
            executor.shutdownNow();
        }
    }
}