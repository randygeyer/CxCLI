package com.checkmarx.cxviewer.ws.data;

public class ConfigurationsChild {
	private String id;

	private String name;

	public ConfigurationsChild(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	@Override
	public String toString() {
		String result;
			result = "" + this.getClass().getSimpleName() + "(id:" + id + ", name:"+name+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return result;
	}

}
