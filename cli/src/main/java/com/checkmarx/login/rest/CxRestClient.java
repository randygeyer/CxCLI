package com.checkmarx.login.rest;

import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxosa.ScanWaitHandler;
import com.checkmarx.cxosa.dto.*;
import com.checkmarx.login.rest.dto.RestLoginRequest;
import com.checkmarx.login.rest.exception.CxClientException;
import com.checkmarx.login.rest.utils.RestClientUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by: Dorg.
 * Date: 16/06/2016.
 */
public class CxRestClient {

    private enum LOGIN_TYPE {TOKEN, USERNAME_AND_PASSWORD}

    private static Logger log;

    private final String username;
    private final String password;
    private final String hostName;
    private final String token;
    private LOGIN_TYPE loginType;

    private static final String OSA_SCAN_PROJECT_PATH = "projects/{projectId}/scans";
    private static final String OSA_SCAN_STATUS_PATH = "scans/{scanId}";
    private static final String OSA_SCAN_SUMMARY_PATH = "osa/reports";
    private static final String CX_ORIGIN_HEADER = "cxOrigin";
    private static final String CX_ORIGIN_VALUE = "cx-CLI";
    public static final String OSA_SCAN_LIBRARIES_PATH = "/osa/libraries";
    public static final String OSA_SCAN_VULNERABILITIES_PATH = "/osa/vulnerabilities";
    public static final String SCAN_ID_QUERY_PARAM = "?scanId=";
    private static final String AUTHENTICATION_PATH = "auth/login";
    private static final String OSA_ZIPPED_FILE_KEY_NAME = "OSAZippedSourceCode";
    private static final String ROOT_PATH = "cxrestapi";
    private static final String CSRF_TOKEN_HEADER = "CXCSRFToken";

    public static final String ITEM_PER_PAGE_QUERY_PARAM = "&itemsPerPage=";
    public static final long MAX_ITEMS = 1000000;
    public static final String OSA_SUMMARY_NAME = "CxOSASummary";
    public static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    public static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    private ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient apacheClient;
    private CookieStore cookieStore;
    private Header cxcsrfTokenHeader;
    private Header authorizationHeader;
    private String cookies;
    private String csrfToken;

