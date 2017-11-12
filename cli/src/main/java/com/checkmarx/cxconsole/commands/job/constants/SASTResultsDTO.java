package com.checkmarx.cxconsole.commands.job.constants;

/**
 * Created by nirli on 12/11/2017.
 */
public class SASTResultsDTO {

    private int highVulnerabilityResult;
    private int mediumVulnerabilityResult;
    private int lowVulnerabilityResult;

    public SASTResultsDTO(String highVulnerabilityStr, String mediumVulnerabilityStr, String lowVulnerabilityStr) {
        if (highVulnerabilityStr != null) {
            highVulnerabilityResult = Integer.parseInt(highVulnerabilityStr);
        } else {
            highVulnerabilityResult = 0;
        }
        if (mediumVulnerabilityStr != null) {
            mediumVulnerabilityResult = Integer.parseInt(mediumVulnerabilityStr);
        } else {
            mediumVulnerabilityResult = 0;
        }
        if (lowVulnerabilityStr != null) {
            lowVulnerabilityResult = Integer.parseInt(lowVulnerabilityStr);
        } else {
            lowVulnerabilityResult = 0;
        }

    }

    public int getHighVulnerabilityResult() {
        return highVulnerabilityResult;
    }

    public int getMediumVulnerabilityResult() {
        return mediumVulnerabilityResult;
    }

    public int getLowVulnerabilityResult() {
        return lowVulnerabilityResult;
    }


}
