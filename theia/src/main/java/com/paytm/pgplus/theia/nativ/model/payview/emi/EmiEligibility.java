package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiEligibility implements Serializable {

    private static final long serialVersionUID = -1419156840785873386L;

    private String channelCode;

    private boolean isEligible;

    private String emiType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public EmiEligibility() {
    }

    public EmiEligibility(String channelCode, boolean isEligible, String emiType, String message) {
        this.channelCode = channelCode;
        this.isEligible = isEligible;
        this.emiType = emiType;
        this.message = message;
    }

    public EmiEligibility(String channelCode, boolean isEligible, String emiType) {
        this.channelCode = channelCode;
        this.isEligible = isEligible;
        this.emiType = emiType;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
