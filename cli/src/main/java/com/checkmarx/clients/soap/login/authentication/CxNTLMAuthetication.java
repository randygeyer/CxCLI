package com.checkmarx.clients.soap.login.authentication;

import org.apache.log4j.Logger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

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
        log.error("User credentials not found in cache or OS, ask user...");

        return new PasswordAuthentication(defaultUser, defaultPassword.toCharArray());
    }

    @Override
    protected RequestorType getRequestorType() {
        return super.getRequestorType();
    }
}
