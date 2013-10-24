package com.checkmarx.cxconsole.utils;

public class CxResult<V> implements IResult<V> {

	private String message = "";
	private Throwable fault;
	private boolean isError = false;
	private V result;
	
	public CxResult(V result) {
		this.result = result;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean getErrorCode() {
		return isError;
	}

	@Override
	public Throwable getFault() {
		return fault;
	}

	@Override
	public V getResult() {
		return result;
	}
}
