package com.checkmarx.login.rest.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class RestResourcesURIBuilder {

    private static final String APPLICATION_NAME = "cxrestapi";
    private static final String CREDENTIALS_LOGIN_RESOURCE = "auth/login";
    private static final String TOKEN_LOGIN_RESOURCE = "token";
    private static final String IDENTITY_CONNECT_RESOURCE = "identity/connect";
    private static final String REVOCATION_RESOURCE = "revocation";

    private RestResourcesURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static URL buildLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + TOKEN_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildRevokeURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + REVOCATION_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL getAccessTokenURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + TOKEN_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildCredentialsLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + CREDENTIALS_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}