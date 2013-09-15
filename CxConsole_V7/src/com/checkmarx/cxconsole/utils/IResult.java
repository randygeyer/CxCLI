package com.checkmarx.cxconsole.utils;

public interface IResult<V> {

	public String getMessage();
	public boolean getErrorCode();
	public Throwable getFault();
	public V getResult();
}
