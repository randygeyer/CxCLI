package com.checkmarx.login.rest;

import com.checkmarx.login.rest.dto.RestLoginRequest;
import com.checkmarx.login.rest.exception.CxClientException;
import com.checkmarx.login.rest.utils.RestClientUtils;
import com.checkmarx.login.rest.utils.RestResourcesURIBuilder;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 24/10/2017.
 */
public class CxRestLoginClient {

    private enum LOGIN_TYPE {TOKEN, USERNAME_AND_PASSWORD}

    private Logger log;

    private final String username;
    private final String password;
    private final String hostName;
    private final String token;
    private LOGIN_TYPE loginType;

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

    private final HttpRequestInterceptor requestFilter = new HttpRequestInterceptor() {
        public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
            if (csrfToken != null) {
                httpRequest.addHeader(CSRF_TOKEN_HEADER, csrfToken);
                cxcsrfTokenHeader = new BasicHeader(CSRF_TOKEN_HEADER, csrfToken);
            }
        }
    };

    private final HttpResponseInterceptor responseFilter = new HttpResponseInterceptor() {
        public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
                if (CSRF_TOKEN_HEADER.equals(c.getName())) {
                    csrfToken = c.getValue();
                }
            }
            Header[] setCookies = httpResponse.getHeaders("Set-Cookie");

            StringBuilder sb = new StringBuilder();
            for (Header h : setCookies) {
                sb.append(h.getValue()).append(";");
            }

            cookies = (cookies == null ? "" : cookies) + sb.toString();
        }
    };

    public CxRestLoginClient(String hostname, String username, String password, Logger log) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;
        this.loginType = LOGIN_TYPE.USERNAME_AND_PASSWORD;
        this.log = log;

        //create httpclient
        cookieStore = new BasicCookieStore();
        List<Header> headers = new ArrayList<>();
        headers.add(cxcsrfTokenHeader);
        headers.add(CLI_ORIGIN_HEADER);
        apacheClient = HttpClientBuilder.create().addInterceptorFirst(requestFilter).addInterceptorLast(responseFilter).setDefaultHeaders(headers).setDefaultCookieStore(cookieStore).build();
    }

    public CxRestLoginClient(String hostname, String token, Logger log) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;
        this.loginType = LOGIN_TYPE.TOKEN;
        this.log = log;

        //create httpclient
        CxTokenizeLogin cxTokenizeLogin = new CxTokenizeLogin();
        List<Header> headers = new ArrayList<>();
        try {
            authorizationHeader = new BasicHeader("Authorization", "Bearer " + cxTokenizeLogin.getAccessToken(new URL(hostname), token));
        } catch (CxClientException | MalformedURLException e) {
            e.printStackTrace();
        }
        headers.add(authorizationHeader);
        apacheClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public void credentialsLogin() throws CxClientException, IOException {
        cookies = null;
        csrfToken = null;
        HttpResponse loginResponse = null;
        //create login request
        HttpPost loginPost = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildCredentialsLoginURL(new URL(hostName))));
        StringEntity requestEntity = new StringEntity(mapper.writeValueAsString(new RestLoginRequest(username, password)), ContentType.APPLICATION_JSON);
        loginPost.setEntity(requestEntity);
        try {
            //send login request
            loginResponse = apacheClient.execute(loginPost);

            //validate login response
            RestClientUtils.validateResponse(loginResponse, 200, "Fail to authenticate");
        } finally {
            loginPost.releaseConnection();
            HttpClientUtils.closeQuietly(loginResponse);
        }
    }




}
