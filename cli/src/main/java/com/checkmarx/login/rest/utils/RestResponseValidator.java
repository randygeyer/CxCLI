package com.checkmarx.login.rest.utils;

import com.checkmarx.login.rest.exception.CxClientException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.Charset;

public class RestResponseValidator {

    private RestResponseValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateResponse(HttpResponse response, int status, String message) throws CxClientException, IOException {
        if (response.getStatusLine().getStatusCode() != status) {
            String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            responseBody = responseBody.replace("{", "").replace("}", "").replace(System.getProperty("line.separator"), " ").replace("  ", "");
            throw new CxClientException(message + ": " + "status code: " + response.getStatusLine() + ". error:" + responseBody);
        }
    }


}
