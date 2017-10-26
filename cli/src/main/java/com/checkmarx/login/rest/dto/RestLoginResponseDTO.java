package com.checkmarx.login.rest.dto;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;

/**
 * Created by nirli on 25/10/2017.
 */
public class RestLoginResponseDTO {

    public enum LOGIN_TYPE {TOKEN, USERNAME_AND_PASSWORD}

    private Header cxcsrfTokenHeader = null;
    private Header tokenAuthorizationHeader = null;
    private CookieStore cookieStore = null;
    private String sessionId;
    private LOGIN_TYPE loginType;


    public RestLoginResponseDTO(Header cxcsrfTokenHeader, CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        this.cxcsrfTokenHeader = cxcsrfTokenHeader;
        loginType = LOGIN_TYPE.USERNAME_AND_PASSWORD;
    }

    public RestLoginResponseDTO(Header tokenAuthorizationHeader, String sessionId) {
        this.cxcsrfTokenHeader = tokenAuthorizationHeader;
        this.sessionId = sessionId;
        loginType = LOGIN_TYPE.TOKEN;
    }

    public Header getCxcsrfTokenHeader() {
        return cxcsrfTokenHeader;
    }

    public Header getTokenAuthorizationHeader() {
        return tokenAuthorizationHeader;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LOGIN_TYPE getLoginType() {
        return loginType;
    }
}
