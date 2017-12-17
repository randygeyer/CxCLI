package com.checkmarx.clients.rest.osa.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class FileNameAndShaOneForOsaScan implements Serializable {

    @JsonProperty("sha1")
    private String sha1;
    @JsonProperty("filename")
    private String fileName;

    public FileNameAndShaOneForOsaScan(String sha1, String fileName) {
        this.sha1 = sha1;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
}
