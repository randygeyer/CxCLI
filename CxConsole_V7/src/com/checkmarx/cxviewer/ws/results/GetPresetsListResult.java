package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.data.PresetChild;
import com.checkmarx.cxviewer.ws.generated.ArrayOfPreset;
import com.checkmarx.cxviewer.ws.generated.CxWSResponsePresetList;
import com.checkmarx.cxviewer.ws.generated.Preset;

public class GetPresetsListResult extends SimpleResult {
	
	private List<PresetChild> presets;
	
	private List<Preset> presetList;

	public List<PresetChild> getPresets() {
		return presets;
	}

	public GetPresetsListResult() {
		presets = new ArrayList<PresetChild>();
	}

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		if (presets == null) {
			presets = new ArrayList<PresetChild>();
		}
		if (presets.size() > 0) {
			presets = new ArrayList<PresetChild>();
		}
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			presets.add(new PresetChild(child.getAttribute("Id").getValue(), child.getAttribute("Name").getValue()));
		}
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(presets.size():" + presets.size() + ", presets:"+presets+")";
//			result = "" + this.getClass().getSimpleName() + "(presets.size():" + presets.size() + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message:" + getErrorMessage() + ")";
		}
		return result;
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		if (responseObject instanceof CxWSResponsePresetList) {

			CxWSResponsePresetList presetSetList = (CxWSResponsePresetList) responseObject;
			ArrayOfPreset array = presetSetList.getPresetList();
			if (array != null) {
				presetList = array.getPreset();
				for (Preset pres : presetList) {
					presets.add(new PresetChild(pres.getID() + "",
							pres.getPresetName()));
				}
			}
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}
	
	public List<Preset> getPresetList() {
		return presetList;
	}
}
