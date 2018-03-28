package com.checkmarx.clients.rest.osa;

import com.checkmarx.clients.rest.exceptions.CxRestClientException;
import com.checkmarx.clients.rest.exceptions.CxRestClientValidatorException;
import com.checkmarx.clients.rest.login.CxRestClient;
import com.checkmarx.clients.rest.login.dto.RestLoginResponseDTO;
import com.checkmarx.clients.rest.osa.constant.FileNameAndShaOneForOsaScan;
import com.checkmarx.clients.rest.osa.constant.OsaShaOneDTO;
import com.checkmarx.clients.rest.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.clients.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.clients.rest.utils.RestResourcesURIBuilder;
import com.checkmarx.cxconsole.cxosa.ScanWaitHandler;
import com.checkmarx.cxconsole.cxosa.dto.*;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.checkmarx.clients.rest.login.dto.RestLoginResponseDTO.LOGIN_TYPE.USERNAME_AND_PASSWORD;
import static com.checkmarx.clients.rest.utils.RestClientUtils.*;
import static com.checkmarx.cxconsole.CxConsoleLauncher.LOG_NAME;

/**
 * Created by: Dorg.
 * Date: 16/06/2016.
 */
public class CxRestOSAClient extends CxRestClient {

    private static Logger log = Logger.getLogger(LOG_NAME);

