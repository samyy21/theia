package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

public class RequestHeader implements Serializable {

    @NotBlank
    @JsonProperty("requestId")
    private String requestId;

    @NotBlank
    @JsonProperty("requestTimeStamp")
    private String requestTimeStamp;

    @NotBlank
    @JsonProperty("clientId")
    private String clientId;

    @NotBlank
    @JsonProperty("version")
    private String version;

    @NotBlank
    @JsonProperty("tokenType")
    private String tokenType;

    @NotBlank
    @JsonProperty("token")
    private String token;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
