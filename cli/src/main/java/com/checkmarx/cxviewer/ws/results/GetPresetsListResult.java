//package com.checkmarx.cxviewer.ws.results;
//
//import com.checkmarx.cxviewer.ws.generated.ArrayOfPreset;
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//import com.checkmarx.cxviewer.ws.generated.CxWSResponsePresetList;
//import com.checkmarx.cxviewer.ws.generated.Preset;
//import com.checkmarx.login.soap.dto.PresetDTO;
//import org.jdom.Element;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class GetPresetsListResult extends SimpleResult {
//
//    private List<PresetDTO> presets;
//
//    private List<Preset> presetList;
//
//
//    public GetPresetsListResult() {
//        presets = new ArrayList<PresetDTO>();
//    }
//
//    @Override
//    protected void parseReturnValue(Element returnValueNode) {
//        if (presets == null) {
//            presets = new ArrayList<PresetDTO>();
//        }
//        if (presets.size() > 0) {
//            presets = new ArrayList<PresetDTO>();
//        }
//        for (Object childObj : returnValueNode.getChildren()) {
//            Element child = (Element) childObj;
//            presets.add(new PresetDTO(child.getAttribute("Id").getValue(), child.getAttribute("Name").getValue()));
//        }
//    }
//
//    @Override
//    public String toString() {
//        String result;
//        if (isSuccessfulResponse()) {
//            result = "" + this.getClass().getSimpleName() + "(presets.size():" + presets.size() + ", presets:" + presets + ")";
//        } else {
//            result = "" + this.getClass().getSimpleName() + "(Message:" + getErrorMessage() + ")";
//        }
//        return result;
//    }
//
//    @Override
//    protected void parseReturnValue(CxWSBasicResponse responseObject) {
//        if (responseObject instanceof CxWSResponsePresetList) {
//
//            CxWSResponsePresetList presetSetList = (CxWSResponsePresetList) responseObject;
//            ArrayOfPreset array = presetSetList.getPresetList();
//            if (array != null) {
//                presetList = array.getPreset();
//                for (Preset pres : presetList) {
//                    presets.add(new PresetDTO(pres.getID() + "",
//                            pres.getPresetName()));
//                }
//            }
//        } else {
//            throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
//        }
//    }
//
//    public List<PresetDTO> getPresetList() {
//        return presets;
//    }
//}
