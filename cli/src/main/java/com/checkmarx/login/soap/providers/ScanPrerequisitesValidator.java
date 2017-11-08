package com.checkmarx.login.soap.providers;

import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.login.soap.dto.ConfigurationDTO;
import com.checkmarx.login.soap.dto.PresetDTO;
import com.checkmarx.login.soap.providers.exceptions.CLISoapProvidersException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by nirli on 26/10/2017.
 */
public class ScanPrerequisitesValidator {

    private static Logger log = Logger.getLogger("com.checkmarx.cxconsole.CxConsoleLauncher");

    private List<PresetDTO> presetsList;
    private List<ConfigurationDTO> configurationsList;

    public ScanPrerequisitesValidator(CxCLIWebServiceV1Soap cxSoapClient, String sessionId) throws CLISoapProvidersException {
        PresetProvider presetProvider = new PresetProvider(cxSoapClient, sessionId);
        presetsList = presetProvider.getPresetsList();

        ConfigurationProvider configurationProvider = new ConfigurationProvider(cxSoapClient, sessionId);
        configurationsList = configurationProvider.getConfigurationsList();
    }

    public PresetDTO validateJobPreset(String presetName) {
        PresetDTO selectedPreset = null;
        if (presetsList != null && presetName != null) {
            for (PresetDTO preset : presetsList) {
                if (preset.getName().equals(presetName)) {
                    selectedPreset = preset;
                    break;
                }
            }

            if (selectedPreset == null) {
                log.trace("Preset [" + presetName + "] is not found");
            }
        } else {
            if (presetsList != null && !presetsList.isEmpty()) {
                // Zero preset will be send. Server will decide what preset to use.
                selectedPreset = new PresetDTO(0, null);
            }
        }

        if (selectedPreset != null && selectedPreset.getName() != null) {
            log.trace("Preset [" + selectedPreset.getName() + "] is selected");
        }
        return selectedPreset;
    }

    public ConfigurationDTO validateJobConfiguration(String configurationName) {
        ConfigurationDTO selectedConfig = null;
        if (configurationName != null && configurationsList != null) {
            for (ConfigurationDTO config : configurationsList) {
                if (config.getName().equals(configurationName)) {
                    selectedConfig = config;
                    break;
                }
            }

            if (selectedConfig == null) {
                log.trace("Configuration [" + configurationName + "] is not found");
            }
        } else {
            selectedConfig = validateJobConfiguration("Default Configuration");
        }

        if (selectedConfig != null && selectedConfig.getName() != null) {
            log.trace("Configuration [" + selectedConfig.getName() + "] is selected");
        }
        return selectedConfig;
    }

}