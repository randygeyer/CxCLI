package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponsePresetList;
import com.checkmarx.cxviewer.ws.generated.Preset;
import com.checkmarx.login.soap.dto.PresetDTO;
import com.checkmarx.login.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.login.soap.utils.SoapClientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 26/10/2017.
 */
class PresetProvider {

    private String sessionId;
    private CxCLIWebServiceV1Soap cxSoapClient;

    PresetProvider(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) {
        this.cxSoapClient = cxSoapClient;
        this.sessionId = sessionId;
    }

    List<PresetDTO> getPresetsList() {
        List<PresetDTO> presets = new ArrayList<>();

        CxWSResponsePresetList response = cxSoapClient.getPresetList(sessionId);
        try {
            SoapClientUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            CxLogger.getLogger().trace("Presets list response: " + e.getMessage());
        }
        for (Preset preset : response.getPresetList().getPreset()) {
            presets.add(new PresetDTO(preset.getID(), preset.getPresetName()));
        }

        return presets;
    }

}