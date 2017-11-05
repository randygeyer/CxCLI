package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseGroupList;
import com.checkmarx.login.soap.dto.TeamDTO;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.utils.SoapClientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 26/10/2017.
 */
class TeamProvider {

    private String sessionId;
    private CxCLIWebServiceV1Soap cxSoapClient;

    TeamProvider(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.cxSoapClient = cxSoapClient;
        this.sessionId = sessionId;
    }

    List<TeamDTO> getTeamsList() {
        List<TeamDTO> teams = new ArrayList<>();

        CxWSResponseGroupList response = cxSoapClient.getAssociatedGroupsList(sessionId);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            CxLogger.getLogger().trace("Teams list response: " + e.getMessage());
        }

        return teams;
    }
}

