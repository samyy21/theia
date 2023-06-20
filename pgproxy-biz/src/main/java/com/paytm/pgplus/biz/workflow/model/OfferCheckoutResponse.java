package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.ItemBreakUp;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferCheckoutResponse implements Serializable {

    private static final long serialVersionUID = 7922907919678879315L;

    private String resultStatus;
    private String resultCode;
    private String resultMsg;
    private String inputFlag;
    private String outputFlag;
    EdcLinkEmiSubventionDetail subventionDetail;
    EdcLinkEmiBankOfferCheckout bankOfferDetails;

    public EdcLinkEmiBankOfferCheckout getBankOfferDetails() {
        return bankOfferDetails;
    }

    public void setBankOfferDetails(EdcLinkEmiBankOfferCheckout bankOfferDetails) {
        this.bankOfferDetails = bankOfferDetails;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public EdcLinkEmiSubventionDetail getSubventionDetail() {
        return subventionDetail;
    }

    public void setSubventionDetail(EdcLinkEmiSubventionDetail subventionDetail) {
        this.subventionDetail = subventionDetail;
    }

    public String getInputFlag() {
        return inputFlag;
    }

    public void setInputFlag(String inputFlag) {
        this.inputFlag = inputFlag;
    }

    public String getOutputFlag() {
        return outputFlag;
    }

    public void setOutputFlag(String outputFlag) {
        this.outputFlag = outputFlag;
    }
}
