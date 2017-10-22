package com.checkmarx.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nirli on 16/10/2017.
 */
public class JwtAccessTokenDto {

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("aud")
    private String audience;

    @JsonProperty("exp")
    private int expired;

    @JsonProperty("nbf")
    private int notBefore;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("scope")
    private String[] scope;

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("auth_time")
    private int authTime;

    @JsonProperty("idp")
    private String idp;

    @JsonProperty("id")
    private String id;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("LCID")
    private String LCID;

    @JsonProperty("email")
    private String email;

    @JsonProperty("Team")
    private String team;

    @JsonProperty("sast_role")
    private String[] sastRole;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("amr")
    private String[] amr;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getScope() {
        return scope;
    }

    public void setScope(String[] scope) {
        this.scope = scope;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public int getAuthTime() {
        return authTime;
    }

    public void setAuthTime(int authTime) {
        this.authTime = authTime;
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLCID() {
        return LCID;
    }

    public void setLCID(String LCID) {
        this.LCID = LCID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String[] getSastRole() {
        return sastRole;
    }

    public void setSastRole(String[] sastRole) {
        this.sastRole = sastRole;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String[] getAmr() {
        return amr;
    }

    public void setAmr(String[] amr) {
        this.amr = amr;
    }
}
