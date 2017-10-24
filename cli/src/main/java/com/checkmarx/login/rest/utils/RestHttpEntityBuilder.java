package com.checkmarx.login.rest.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.message.BasicNameValuePair;

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

    private RestHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpEntity createGenerateTokenParamsEntity(String userName, String password) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("scope", "sast_rest_api offline_access soap_api"));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setParameters(urlParameters);

        return entityBuilder.build();
    }

    public static HttpEntity createRevokeTokenParamsEntity(String token) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token_type_hint", REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair("token", token));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setParameters(urlParameters);

        return entityBuilder.build();
    }

    public static HttpEntity createGetAccessTokenParamsEntity(String token) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));
        urlParameters.add(new BasicNameValuePair(REFRESH_TOKEN, token));

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setParameters(urlParameters);

        return entityBuilder.build();
    }
}