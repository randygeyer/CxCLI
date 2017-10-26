package com.checkmarx.login.authentication;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;

public class CxNTLMAuthetication extends Authenticator {

	protected Logger log;
	
	private String defaultUser;
	private String defaultPassword;
	
	
	public CxNTLMAuthetication(String defaultUser, String defaultPassword, Logger log) {
		this.defaultUser = defaultUser;
		this.defaultPassword = defaultPassword;
		this.log = log;
	}
	
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		log.info("User credentials not found in cache or OS, ask user...");
		System.err.println("User credentials not found in cache or OS, ask user...");
		
		return new PasswordAuthentication (defaultUser, defaultPassword.toCharArray());
	}
	
	@Override
	protected RequestorType getRequestorType() {
		return super.getRequestorType();
	}
}
