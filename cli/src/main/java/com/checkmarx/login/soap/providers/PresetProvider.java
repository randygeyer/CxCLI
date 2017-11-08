package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponsePresetList;
import com.checkmarx.cxviewer.ws.generated.Preset;
import com.checkmarx.login.soap.dto.PresetDTO;
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
class PresetProvider {

    private static Logger log = Logger.getLogger(LOG_NAME);

    private String sessionId;
    private CxCLIWebServiceV1Soap cxSoapClient;

    PresetProvider(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.cxSoapClient = cxSoapClient;
        this.sessionId = sessionId;
    }

    List<PresetDTO> getPresetsList() throws CLISoapProvidersException{
        List<PresetDTO> presets = new ArrayList<>();

        CxWSResponsePresetList response = cxSoapClient.getPresetList(sessionId);
        try {
            log.info("Read preset settings");
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.trace("Presets list response: " + e.getMessage());
            throw new CLISoapProvidersException("Presets list response: " + e.getMessage());
        }
        for (Preset preset : response.getPresetList().getPreset()) {
            presets.add(new PresetDTO(preset.getID(), preset.getPresetName()));
        }

        log.trace("Succeeded get Presets from server");
        return presets;
    }

}