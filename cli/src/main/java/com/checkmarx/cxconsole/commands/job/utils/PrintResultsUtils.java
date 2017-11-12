package com.checkmarx.cxconsole.commands.job.utils;

import com.checkmarx.cxconsole.cxosa.dto.OSASummaryResults;
import org.apache.log4j.Logger;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 06/11/2017.
 */
public class PrintResultsUtils {

    private static final int LOW_VULNERABILITY_RESULTS = 0;
    private static final int MEDIUM_VULNERABILITY_RESULTS = 1;
    private static final int HIGH_VULNERABILITY_RESULTS = 2;

    private static final String LINE_SPACER = "------------------------";
    private static final String RESULT_FOOTER = "-----------------------------------------------------------------------------------------";

    protected static Logger log = Logger.getLogger(LOG_NAME);

    private PrintResultsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void printOSAResultsToConsole(OSASummaryResults osaSummaryResults, String osaProjectSummaryLink) {
        log.info("----------------------------Checkmarx Scan Results(CxOSA):-------------------------------");
        log.info("");
        log.info(LINE_SPACER);
        log.info("OSA vulnerabilities Summary:");
        log.info(LINE_SPACER);
        log.info("OSA high severity results: " + osaSummaryResults.getTotalHighVulnerabilities());
        log.info("OSA medium severity results: " + osaSummaryResults.getTotalMediumVulnerabilities());
        log.info("OSA low severity results: " + osaSummaryResults.getTotalLowVulnerabilities());
        log.info("Vulnerability score: " + osaSummaryResults.getVulnerabilityScore());
        log.info("");
        log.info(LINE_SPACER);
        log.info("Libraries Scan Results:");
        log.info(LINE_SPACER);
        log.info("Open-source libraries: " + osaSummaryResults.getTotalLibraries());
        log.info("Vulnerable and outdated: " + osaSummaryResults.getVulnerableAndOutdated());
        log.info("Vulnerable and updated: " + osaSummaryResults.getVulnerableAndUpdated());
        log.info("Non-vulnerable libraries: " + osaSummaryResults.getNonVulnerableLibraries());
        log.info("");
        log.info("");
        log.info("OSA scan results location: " + osaProjectSummaryLink);
        log.info(RESULT_FOOTER);
    }

    public static void printSASTResultsToConsole(int[] scanResults) {
        log.info("----------------------------Checkmarx Scan Results(CxSAST):-------------------------------");
        log.info("");
        log.info(LINE_SPACER);
        log.info("SAST vulnerabilities Summary:");
        log.info(LINE_SPACER);
        log.info("SAST high severity results: " + scanResults[HIGH_VULNERABILITY_RESULTS]);
        log.info("SAST medium severity results: " + scanResults[MEDIUM_VULNERABILITY_RESULTS]);
        log.info("SAST low severity results: " + scanResults[LOW_VULNERABILITY_RESULTS]);
        log.info("");
        log.info(RESULT_FOOTER);
    }

}
