package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TheiaCreateAgreementRequestBody implements Serializable {

    private static final long serialVersionUID = -3901738099261246309L;

    private String merchantMid;
    private String merchantAgreementId;
    private Boolean needImmediateActivation;
    private String description;
    private String callBackURL;

    public String getMerchantMid() {
        return merchantMid;
    }

    public void setMerchantMid(String merchantMid) {
        this.merchantMid = merchantMid;
    }

    public String getMerchantAgreementId() {
        return merchantAgreementId;
    }

    public void setMerchantAgreementId(String merchantAgreementId) {
        this.merchantAgreementId = merchantAgreementId;
    }

    public Boolean getNeedImmediateActivation() {
        return needImmediateActivation;
    }

    public void setNeedImmediateActivation(Boolean needImmediateActivation) {
        this.needImmediateActivation = needImmediateActivation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TheiaCreateAgreementRequestBody[ merchantMid=").append(merchantMid)
                .append(", merchantAgreementId=").append(merchantAgreementId).append(", needImmediateActivation=")
                .append(needImmediateActivation).append(", description=").append(description).append(", callBackURL=")
                .append(callBackURL).append("]");
        return builder.toString();
    }
}
