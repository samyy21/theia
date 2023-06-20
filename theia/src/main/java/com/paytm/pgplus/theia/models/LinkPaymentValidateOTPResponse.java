package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

import java.io.Serializable;

/**
 * @author Raman Preet Singh
 * @since 30/10/17
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkPaymentValidateOTPResponse implements Serializable {

    private static final long serialVersionUID = -6615078545845554086L;

    private String status;
    private String message;
    private String orderId;
    private Integer retryCount;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String ssoToken;
    private String custId;

    public LinkPaymentValidateOTPResponse(String status, String message, String orderId, Integer retryCount,
            String ssoToken, String custId) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.retryCount = retryCount;
        this.ssoToken = ssoToken;
        this.custId = custId;
    }

    public LinkPaymentValidateOTPResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
