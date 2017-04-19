package com.checkmarx.cxosa;


import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxosa.dto.*;
import com.checkmarx.cxosa.exception.CxClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
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
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by: Dorg.
 * Date: 16/06/2016.
 */
public class CxRestClient {

    private static Logger log;

    private final String username;
    private final String password;
    private final String hostName;

    private static final String OSA_SCAN_PROJECT_PATH = "projects/{projectId}/scans";
    private static final String OSA_SCAN_STATUS_PATH = "scans/{scanId}";
    private static final String OSA_SCAN_SUMMARY_PATH = "projects/{projectId}/summaryresults";
    private static final String OSA_SCAN_HTML_PATH = "projects/{projectId}/opensourceanalysis/htmlresults";
    private static final String OSA_SCAN_PDF_PATH = "projects/{projectId}/opensourceanalysis/pdfresults";
    private static final String AUTHENTICATION_PATH = "auth/login";
    private static final String OSA_ZIPPED_FILE_KEY_NAME = "OSAZippedSourceCode";
    private static final String ROOT_PATH = "CxRestAPI";
    private static final String CSRF_TOKEN_HEADER = "CXCSRFToken";
    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private static final String CX_REPORT_LOCATION = File.separator + "Reports";

    private HttpClient apacheClient;
    private CookieStore cookieStore;
    private String cookies;
    private String csrfToken;

    private ObjectMapper mapper = new ObjectMapper();
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_OSA_PROGRESS_INTERVAL);


    private final HttpRequestInterceptor requestFilter = new HttpRequestInterceptor() {
        public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
            if (csrfToken != null) {
                httpRequest.addHeader(CSRF_TOKEN_HEADER, csrfToken);
            }

            if (cookies != null) {
                httpRequest.addHeader("cookie", cookies);
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
        setLogger(log);

        //create httpclient
        cookieStore = new BasicCookieStore();

        apacheClient = HttpClientBuilder.create().addInterceptorFirst(requestFilter).addInterceptorLast(responseFilter).setDefaultCookieStore(cookieStore).build();
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
        StringEntity requestEntity = new StringEntity(mapper.writeValueAsString(new LoginRequest(username, password)), ContentType.APPLICATION_JSON);
        loginPost.setEntity(requestEntity);
        try {
            //send login request
            loginResponse = apacheClient.execute(loginPost);

            //validate login response
            validateResponse(loginResponse, 200, "Fail to authenticate");
        } finally {
            loginPost.releaseConnection();
            HttpClientUtils.closeQuietly(loginResponse);

        }
    }

    public CreateOSAScanResponse createOSAScan(long projectId, File zipFile) throws IOException, CxClientException {
        login();
        //create scan request
        HttpPost post = new HttpPost(hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_PROJECT_PATH.replace("{projectId}", String.valueOf(projectId)));
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
            validateResponse(response, 202, "Fail to create OSA scan");
            //extract response as object and return the link
            return convertToObject(response, CreateOSAScanResponse.class);
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
            validateResponse(response, 200, "fail get OSA scan status");

            return convertToObject(response, OSAScanStatus.class);
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSASummaryResults getOSAScanSummaryResults(long projectId) throws CxClientException, IOException {

        String resolvedPath = hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_SUMMARY_PATH.replace("{projectId}", String.valueOf(projectId));
        HttpGet getRequest = new HttpGet(resolvedPath);
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "fail get OSA scan summary results");

            return convertToObject(response, OSASummaryResults.class);
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private String getOSAScanHTMLResults(long projectId) throws CxClientException, IOException {

        String resolvedPath = hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_HTML_PATH.replace("{projectId}", String.valueOf(projectId));
        HttpGet getRequest = new HttpGet(resolvedPath);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "fail get OSA scan html results");

            return IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());

        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private byte[] getOSAScanPDFResults(long projectId) throws CxClientException, IOException {
        String resolvedPath = hostName + "/" + ROOT_PATH + "/" + OSA_SCAN_PDF_PATH.replace("{projectId}", String.valueOf(projectId));
        HttpGet getRequest = new HttpGet(resolvedPath);
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "fail get OSA scan pdf results");
            return IOUtils.toByteArray(response.getEntity().getContent());
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void close() {
        HttpClientUtils.closeQuietly(apacheClient);
    }

    private void validateResponse(HttpResponse response, int status, String message) throws CxClientException {
        if (response.getStatusLine().getStatusCode() != status) {
            throw new CxClientException(message + ": " + "status code: " + response.getStatusLine().getStatusCode() + ". reason phrase: " + response.getStatusLine().getReasonPhrase());
        }
    }

    private <T> T convertToObject(HttpResponse response, Class<T> valueType) throws CxClientException {
        String json = "";
        try {
            json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            return mapper.readValue(json, valueType);
        } catch (IOException e) {
            log.debug("fail to parse json response: [" + json + "]", e);
            throw new CxClientException("fail to parse json response: " + e.getMessage());
        }
    }

    public OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler) throws CxClientException, IOException {
        //re login in case of session timed out
        login();
        long timeToStop = (System.currentTimeMillis() / 60000) + scanTimeoutInMin;

        long startTime = System.currentTimeMillis();
        OSAScanStatus scanStatus = null;
        OSAScanStatusEnum status = null;

        waitHandler.onStart(startTime, scanTimeoutInMin);

        int retry = waitForScanToFinishRetry;

        while (scanTimeoutInMin <= 0 || (System.currentTimeMillis() / 60000) <= timeToStop) {

            try {
                Thread.sleep(10000); //Get status every 10 sec
            } catch (InterruptedException e) {
                // log.debug("caught exception during sleep", e);
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

    public void createOsaHtmlReport(long projectId, String now, String workDirectory) throws IOException, CxClientException {
        String osaHtml = getOSAScanHTMLResults(projectId);
        String htmlFileName = OSA_REPORT_NAME + "_" + now + ".html" ;
        FileUtils.writeStringToFile(new File(workDirectory + CX_REPORT_LOCATION, htmlFileName), osaHtml, Charset.defaultCharset());
        log.info("OSA HTML report location: " + workDirectory + CX_REPORT_LOCATION + File.separator + htmlFileName);
    }
    public void createOsaPdfReport(long projectId, String now, String workDirectory) throws IOException, CxClientException {
        byte[] osaPDF = getOSAScanPDFResults(projectId);
        String pdfFileName = OSA_REPORT_NAME + "_" + now + ".pdf" ;
        FileUtils.writeByteArrayToFile(new File(workDirectory + CX_REPORT_LOCATION, pdfFileName), osaPDF);
        log.info("OSA PDF report location: " + workDirectory + CX_REPORT_LOCATION + File.separator + pdfFileName);

}

    public static TrustManager[] createFakeTrustManager() {
        return new TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

    }
}

