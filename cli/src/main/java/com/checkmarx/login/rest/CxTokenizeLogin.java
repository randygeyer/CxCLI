package com.checkmarx.login.rest;

import com.checkmarx.jwt.dto.JwtAccessTokenDto;
import com.checkmarx.jwt.exceptions.JWTException;
import com.checkmarx.jwt.utils.JwtUtils;
import com.checkmarx.login.rest.dto.RestGenerateTokenDTO;
import com.checkmarx.login.rest.dto.RestGetAccessTokenDTO;
import com.checkmarx.login.rest.exception.CxClientException;
import com.checkmarx.login.rest.utils.RestResourcesURIBuilder;
import com.checkmarx.login.rest.utils.RestResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CxTokenizeLogin {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_X_FORM_HEADER = "application/x-www-form-urlencoded";
    private static final String JSON_PARSING_ERROR = "Error parsing JSON from response: ";
    private static final String FAIL_TO_AUTHENTICATE_ERROR = "Fail to authenticate";

    private HttpClient client = HttpClientBuilder.create().build();

    public String generateToken(URL serverUrl, String userName, String password) throws CxClientException {
        HttpPost postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildLoginURL(serverUrl)));
        postRequest.setHeader(CONTENT_TYPE_HEADER, APPLICATION_X_FORM_HEADER);

        HttpResponse generateTokenResponse = null;
        String token;
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(createGenerateTokenParamsList(userName, password)));
            generateTokenResponse = client.execute(postRequest);

            RestResponseValidator.validateResponse(generateTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

            RestGenerateTokenDTO jsonResponse = parseJsonFromResponse(generateTokenResponse, RestGenerateTokenDTO.class);
            token = jsonResponse.getRefreshToken();
        } catch (IOException e) {
            throw new CxClientException(JSON_PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }

        return token;
    }

    public void revokeToken(URL serverUrl, String token) throws CxClientException {
        HttpPost postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildRevokeURL(serverUrl)));
        postRequest.setHeader(CONTENT_TYPE_HEADER, APPLICATION_X_FORM_HEADER);

        HttpResponse generateTokenResponse = null;
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(createRevokeTokenParamsList(token)));
            generateTokenResponse = client.execute(postRequest);

            RestResponseValidator.validateResponse(generateTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

        } catch (IOException e) {
            throw new CxClientException(JSON_PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }
    }

    public String getSessionIdFromToken(URL serverUrl, String token) throws JWTException, CxClientException {
        String accessToken = getAccessToken(serverUrl, token);
        String payload = JwtUtils.getPayloadSectionFromAccessJWT(accessToken);
        String decodedPayload = JwtUtils.convertBase64ToString(payload);
        JwtAccessTokenDto jwtAccessTokenDto = JwtUtils.parseJsonFromString(decodedPayload, JwtAccessTokenDto.class);

        return jwtAccessTokenDto.getSessionId();
    }

    public String getAccessToken(URL serverUrl, String token) throws CxClientException {
        HttpPost postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.getAccessTokenURL(serverUrl)));
        postRequest.setHeader(CONTENT_TYPE_HEADER, APPLICATION_X_FORM_HEADER);

        HttpResponse getAccessTokenResponse = null;
        String accessToken;
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(createGetAccessTokenParamsList(token)));
            getAccessTokenResponse = client.execute(postRequest);

            RestResponseValidator.validateResponse(getAccessTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);
            RestGetAccessTokenDTO jsonResponse = parseJsonFromResponse(getAccessTokenResponse, RestGetAccessTokenDTO.class);
            accessToken = jsonResponse.getAccessToken();

        } catch (IOException e) {
            throw new CxClientException(JSON_PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(getAccessTokenResponse);
        }

        return accessToken;
    }

    private <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse generateTokenResponse, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader rd = new BufferedReader(new InputStreamReader(generateTokenResponse.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return mapper.readValue(result.toString(), dtoClass);
    }

    private List<NameValuePair> createGenerateTokenParamsList(String userName, String password) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("scope", "sast_rest_api offline_access soap_api"));
        urlParameters.add(new BasicNameValuePair("client_id", "cli_client"));
        urlParameters.add(new BasicNameValuePair("client_secret", "B9D84EA8-E476-4E83-A628-8A342D74D3BD"));

        return urlParameters;
    }

    private List<NameValuePair> createRevokeTokenParamsList(String token) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token_type_hint", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("token", token));
        urlParameters.add(new BasicNameValuePair("client_id", "cli_client"));
        urlParameters.add(new BasicNameValuePair("client_secret", "B9D84EA8-E476-4E83-A628-8A342D74D3BD"));

        return urlParameters;
    }

    private List<NameValuePair> createGetAccessTokenParamsList(String token) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("client_id", "cli_client"));
        urlParameters.add(new BasicNameValuePair("client_secret", "B9D84EA8-E476-4E83-A628-8A342D74D3BD"));
        urlParameters.add(new BasicNameValuePair("refresh_token", token));

        return urlParameters;
    }
}