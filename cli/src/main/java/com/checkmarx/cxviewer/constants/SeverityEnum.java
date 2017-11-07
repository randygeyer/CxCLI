package com.checkmarx.cxviewer.constants;

public enum SeverityEnum {

	Information(0),
	Low(1),
	Medium(2),
	High(3),
	Undefined(-1);

	private int code;

	SeverityEnum(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static SeverityEnum byCode(int code) {
		SeverityEnum[] values = SeverityEnum.values();
		for (SeverityEnum enConst : values) {
			if (enConst.getCode() == code) {
				return enConst;
			}
		}
		return null;
	}

	public static SeverityEnum byLabel(String constLabel) {
		SeverityEnum[] values = SeverityEnum.values();
		for (SeverityEnum enConst : values) {
			if (enConst.name().equalsIgnoreCase(constLabel)) {
				return enConst;
			}
		}
		return null;
	}
}
