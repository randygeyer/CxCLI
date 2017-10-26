package com.checkmarx.login.rest;

import com.checkmarx.jwt.dto.JwtAccessTokenDto;
import com.checkmarx.jwt.exceptions.JWTException;
import com.checkmarx.jwt.utils.JwtUtils;
import com.checkmarx.login.rest.dto.RestGetAccessTokenDTO;
import com.checkmarx.login.rest.dto.RestLoginResponseDTO;
import com.checkmarx.login.rest.exception.CxRestClientValidatorException;
import com.checkmarx.login.rest.exception.CxRestLoginClientException;
import com.checkmarx.login.rest.utils.RestClientUtils;
import com.checkmarx.login.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.login.rest.utils.RestResourcesURIBuilder;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.checkmarx.login.rest.utils.RestClientUtils.FAIL_TO_AUTHENTICATE_ERROR;
import static com.checkmarx.login.rest.utils.RestClientUtils.validateResponse;

/**
 * Created by nirli on 24/10/2017.
 */
public class CxRestLoginClient {

    private Logger log;

    private final String username;
    private final String password;
    private final String hostName;
    private final String token;

    private HttpClient apacheClient;
    private CookieStore cookieStore;
    private Header cxcsrfTokenHeader;
    private Header authorizationHeader;
    private String cookies;
    private String csrfToken;

    private static final String CX_ORIGIN_HEADER_KEY = "cxOrigin";
    private static final String CX_ORIGIN_HEADER_VALUE = "cx-CLI";
    private static final Header CLI_ORIGIN_HEADER = new BasicHeader(CX_ORIGIN_HEADER_KEY, CX_ORIGIN_HEADER_VALUE);
    private static final String CSRF_TOKEN_HEADER = "CXCSRFToken";

    public CxRestLoginClient(String hostname, String token, Logger log) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;
        this.log = log;

        //create httpclient
        List<Header> headers = new ArrayList<>();
        apacheClient = HttpClientBuilder.create().build();
        try {
            String accessToken = getAccessToken(token);
            authorizationHeader = new BasicHeader("Authorization", "Bearer " + accessToken);
        } catch (CxRestLoginClientException e) {
            log.error("Failed to login with token, due to: " + e.getMessage());
        }
        headers.add(authorizationHeader);
        apacheClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public CxRestLoginClient(String hostname, String username, String password, Logger log) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;
        this.log = log;

        //create httpclient
        cookieStore = new BasicCookieStore();
        List<Header> headers = new ArrayList<>();
        headers.add(cxcsrfTokenHeader);
        headers.add(CLI_ORIGIN_HEADER);
        apacheClient = HttpClientBuilder.create().addInterceptorLast(responseFilter).setDefaultHeaders(headers).setDefaultCookieStore(cookieStore).build();
    }

    private final HttpResponseInterceptor responseFilter = new HttpResponseInterceptor() {
        public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
                if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                    csrfToken = c.getValue();
                }
            }
            if (csrfToken != null) {
                cxcsrfTokenHeader = new BasicHeader(CSRF_TOKEN_HEADER, csrfToken);
            }

            Header[] setCookies = httpResponse.getHeaders("Set-Cookie");

            StringBuilder sb = new StringBuilder();
            for (Header h : setCookies) {
                sb.append(h.getValue()).append(";");
            }

            cookies = (cookies == null ? "" : cookies) + sb.toString();
        }
    };

    public RestLoginResponseDTO credentialsLogin() throws CxRestLoginClientException {
        cookies = null;
        csrfToken = null;
        HttpResponse loginResponse = null;
        HttpPost loginPost = null;
        try {
            loginPost = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildCredentialsLoginURL(new URL(hostName))));
            loginPost.setEntity(RestHttpEntityBuilder.createLoginParamsEntity(username, password));
            //send login request
            loginResponse = apacheClient.execute(loginPost);

            //validate login response
            validateResponse(loginResponse, 200, "Fail to authenticate");
            log.info("Logged in successfully to: " + hostName);
        } catch (IOException | CxRestClientValidatorException e) {
            log.error("Fail to login with credentials: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with credentials: " + e.getMessage());
        } finally {
            if (loginPost != null) {
                loginPost.releaseConnection();
            }
            HttpClientUtils.closeQuietly(loginResponse);
        }

        return new RestLoginResponseDTO(cxcsrfTokenHeader, cookieStore);
    }

    public RestLoginResponseDTO tokenLogin() throws CxRestLoginClientException {
        return new RestLoginResponseDTO(authorizationHeader, getSessionIdFromToken(token));
    }

    private String getSessionIdFromToken(String token) throws CxRestLoginClientException {
        String accessToken = getAccessToken(token);
        JwtAccessTokenDto jwtAccessTokenDto;
        try {
            String payload = JwtUtils.getPayloadSectionFromAccessJWT(accessToken);
            String decodedPayload = JwtUtils.convertBase64ToString(payload);
            jwtAccessTokenDto = JwtUtils.parseJsonFromString(decodedPayload, JwtAccessTokenDto.class);
        } catch (JWTException e) {
            log.error("Failed to get session ID from token: " + e.getMessage());
            throw new CxRestLoginClientException("Failed to get session ID from token: " + e.getMessage());
        }

        log.info("Logged in successfully to: " + hostName);
        return jwtAccessTokenDto.getSessionId();
    }

    private String getAccessToken(String token) throws CxRestLoginClientException {
        HttpResponse getAccessTokenResponse = null;
        String accessToken;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.getAccessTokenURL(new URL(hostName))));
            postRequest.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
            postRequest.setEntity(RestHttpEntityBuilder.createGetAccessTokenParamsEntity(token));
            getAccessTokenResponse = apacheClient.execute(postRequest);

            validateResponse(getAccessTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

            RestGetAccessTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(getAccessTokenResponse, RestGetAccessTokenDTO.class);
            accessToken = jsonResponse.getAccessToken();
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestLoginClientException("Failed to get access token: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(getAccessTokenResponse);
        }

        return accessToken;
    }
}