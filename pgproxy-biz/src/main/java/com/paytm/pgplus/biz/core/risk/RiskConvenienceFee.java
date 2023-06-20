package com.paytm.pgplus.biz.core.risk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.enums.PayMethod;

import java.io.Serializable;
import java.util.Map;

public class RiskConvenienceFee implements Serializable {

    private static final long serialVersionUID = -9020481872879034654L;

    private PayMethod payMethod;
    private String feePercent;
    private String reason;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, Object> feeDetails;

    public Map<String, Object> getFeeDetails() {
        return feeDetails;
    }

    public void setFeeDetails(Map<String, Object> feeDetails) {
        this.feeDetails = feeDetails;
    }

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public String getFeePercent() {
        return feePercent;
    }

    public void setFeePercent(String feePercent) {
        this.feePercent = feePercent;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RiskConvenienceFee{");
        sb.append("payMethod=").append(payMethod);
        sb.append(", feePercent='").append(feePercent).append('\'');
        sb.append(", reason='").append(reason).append('\'');
        sb.append(", feeDetails='").append(feeDetails).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
