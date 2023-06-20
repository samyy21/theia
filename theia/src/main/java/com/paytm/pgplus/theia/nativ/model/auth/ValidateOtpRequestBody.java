package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateOtpRequestBody implements Serializable {

    private static final long serialVersionUID = 1680106647483445708L;

    private String otp;

    private String authCode;

    private boolean rememberMe;

    private boolean fetchCashierData;

    private String referenceId;

    @JsonIgnore
    private String originalVersion;

    public ValidateOtpRequestBody() {

    }

    public boolean isFetchCashierData() {
        return fetchCashierData;
    }

    public void setFetchCashierData(boolean fetchCashierData) {
        this.fetchCashierData = fetchCashierData;
    }

    public String getOtp() {
        return otp;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getOriginalVersion() {
        return originalVersion;
    }

    public void setOriginalVersion(String originalVersion) {
        this.originalVersion = originalVersion;
    }

    public String getAuthCode() {
        return authCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValidateOtpRequestBody{");
        sb.append("rememberMe=").append(rememberMe);
        sb.append(", fetchCashierData=").append(fetchCashierData);
        sb.append('}');
        return sb.toString();
    }
}