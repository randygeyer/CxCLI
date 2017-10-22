package com.checkmarx.login.rest.utils;

import com.checkmarx.login.rest.exception.CxClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by nirli on 19/10/2017.
 */
public class RestClientUtils {

    private RestClientUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateResponse(HttpResponse response, int status, String message) throws CxClientException, IOException {
        if (response.getStatusLine().getStatusCode() != status) {
            String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            responseBody = responseBody.replace("{", "").replace("}", "").replace(System.getProperty("line.separator"), " ").replace("  ", "");
            throw new CxClientException(message + ": " + "status code: " + response.getStatusLine() + ". error:" + responseBody);
        }
    }

    public static <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse generateTokenResponse, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader rd = new BufferedReader(new InputStreamReader(generateTokenResponse.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return mapper.readValue(result.toString(), dtoClass);
    }

    public static <ResponseObj> List<ResponseObj> parseJsonListFromResponse(HttpResponse generateTokenResponse, CollectionType dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader rd = new BufferedReader(new InputStreamReader(generateTokenResponse.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return mapper.readValue(result.toString(), dtoClass);
    }
}
