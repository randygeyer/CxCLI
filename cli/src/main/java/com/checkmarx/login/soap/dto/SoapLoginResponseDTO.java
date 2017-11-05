//package com.checkmarx.login.soap.dto;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
//import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
//import com.checkmarx.login.soap.exceptions.CxSoapLoginClientException;
//
//public class SoapLoginResponseDTO extends CxWSResponseLoginData {
//    private boolean isAllowedManageUsers;
//    private CxWSResponseLoginData loginData;
//
//    public SoapLoginResponseDTO(CxWSBasicRepsonse responseObject) throws CxSoapLoginClientException {
//        if (responseObject instanceof CxWSResponseLoginData) {
//            loginData = ((CxWSResponseLoginData) responseObject);
//            this.sessionId = loginData.getSessionId();
//            this.isScanner = loginData.isIsScanner();
//            this.isAllowedManageUsers = loginData.isIsAllowedToManageUsers();
//        } else {
//            throw new CxSoapLoginClientException("responseObject is invalid. Input parameter type is [" + responseObject.getClass().getName() + "] and not "
//                    + this.getClass().getName());
//        }
//    }
//
//    public boolean isScanner() {
//        return isScanner;
//    }
//
//    public boolean isAllowedManageUsers() {
//        return isAllowedManageUsers;
//    }
//
//    public CxWSResponseLoginData getLoginData() {
//        return loginData;
//    }
//}
