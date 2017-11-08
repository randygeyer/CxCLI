package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.ws.generated.ConfigurationSet;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseConfigSetList;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.providers.exceptions.CLISoapProvidersException;
import com.checkmarx.login.soap.utils.SoapClientUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 26/10/2017.
 */
class ConfigurationProvider {

    private static Logger log = Logger.getLogger(LOG_NAME);

    private String sessionId;
    private  CxCLIWebServiceV1Soap cxSoapClient;

    ConfigurationProvider(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.cxSoapClient = cxSoapClient;
        this.sessionId = sessionId;
    }

    List<ConfigurationDTO> getConfigurationsList() throws CLISoapProvidersException {
        List<ConfigurationDTO> configurations = new ArrayList<>();

        CxWSResponseConfigSetList response = cxSoapClient.getConfigurationSetList(sessionId);
        try {
            log.info("Read configuration settings");
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.trace("Configurations list response: " + e.getMessage());
            throw new CLISoapProvidersException("Configurations list response: " + e.getMessage());
        }
        for (ConfigurationSet configurationSet : response.getConfigSetList().getConfigurationSet()) {
            configurations.add(new ConfigurationDTO(configurationSet.getID(), configurationSet.getConfigSetName()));
        }

        log.trace("Succeeded get configurations from server");
        return configurations;
    }
}
