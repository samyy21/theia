package com.paytm.pgplus.theia.nativ.model.vpa.details;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchVpaDetailsRequestBody implements Serializable {

    private static final long serialVersionUID = 958338953026217847L;

    @JsonProperty("isMultiAccForVpaSupported")
    private boolean isMultiAccForVpaSupported;

    private String mid;

    private String userId;

    public boolean isMultiAccForVpaSupported() {
        return isMultiAccForVpaSupported;
    }

    public void setMultiAccForVpaSupported(boolean multiAccForVpaSupported) {
        isMultiAccForVpaSupported = multiAccForVpaSupported;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
