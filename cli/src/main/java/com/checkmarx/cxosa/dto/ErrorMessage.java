package com.checkmarx.cxosa.dto;

/**
 * Created by Galn on 14/06/2017.
 */
public class ErrorMessage {
    String messageCode;
    String messageDetails;

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessageDetails() {
        return messageDetails;
    }

    public void setMessageDetails(String messageDetails) {
        this.messageDetails = messageDetails;
    }
}
