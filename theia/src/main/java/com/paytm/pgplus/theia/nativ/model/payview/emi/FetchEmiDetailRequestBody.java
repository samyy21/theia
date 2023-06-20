package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.util.List;

public class FetchEmiDetailRequestBody implements Serializable {

    @JsonProperty("channelCode")
    private String channelCode;

    @JsonProperty("amount")
    private Money amount;

    @JsonProperty("emiType")
    private String emiType;

    @JsonProperty("mid")
    private String mid;

    private final static long serialVersionUID = 583745953026217847L;
    private String orderId;

    public Money getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(Money originalAmount) {
        this.originalAmount = originalAmount;
    }

    public List<String> getApplicableTenures() {
        return applicableTenures;
    }

    public void setApplicableTenures(List<String> applicableTenures) {
        this.applicableTenures = applicableTenures;
    }

    private Money originalAmount;
    private List<String> applicableTenures;

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchEmiDetailRequestBody{");
        sb.append("channelCode='").append(channelCode).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", emiType=").append(emiType);
        sb.append(", mid=").append(mid);
        sb.append(", originalAmount=").append(originalAmount);
        sb.append(", applicableTenures=").append(applicableTenures);
        sb.append('}');
        return sb.toString();
    }
}
