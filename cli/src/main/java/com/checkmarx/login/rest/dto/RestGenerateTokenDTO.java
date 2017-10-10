package com.checkmarx.login.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestGenerateTokenDTO {

    @JsonProperty("access_token")
    public String acessToken;

    @JsonProperty("expires_in")
    public int expiresIn;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("refresh_token")
    public String refreshToken;

    public String getAcessToken() {
        return acessToken;
    }

    public void setAcessToken(String acessToken) {
        this.acessToken = acessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}