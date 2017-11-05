package com.checkmarx.login.soap.utils;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.auth.SpnegoAuthSupplier;

import java.net.URI;
;

/**
 * Created with IntelliJ IDEA.
 * User: denisk
 * Date: 2/24/14
 * Time: 3:49 PM
 * Description: This class dynamically replaces all occurrences of ${var_name} in cxf.xml by values of System.getProperty("var_name")
 */
public class DynamicAuthSupplier extends SpnegoAuthSupplier {
    public static final String PROPERTY_PATTERN = "^\\$\\{.*\\}$";
    public static final String CLEAN_PROPERTY_NAME = "(\\$)|(\\{)|(\\})";

    private static boolean isKerberosActive = false;

    public static void setKerberosActive(boolean isActive){
        isKerberosActive = isActive;
    }

    @Override
    public String getAuthorization(AuthorizationPolicy authPolicy, URI currentURI, Message message, String fullHeader) {
        if(!isKerberosActive){
            return null;
        }

        String userName = authPolicy.getUserName();
        authPolicy.setUserName(
                userName != null && userName.matches(PROPERTY_PATTERN)
                        ? System.getProperty(userName.replaceAll(CLEAN_PROPERTY_NAME, ""))
                        : userName);

        String password = authPolicy.getPassword();
        authPolicy.setPassword(
                password != null && password.matches(PROPERTY_PATTERN)
                        ? System.getProperty(password.replaceAll(CLEAN_PROPERTY_NAME, ""))
                        : password);

        return super.getAuthorization(authPolicy, currentURI, message, fullHeader);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
