package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.generated.ConfigurationSet;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseConfigSetList;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.utils.SoapClientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 26/10/2017.
 */
class ConfigurationProvider {

    private String sessionId;
    private  CxCLIWebServiceV1Soap cxSoapClient;

    ConfigurationProvider(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.cxSoapClient = cxSoapClient;
        this.sessionId = sessionId;
    }

    List<ConfigurationDTO> getConfigurationsList() {
        List<ConfigurationDTO> configurations = new ArrayList<>();

        CxWSResponseConfigSetList response = cxSoapClient.getConfigurationSetList(sessionId);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            CxLogger.getLogger().trace("Configurations list response: " + e.getMessage());
        }
        for (ConfigurationSet configurationSet : response.getConfigSetList().getConfigurationSet()) {
            configurations.add(new ConfigurationDTO(configurationSet.getID(), configurationSet.getConfigSetName()));
        }

        return configurations;
    }
}
