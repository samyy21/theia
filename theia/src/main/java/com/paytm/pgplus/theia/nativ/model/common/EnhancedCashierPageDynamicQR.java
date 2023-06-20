package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPageDynamicQR implements Serializable {

    private static final long serialVersionUID = 5811963754876820862L;

    private String dataurl;
    private Long pageTimeout;
    private String displayMessage;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    @JsonProperty("isPRN")
    private boolean isPRN;
    /**
     * If true then UPI QR else paytm QR
     */
    @JsonProperty("isUPIQR")
    private boolean isUPIQR;

    public EnhancedCashierPageDynamicQR(String dataurl, Long pageTimeout, String displayMessage, boolean isEnabled,
            boolean isPRN, boolean isUPIQR) {
        this.dataurl = dataurl;
        this.pageTimeout = pageTimeout;
        this.displayMessage = displayMessage;
        this.isEnabled = isEnabled;
        this.isPRN = isPRN;
        this.isUPIQR = isUPIQR;
    }

    public String getDataurl() {
        return dataurl;
    }

    public void setDataurl(String dataurl) {
        this.dataurl = dataurl;
    }

    public Long getPageTimeout() {
        return pageTimeout;
    }

    public void setPageTimeout(Long pageTimeout) {
        this.pageTimeout = pageTimeout;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isPRN() {
        return isPRN;
    }

    public void setPRN(boolean PRN) {
        isPRN = PRN;
    }

    public boolean isUPIQR() {
        return isUPIQR;
    }

    public void setUPIQR(boolean UPIQR) {
        isUPIQR = UPIQR;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
