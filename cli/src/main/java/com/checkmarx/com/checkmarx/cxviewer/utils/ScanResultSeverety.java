package com.checkmarx.cxviewer.utils;

public enum ScanResultSeverety {
	SEVERETY_UNKNOWN("Unknown", -1),
	SEVERETY_HIGH("High", 3),
	SEVERETY_MEDIUM("Medium", 2),
	SEVERETY_LOW("Low", 1),
	SEVERETY_INFO("Information", 0);
	
	private int category;
	private String name;
	
	ScanResultSeverety(String name, int code) {
		this.category = code;
		this.name = name;
	}
	
	public int getCategory() {
		return category;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static ScanResultSeverety forName (String name) {
		if (SEVERETY_HIGH.name.equals(name)) {
			return SEVERETY_HIGH;
		} else if (SEVERETY_MEDIUM.name.equals(name)) {
			return SEVERETY_MEDIUM;
		} else if (SEVERETY_LOW.name.equals(name)) {
			return SEVERETY_LOW;
		} else if (SEVERETY_INFO.name.equals(name)) {
			return SEVERETY_INFO;
		}
		return SEVERETY_UNKNOWN;
	}
	
	public static ScanResultSeverety forCategory(int category) {
		
		ScanResultSeverety[] constants = ScanResultSeverety.values();
		for (ScanResultSeverety constant : constants) {
			if (constant.category == category) {
				return constant;
			}
		}
		return null;
	}
}