    private ObjectMapper mapper = new ObjectMapper();
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_OSA_PROGRESS_INTERVAL);


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

    public CxRestClient(String hostname, String username, String password, Logger log) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;
        this.loginType = LOGIN_TYPE.USERNAME_AND_PASSWORD;
        setLogger(log);

        //create httpclient
        cookieStore = new BasicCookieStore();
        List<Header> headers = new ArrayList<>();
        headers.add(cxcsrfTokenHeader);
        apacheClient = HttpClientBuilder.create().addInterceptorFirst(requestFilter).addInterceptorLast(responseFilter).setDefaultHeaders(headers).setDefaultCookieStore(cookieStore).build();
    }

    public CxRestClient(String hostname, String token, Logger log) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;
        this.loginType = LOGIN_TYPE.TOKEN;
        setLogger(log);

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

    public void setLogger(Logger log) {
        CxRestClient.log = log;
    }

    public void login() throws CxClientException, IOException {
        cookies = null;
        csrfToken = null;
        HttpResponse loginResponse = null;
        //create login request
        HttpPost loginPost = new HttpPost(hostName + "/" + ROOT_PATH + "/" + AUTHENTICATION_PATH);
        loginPost.setHeader(CX_ORIGIN_HEADER, CX_ORIGIN_VALUE);
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

    public CreateOSAScanResponse createOSAScan(long projectId, File zipFile) throws IOException, CxClientException {
        //create scan request
        HttpPost post = new HttpPost(hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_PROJECT_PATH.replace("{projectId}", String.valueOf(projectId)));
        if (loginType == LOGIN_TYPE.USERNAME_AND_PASSWORD) {
            post.setHeader(CX_ORIGIN_HEADER, CX_ORIGIN_VALUE);
        } else {
            post.setHeader(authorizationHeader);
        }
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        InputStreamBody streamBody = new InputStreamBody(fileInputStream, ContentType.APPLICATION_OCTET_STREAM, OSA_ZIPPED_FILE_KEY_NAME);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(OSA_ZIPPED_FILE_KEY_NAME, streamBody);
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        HttpResponse response = null;

        try {
            //send scan request
            response = apacheClient.execute(post);
            //verify scan request
            RestClientUtils.validateResponse(response, 202, "Fail to create OSA scan");

            //extract response as object and return the link
            return RestClientUtils.parseJsonFromResponse(response, CreateOSAScanResponse.class);
        } finally {
            post.releaseConnection();
            IOUtils.closeQuietly(fileInputStream);
            HttpClientUtils.closeQuietly(response);
        }
    }

    private OSAScanStatus getOSAScanStatus(String scanId) throws CxClientException, IOException {
        String resolvedPath = hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_STATUS_PATH.replace("{scanId}", String.valueOf(scanId));
        HttpGet getRequest = new HttpGet(resolvedPath);
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "Failed to get OSA scan status");

            return RestClientUtils.parseJsonFromResponse(response, OSAScanStatus.class);
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSASummaryResults getOSAScanSummaryResults(String scanId) throws CxClientException, IOException {
        String relativePath = OSA_SCAN_SUMMARY_PATH + SCAN_ID_QUERY_PARAM + scanId;
        HttpGet getRequest = createHttpRequest(relativePath, "application/json");
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "fail get OSA scan summary results");

            return RestClientUtils.parseJsonFromResponse(response, OSASummaryResults.class);
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private String getOSAScanHTMLResults(String scanId) throws CxClientException, IOException {

        String relativePath = OSA_SCAN_SUMMARY_PATH + SCAN_ID_QUERY_PARAM + scanId;
        HttpGet getRequest = createHttpRequest(relativePath, "text/html");
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "Failed to get OSA scan html results");

            return IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private byte[] getOSAScanPDFResults(String scanId) throws CxClientException, IOException {
        String relativePath = OSA_SCAN_SUMMARY_PATH + SCAN_ID_QUERY_PARAM + scanId;
        HttpGet getRequest = createHttpRequest(relativePath, "application/pdf");
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "Failed to get OSA scan pdf results");

            return IOUtils.toByteArray(response.getEntity().getContent());
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void createOsaHtmlReport(String scanId, String filePath) throws IOException, CxClientException {
        String osaHtml = getOSAScanHTMLResults(scanId);
        writeReport(osaHtml, filePath, "HTML report");
    }


    public void createOsaPdfReport(String scanId, String filePath) throws IOException, CxClientException {
        byte[] osaPDF = getOSAScanPDFResults(scanId);
        writeReport(osaPDF, filePath, "PDF report");
    }

    public void createOsaJson(String scanId, String filePath, OSASummaryResults osaSummaryResults) throws IOException, CxClientException {

        String specificFilePath = filePath.replace(".json", "_" + OSA_SUMMARY_NAME + ".json");
        writeReport(osaSummaryResults, specificFilePath, "summary json");

        List<Library> libraries = getOSALibraries(scanId);
        specificFilePath = filePath.replace(".json", "_" + OSA_LIBRARIES_NAME + ".json");
        writeReport(libraries, specificFilePath, "libraries json");

        List<CVE> osaVulnerabilities = getOSAVulnerabilities(scanId);
        specificFilePath = filePath.replace(".json", "_" + OSA_VULNERABILITIES_NAME + ".json");
        writeReport(osaVulnerabilities, specificFilePath, "vulnerabilities json");
    }

    private void writeReport(Object data, String filePath, String toLog) throws IOException {
        File file = new File(filePath);

        switch (FilenameUtils.getExtension(filePath)) {
            case ("html"):
                FileUtils.writeStringToFile(file, (String) data, Charset.defaultCharset());
                break;
            case ("pdf"):
                FileUtils.writeByteArrayToFile(file, (byte[]) data);
                break;
            case ("json"):
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
                break;
            default:
                log.error("OSA " + toLog + " location is invalid");
                return;
        }
        log.info("OSA " + toLog + " location: " + file.getAbsolutePath());
    }


    private List<Library> getOSALibraries(String scanId) throws CxClientException, IOException {
        String relativePath = OSA_SCAN_LIBRARIES_PATH + SCAN_ID_QUERY_PARAM + scanId + ITEM_PER_PAGE_QUERY_PARAM + MAX_ITEMS;
        HttpGet getRequest = createHttpRequest(relativePath, "application/json");
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "Failed to get OSA libraries");

            return RestClientUtils.parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, Library.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    public List<CVE> getOSAVulnerabilities(String scanId) throws CxClientException, IOException {
        String relativePath = OSA_SCAN_VULNERABILITIES_PATH + SCAN_ID_QUERY_PARAM + scanId + ITEM_PER_PAGE_QUERY_PARAM + MAX_ITEMS;
        HttpGet getRequest = createHttpRequest(relativePath, "application/json");
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateResponse(response, 200, "Failed to get OSA vulnerabilities");

            return RestClientUtils.parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, CVE.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private HttpGet createHttpRequest(String relativePath, String mediaType) {
        String resolvedPath = hostName + "/" + ROOT_PATH + "/" + relativePath;
        HttpGet getRequest = new HttpGet(resolvedPath);
        getRequest.setHeader("Accept", mediaType);
        return getRequest;
    }

    public void close() {
        HttpClientUtils.closeQuietly(apacheClient);
    }

    public OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler, boolean isAsyncOsaScan) throws CxClientException, IOException {
        //re login in case of session timed out
        // TODO: verify if needed
        if (token == null) {
            login();
        }
        long timeToStop = (System.currentTimeMillis() / 60000) + scanTimeoutInMin;

        long startTime = System.currentTimeMillis();
        OSAScanStatus scanStatus = null;
        OSAScanStatusEnum status = null;

        waitHandler.onStart(startTime, scanTimeoutInMin);

        int retry = waitForScanToFinishRetry;

        while (scanTimeoutInMin <= 0 || (System.currentTimeMillis() / 60000) <= timeToStop) {

            if (!isAsyncOsaScan) {
                try {
                    Thread.sleep(10000); //Get status every 10 sec
                } catch (InterruptedException e) {
                    // log.debug("caught exception during sleep", e);
                }
            }

            try {
                scanStatus = getOSAScanStatus(scanId);
            } catch (Exception e) {
                retry = checkRetry(retry, e.getMessage());
                continue;
            }

            retry = waitForScanToFinishRetry;

            status = scanStatus.getStatus();

            if (OSAScanStatusEnum.FAILED.equals(status)) {
                waitHandler.onFail(scanStatus);
                throw new CxClientException("OSA scan cannot be completed. status: [" + status.uiValue() + "]. message: [" + StringUtils.defaultString(scanStatus.getMessage()) + "]");
            }

            if (isAsyncOsaScan && (OSAScanStatusEnum.QUEUED.equals(status) || OSAScanStatusEnum.IN_PROGRESS.equals(status))) {
                waitHandler.onQueued(scanStatus);
                return scanStatus;
            }

            if (OSAScanStatusEnum.FINISHED.equals(status)) {
                waitHandler.onSuccess(scanStatus);
                return scanStatus;
            }
            waitHandler.onIdle(scanStatus);
        }

        if (!OSAScanStatusEnum.FINISHED.equals(status)) {
            waitHandler.onTimeout(scanStatus);
            throw new CxClientException("OSA scan has reached the time limit. (" + scanTimeoutInMin + " minutes).");
        }

        return scanStatus;
    }

    private int checkRetry(int retry, String errorMessage) throws CxClientException {
        log.debug("fail to get status from scan. retrying (" + (retry - 1) + " tries left). error message: " + errorMessage);
        retry--;
        if (retry <= 0) {
            throw new CxClientException("fail to get status from scan. error message: " + errorMessage);
        }

        return retry;
    }
}