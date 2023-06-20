package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.enums.ERequestType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetailRequestBody extends RequestBody {

    private static final long serialVersionUID = -274575179046736657L;

    @NotBlank(message = "bin cannot be blank")
    @Length(min = 6, message = "bin cannot be less than 6 characters")
    private String bin;

    @NotBlank(message = "orderId cannot be blank")
    private String orderId;

    private ERequestType requestType;

    private String channelCode;

    private String emiType;

    private String payMode;

    private String mid;

    public ERequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ERequestType requestType) {
        this.requestType = requestType;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinDetailRequestBody{");
        sb.append("bin='").append(bin).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", requestType=").append(requestType);
        sb.append(", channelCode='").append(channelCode).append('\'');
        sb.append(", emiType=").append(emiType);
        sb.append(", payMode='").append(payMode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
