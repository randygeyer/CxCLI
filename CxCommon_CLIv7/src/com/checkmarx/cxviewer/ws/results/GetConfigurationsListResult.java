package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.data.ConfigurationsChild;
import com.checkmarx.cxviewer.ws.generated.ArrayOfConfigurationSet;
import com.checkmarx.cxviewer.ws.generated.ConfigurationSet;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseConfigSetList;

public class GetConfigurationsListResult extends SimpleResult {
	private List<ConfigurationsChild> configurations;

	
	private List<ConfigurationSet> configList;
	
	public List<ConfigurationsChild> getConfigurations() {
		return configurations;
	}

	public GetConfigurationsListResult() {
		configurations = new ArrayList<ConfigurationsChild>();
	}

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		if (configurations == null) {
			configurations = new ArrayList<ConfigurationsChild>();
		}
		if (configurations.size() > 0) {
			configurations = new ArrayList<ConfigurationsChild>();
		}
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			configurations.add(new ConfigurationsChild(child.getAttribute("Id").getValue(), child.getAttribute("Name")
					.getValue()));
		}
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(presets.size():" + configurations.size() + ", presets:"
					+ configurations + ")";
			// result = "" + this.getClass().getSimpleName() +
			// "(presets.size():" + presets.size() + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message:" + getErrorMessage() + ")";
		}
		return result;
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		if (responseObject instanceof CxWSResponseConfigSetList) {

			CxWSResponseConfigSetList cfgSetList = (CxWSResponseConfigSetList) responseObject;
			ArrayOfConfigurationSet array = cfgSetList.getConfigSetList();
			if (array != null) {
				configList = array.getConfigurationSet();
				for (ConfigurationSet set : configList) {
					configurations.add(new ConfigurationsChild("" + set.getID(),
							set.getConfigSetName()));
				}
			}
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}
	
	public List<ConfigurationSet> getConfigList() {
		return configList;
	}
}
