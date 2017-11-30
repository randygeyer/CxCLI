package com.checkmarx.clients.rest.login;

import com.checkmarx.clients.rest.exceptions.CxRestClientValidatorException;
import com.checkmarx.clients.rest.login.dto.RestGetAccessTokenDTO;
import com.checkmarx.clients.rest.login.dto.RestLoginResponseDTO;
import com.checkmarx.clients.rest.login.exceptions.CxRestLoginClientException;
import com.checkmarx.clients.rest.utils.RestClientUtils;
import com.checkmarx.clients.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.clients.rest.utils.RestResourcesURIBuilder;
import com.checkmarx.jwt.dto.JwtAccessTokenDto;
import com.checkmarx.jwt.exceptions.JWTException;
import com.checkmarx.jwt.utils.JwtUtils;
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

import static com.checkmarx.clients.rest.login.CxRestTokenClient.FAIL_TO_AUTHENTICATE_ERROR;
import static com.checkmarx.clients.rest.utils.RestClientUtils.validateResponse;
import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by nirli on 24/10/2017.
 */
public class CxRestLoginClient {

    private Logger log = Logger.getLogger(LOG_NAME);

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
    private RestLoginResponseDTO restLoginResponseDTO;

    private static final String CX_ORIGIN_HEADER_KEY = "cxOrigin";
    private static final String CX_ORIGIN_HEADER_VALUE = "cx-CLI";
    private static final Header CLI_ORIGIN_HEADER = new BasicHeader(CX_ORIGIN_HEADER_KEY, CX_ORIGIN_HEADER_VALUE);
    private static final String CSRF_TOKEN_HEADER = "CXCSRFToken";

    private static final String SERVER_STACK_TRACE_ERROR_MESSAGE = "Failed to get access token: Fail to authenticate: status code: HTTP/1.1 400 Bad Request. error:\"error\":\"invalid_grant\"";

    public CxRestLoginClient(String hostname, String token) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;

        //create httpclient
        List<Header> headers = new ArrayList<>();
        apacheClient = HttpClientBuilder.create().build();
        try {
            String accessToken = getAccessToken(token);
            authorizationHeader = new BasicHeader("Authorization", "Bearer " + accessToken);
        } catch (CxRestLoginClientException e) {
            if (e.getMessage().contains(SERVER_STACK_TRACE_ERROR_MESSAGE)) {
                log.trace("Failed to login, due to: " + e.getMessage());
                log.error("Failed to login: User authentication failed");
            } else {
                log.error("Failed to login with token, due to: " + e.getCause().getMessage());
            }
        }
        headers.add(authorizationHeader);
        apacheClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public CxRestLoginClient(String hostname, String username, String password) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;

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

    public void credentialsLogin() throws CxRestLoginClientException {
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
        } catch (IOException | CxRestClientValidatorException e) {
            log.error("Fail to login with credentials: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with credentials: " + e.getMessage());
        } finally {
            if (loginPost != null) {
                loginPost.releaseConnection();
            }
            HttpClientUtils.closeQuietly(loginResponse);
        }

        restLoginResponseDTO = new RestLoginResponseDTO(cxcsrfTokenHeader, cookieStore);
    }

    public void tokenLogin() throws CxRestLoginClientException {
        restLoginResponseDTO = new RestLoginResponseDTO(authorizationHeader, getSessionIdFromToken(token));
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

    public RestLoginResponseDTO getRestLoginResponseDTO() {
        return restLoginResponseDTO;
    }
}