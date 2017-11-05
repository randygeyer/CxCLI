package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.dto.PresetDTO;
import com.checkmarx.login.soap.dto.TeamDTO;

import java.util.List;

/**
 * Created by nirli on 26/10/2017.
 */
public class ScanPrerequisitesValidator {

    private CxCLIWebServiceV1Soap cxSoapClient;
    private String sessionId;

    private List<PresetDTO> presetList;
    private List<ConfigurationDTO> configurationList;
    private List<TeamDTO> teamList;

    public ScanPrerequisitesValidator(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.sessionId = sessionId;
        this.cxSoapClient = cxSoapClient;

        PresetProvider presetProvider = new PresetProvider(cxSoapClient, sessionId);
        presetList = presetProvider.getPresetsList();

        ConfigurationProvider configurationProvider = new ConfigurationProvider(cxSoapClient, sessionId);
        configurationList = configurationProvider.getConfigurationsList();

        TeamProvider teamProvider = new TeamProvider(cxSoapClient, sessionId);
        teamList = teamProvider.getTeamsList();
    }

    public List<PresetDTO> getPresetList() {
        return presetList;
    }

    public List<ConfigurationDTO> getConfigurationList() {
        return configurationList;
    }

    public List<TeamDTO> getTeamList() {
        return teamList;
    }
}