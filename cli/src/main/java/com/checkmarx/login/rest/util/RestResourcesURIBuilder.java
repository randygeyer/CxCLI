package com.checkmarx.login.rest.util;

import java.net.MalformedURLException;
import java.net.URL;

public class RestResourcesURIBuilder {

    private static final String APPLICATION_NAME = "CxRestApi";
    private static final String IDENTITY_CONNECT_RESOURCE = "identity/connect";
    private static final String LOGIN_RESOURCE = "token";
    private static final String REVOCATION_RESOURCE = "revocation";

    private RestResourcesURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static URL buildLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + LOGIN_RESOURCE);
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
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}