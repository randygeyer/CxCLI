package com.checkmarx.clients.rest.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class RestResourcesURIBuilder {

    private static final String APPLICATION_NAME = "cxrestapi";

    private static final String CREDENTIALS_LOGIN_RESOURCE = "auth/login";
    private static final String TOKEN_LOGIN_RESOURCE = "token";
    private static final String IDENTITY_CONNECT_RESOURCE = "auth/identity/connect";
    private static final String REVOCATION_RESOURCE = "revocation";

    private static final String SCAN_ID_QUERY_PARAM = "?scanId=";
    private static final String ITEM_PER_PAGE_QUERY_PARAM = "&itemsPerPage=";
    private static final String OSA_SCAN_STATUS_RESOURCE = "scans/{scanId}";
    private static final String OSA_SCAN_SUMMARY_RESOURCE = "osa/reports";
    private static final String OSA_FILE_EXTENSIONS_RESOURCE = "osa/fileextensions";
    private static final String OSA_SCAN_LIBRARIES_PATH = "/osa/libraries";
    private static final String OSA_SCAN_VULNERABILITIES_PATH = "/osa/vulnerabilities";
    private static final String OSA_CREATE_SHA1_SCAN_PATH = "osa/scans";
    private static final long MAX_ITEMS = 1000000;

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

    public static URL getFileExtensionsURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_FILE_EXTENSIONS_RESOURCE);
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

    public static URL buildCreateOSASha1ScanURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_CREATE_SHA1_SCAN_PATH);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanStatusURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_STATUS_RESOURCE.replace("{scanId}", String.valueOf(scanId)));
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanSummaryResultsURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_SUMMARY_RESOURCE + SCAN_ID_QUERY_PARAM + scanId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanLibrariesResultsURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_LIBRARIES_PATH + SCAN_ID_QUERY_PARAM + scanId + ITEM_PER_PAGE_QUERY_PARAM + MAX_ITEMS);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanVulnerabilitiesResultsURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_VULNERABILITIES_PATH + SCAN_ID_QUERY_PARAM + scanId + ITEM_PER_PAGE_QUERY_PARAM + MAX_ITEMS);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}