package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QrDetail implements Serializable {

    private static final long serialVersionUID = 1473426006857107221L;

    private String dataUrl;
    private Long pageTimeout;
    private String displayMessage;
    private boolean enabled;
    private boolean prn;
    private boolean upiQR;

    public QrDetail(String dataUrl, Long pageTimeout, String displayMessage, boolean enabled, boolean prn, boolean upiQR) {
        this.dataUrl = dataUrl;
        this.pageTimeout = pageTimeout;
        this.displayMessage = displayMessage;
        this.enabled = enabled;
        this.prn = prn;
        this.upiQR = upiQR;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
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
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPrn() {
        return prn;
    }

    public void setPrn(boolean prn) {
        this.prn = prn;
    }

    public boolean isUpiQR() {
        return upiQR;
    }

    public void setUpiQR(boolean upiQR) {
        this.upiQR = upiQR;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }

}
