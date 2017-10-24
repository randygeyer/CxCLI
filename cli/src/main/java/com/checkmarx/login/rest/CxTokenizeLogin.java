package com.checkmarx.login.rest;

import com.checkmarx.jwt.dto.JwtAccessTokenDto;
import com.checkmarx.jwt.exceptions.JWTException;
import com.checkmarx.jwt.utils.JwtUtils;
import com.checkmarx.login.rest.dto.RestGenerateTokenDTO;
import com.checkmarx.login.rest.dto.RestGetAccessTokenDTO;
import com.checkmarx.login.rest.exception.CxClientException;
import com.checkmarx.login.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.login.rest.utils.RestClientUtils;
import com.checkmarx.login.rest.utils.RestResourcesURIBuilder;
import com.checkmarx.login.rest.utils.RestResponseValidator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URL;

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
            postRequest.setEntity(RestHttpEntityBuilder.createGenerateTokenParamsEntity(userName, password));
            generateTokenResponse = client.execute(postRequest);

            RestResponseValidator.validateResponse(generateTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

            RestGenerateTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(generateTokenResponse, RestGenerateTokenDTO.class);
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
            postRequest.setEntity(RestHttpEntityBuilder.createRevokeTokenParamsEntity(token));
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
            postRequest.setEntity(RestHttpEntityBuilder.createGetAccessTokenParamsEntity(token));
            getAccessTokenResponse = client.execute(postRequest);

            RestResponseValidator.validateResponse(getAccessTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

            RestGetAccessTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(getAccessTokenResponse, RestGetAccessTokenDTO.class);
            accessToken = jsonResponse.getAccessToken();
        } catch (IOException e) {
            throw new CxClientException(JSON_PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(getAccessTokenResponse);
        }

        return accessToken;
    }
}