package com.checkmarx.clients.rest.osa.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by nirli on 05/12/2017.
 */
public class OsaShaOneDTO implements Serializable {

    @JsonProperty("ProjectId")
    private long projectId;

    @JsonProperty("Origin")
    private String origin;

    @JsonProperty("HashedFilesList")
    private OSAFileToScan[] hashedFilesList;

    public OsaShaOneDTO(long projectId, String origin, OSAFileToScan[] hashedFilesList) {
        this.projectId = projectId;
        this.origin = origin;
        this.hashedFilesList = hashedFilesList;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public OSAFileToScan[] getHashedFilesList() {
        return hashedFilesList;
    }

    public void setHashedFilesList(OSAFileToScan[] hashedFilesList) {
        this.hashedFilesList = hashedFilesList;
    }
}