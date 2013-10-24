package com.checkmarx.cxviewer;

import org.apache.log4j.Logger;

public class CxLogger {
	
	private static Logger _log = Logger.getLogger("com.checkmarx");

	public static void debug(String msg) {
		_log.debug(msg);
		System.out.println(msg);
	}

	public static void error(String msg) {
		_log.error(msg);
		System.err.println(msg);
	}
	
	public static void error(String msg, Throwable e) {
		_log.error(msg, e);
		System.err.println(msg);
	}
	
	public static void error(Throwable e) {
		_log.error("Error catched.", e);
		e.printStackTrace();
	}
	
	public static void trace(String msg) {
		_log.trace(msg);
	}
	
	public static Logger getLogger() {
		return _log;		
	}
}
