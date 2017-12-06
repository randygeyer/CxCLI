package com.checkmarx.clients.rest.utils;

import com.checkmarx.clients.rest.exceptions.CxRestClientException;
import com.checkmarx.clients.rest.login.exceptions.CxRestLoginClientException;
import com.checkmarx.clients.rest.osa.constant.OsaShaOneDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 23/10/2017.
 */
public class RestHttpEntityBuilder {

    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLI_CLIENT = "cli_client";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String CLIENT_SECRET_VALUE = "B9D84EA8-E476-4E83-A628-8A342D74D3BD";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String PASS_KEY = "password";
    private static final String USERNAME_KEY = "username";
    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    private RestHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static StringEntity createGenerateTokenParamsEntity(String userName, String password) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_KEY, userName));
        urlParameters.add(new BasicNameValuePair(PASS_KEY, password));
        urlParameters.add(new BasicNameValuePair("grant_type", PASS_KEY));
        urlParameters.add(new BasicNameValuePair("scope", "sast_rest_api offline_access soap_api"));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createRevokeTokenParamsEntity(String token) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token_type_hint", REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair("token", token));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createGetAccessTokenParamsEntity(String token) throws CxRestLoginClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));
        urlParameters.add(new BasicNameValuePair(REFRESH_TOKEN, token));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static UrlEncodedFormEntity createLoginParamsEntity(String userName, String password) throws CxRestLoginClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_KEY, userName));
        urlParameters.add(new BasicNameValuePair(PASS_KEY, password));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createOsaShaOneEntity(OsaShaOneDTO osaShaOneDTO) throws CxRestClientException {
        ObjectMapper mapper = new ObjectMapper();
        String osaShaOneDTOStr;
        try {
            osaShaOneDTOStr = mapper.writeValueAsString(osaShaOneDTO);
            return new StringEntity(osaShaOneDTOStr, ContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }

    }
}