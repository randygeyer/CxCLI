package com.checkmarx.jwt.utils;

import com.checkmarx.jwt.exceptions.JWTException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by nirli on 16/10/2017.
 */
public class JwtUtils {

    private JwtUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getPayloadSectionFromAccessJWT(String accessJWT) throws JWTException {
        String[] accessJWTDividedToSection = accessJWT.split("\\.");
        if (accessJWTDividedToSection.length != 3) {
            throw new JWTException("Access token is incomplete");
        }

        return accessJWTDividedToSection[1];
    }

    public static String convertBase64ToString(String base64String) throws JWTException {
        byte[] decoded = Base64.decodeBase64(base64String);

        String decodedString;
        try {
            decodedString = new String(decoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JWTException("Can't decode base64 to String");
        }

        return decodedString;
    }

    public static <ResponseObj> ResponseObj parseJsonFromString(String jsonInString, Class<ResponseObj> dtoClass) throws JWTException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(jsonInString, dtoClass);
        } catch (IOException e) {
            throw new JWTException("Can't convert string into JSON");
        }
    }


}