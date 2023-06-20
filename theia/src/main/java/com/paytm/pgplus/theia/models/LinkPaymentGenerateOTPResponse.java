package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Raman Preet Singh
 * @since 30/10/17
 **/

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkPaymentGenerateOTPResponse implements Serializable {

    private static final long serialVersionUID = -6615078545845554086L;

    private String status;
    private String statusMessage;
    private String otpState;

    public LinkPaymentGenerateOTPResponse(String status, String statusMessage, String otpState) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.otpState = otpState;
    }

    public LinkPaymentGenerateOTPResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getOtpState() {
        return otpState;
    }

    public void setOtpState(String otpState) {
        this.otpState = otpState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("statusMessage", statusMessage)
                .append("otpState", otpState).toString();
    }
}
