package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.TwoFADetails;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class FetchBalanceInfoRequestBody implements Serializable {

    private final static long serialVersionUID = 958338953026217847L;

    @JsonProperty("paymentMode")
    private String paymentMode;

    @JsonProperty("mid")
    private String mid;

    private String referenceId;

    private String exchangeRate;

    private String rootUserId;

    @JsonProperty("twoFADetails")
    private TwoFADetails twoFADetails;

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getRootUserId() {
        return rootUserId;
    }

    public void setRootUserId(String rootUserId) {
        this.rootUserId = rootUserId;
    }

    public TwoFADetails getTwoFADetails() {
        return twoFADetails;
    }

    public void setTwoFADetails(TwoFADetails twoFADetails) {
        this.twoFADetails = twoFADetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("paymentMode", paymentMode).append("mid", mid)
                .append("exchangeRate", exchangeRate).append("rootUserId", rootUserId).toString();
    }

}