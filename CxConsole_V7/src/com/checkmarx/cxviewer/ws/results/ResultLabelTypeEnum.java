package com.checkmarx.cxviewer.ws.results;

public enum ResultLabelTypeEnum {
	Remark(1),
	Severity(2),
	State(3),
	User(4);
	
	private int code;
	
	ResultLabelTypeEnum(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static ResultLabelTypeEnum byCode(int code) {
		ResultLabelTypeEnum[] values = ResultLabelTypeEnum.values();
		for (ResultLabelTypeEnum enConst : values) {
			if (enConst.getCode() == code) {
				return enConst;
			}
		}
		return null;
	} 
}