    private String hostName;
    private HttpClient apacheClient;
    private RestLoginResponseDTO restLoginResponseDTO;
    private RestLoginResponseDTO.LOGIN_TYPE loginType;
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_OSA_PROGRESS_INTERVAL);

    private static final String OSA_SUMMARY_NAME = "CxOSASummary";
    private static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    private static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    private static final String CLI_ORIGIN_VALUE_IN_SERVER = "CLI";
    private static final String JSON_FILE = ".json";
    private ObjectMapper objectMapper = new ObjectMapper();

    public CxRestOSAClient(String hostName, RestLoginResponseDTO restLoginResponseDTO) {
        this.hostName = hostName;
        this.restLoginResponseDTO = restLoginResponseDTO;
        this.loginType = restLoginResponseDTO.getLoginType();
    }

    public CreateOSAScanResponse createOSAScan(long projectId, List<FileNameAndShaOneForOsaScan> hashedFilesList) throws CxRestOSAClientException {
        HttpPost post = null;
        HttpResponse response = null;
        OsaShaOneDTO osaShaOneDTO = new OsaShaOneDTO(projectId, CLI_ORIGIN_VALUE_IN_SERVER, hashedFilesList);

        try {
            post = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildCreateOSASha1ScanURL(new URL(hostName))));
            List<Header> defaultHeaders = new ArrayList<>();
            if (loginType == USERNAME_AND_PASSWORD) {
                defaultHeaders.add(restLoginResponseDTO.getCxcsrfTokenHeader());
                super.cookieStore = restLoginResponseDTO.getCookieStore();
                apacheClient = super.createClient(null, null, defaultHeaders);
            } else {
                defaultHeaders.add(restLoginResponseDTO.getTokenAuthorizationHeader());
                apacheClient = super.createClient(null, null, defaultHeaders);
            }
            post.setEntity(RestHttpEntityBuilder.createOsaShaOneEntity(osaShaOneDTO));

            //send scan request
            response = apacheClient.execute(post);
            //verify scan request
            validateResponse(response, 202, "Fail to create OSA scan");

            //extract response as object and return the link
            return parseJsonFromResponse(response, CreateOSAScanResponse.class);
        } catch (IOException | CxRestClientException e) {
            log.error(e.getMessage());
            throw new CxRestOSAClientException(e.getMessage());
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSASummaryResults getOSAScanSummaryResults(String scanId) throws CxRestOSAClientException {
        HttpGet getRequest = null;
        HttpResponse response = null;

        try {
            getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanSummaryResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "fail get OSA scan summary results");

            return parseJsonFromResponse(response, OSASummaryResults.class);
        } catch (IOException | CxRestClientValidatorException e) {
            log.error("Failed to get OSA scan summary results: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA scan summary results: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void createOsaHtmlReport(String scanId, String filePath) throws CxRestOSAClientException {
        try {
            String osaHtml = getOSAScanHTMLResults(scanId);
            writeReport(osaHtml, filePath, "HTML report");
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestOSAClientException("Failed to create OSA HTML report: " + e.getMessage());
        }
    }

    public void createOsaPdfReport(String scanId, String filePath) throws CxRestOSAClientException {
        try {
            byte[] osaPDF = getOSAScanPDFResults(scanId);
            writeReport(osaPDF, filePath, "PDF report");
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestOSAClientException("Failed to create OSA PDF report: " + e.getMessage());
        }
    }

    public void createOsaJson(String scanId, String filePath, OSASummaryResults osaSummaryResults) throws CxRestOSAClientException {
        try {
            String specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_SUMMARY_NAME + JSON_FILE);
            writeReport(osaSummaryResults, specificFilePath, "summary json");

            List<Library> libraries = getOSALibraries(scanId);
            specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_LIBRARIES_NAME + JSON_FILE);
            writeReport(libraries, specificFilePath, "libraries json");

            List<CVE> osaVulnerabilities = getOSAVulnerabilities(scanId);
            specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_VULNERABILITIES_NAME + JSON_FILE);
            writeReport(osaVulnerabilities, specificFilePath, "vulnerabilities json");
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestOSAClientException("Failed to create OSA JSON report: " + e.getMessage());
        }
    }

    public void close() {
        HttpClientUtils.closeQuietly(apacheClient);
    }

    private String getOSAScanHTMLResults(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanSummaryResultsURL(new URL(hostName), scanId)), MediaType.TEXT_HTML);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "Failed to get OSA scan html results");

            return IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private byte[] getOSAScanPDFResults(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanSummaryResultsURL(new URL(hostName), scanId)), "application/pdf");
        HttpResponse response = null;

        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "Failed to get OSA scan pdf results");

            return IOUtils.toByteArray(response.getEntity().getContent());
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
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

    private List<Library> getOSALibraries(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanLibrariesResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "Failed to get OSA libraries");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, Library.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private List<CVE> getOSAVulnerabilities(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanVulnerabilitiesResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "Failed to get OSA vulnerabilities");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, CVE.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private HttpGet createHttpRequest(String path, String mediaType) {
        HttpGet getRequest = new HttpGet(path);
        getRequest.setHeader("Accept", mediaType);
        return getRequest;
    }

    private OSAScanStatus getOSAScanStatus(String scanId) throws CxRestOSAClientException, CxRestClientValidatorException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanStatusURL(new URL(hostName), scanId)));
            response = apacheClient.execute(getRequest);
            validateResponse(response, 200, "Failed to get OSA scan status");

            return parseJsonFromResponse(response, OSAScanStatus.class);
        } catch (IOException e) {
            throw new CxRestOSAClientException("Failed to get OSA scan status: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler, boolean isAsyncOsaScan) throws CxRestOSAClientException {
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
                    Thread.currentThread().interrupt();
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
                throw new CxRestOSAClientException("OSA scan cannot be completed. status: [" + status.uiValue() + "]. message: [" + StringUtils.defaultString(scanStatus.getMessage()) + "]");
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
            log.error("OSA scan has reached the time limit. (" + scanTimeoutInMin + " minutes).");
            throw new CxRestOSAClientException("OSA scan has reached the time limit. (" + scanTimeoutInMin + " minutes).");
        }

        return scanStatus;
    }

    private int checkRetry(int retry, String errorMessage) throws CxRestOSAClientException {
        log.debug("fail to get status from scan. retrying (" + (retry - 1) + " tries left). error message: " + errorMessage);
        retry--;
        if (retry <= 0) {
            log.error("fail to get status from scan. error message: " + errorMessage);
            throw new CxRestOSAClientException("fail to get status from scan. error message: " + errorMessage);
        }

        return retry;
    }
}