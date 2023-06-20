package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.ItemBreakUp;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdcLinkEmiSubventionDetail implements Serializable {

    private static final long serialVersionUID = -5514107429887502154L;
    private String status;
    private String error;
    private String bankId;
    private String bankName;
    private String bankCode;
    private String cardType;
    private String bankLogoUrl;
    private String planId;
    private String pgPlanId;
    private String rate;
    private String interval;
    private String emi;
    private String interest;
    private String emiLabel;
    private String message;
    private String emiType;
    private List<Gratification> gratifications;
    private List<ItemBreakUp> itemBreakUp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPgPlanId() {
        return pgPlanId;
    }

    public void setPgPlanId(String pgPlanId) {
        this.pgPlanId = pgPlanId;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getEmi() {
        return emi;
    }

    public void setEmi(String emi) {
        this.emi = emi;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getEmiLabel() {
        return emiLabel;
    }

    public void setEmiLabel(String emiLabel) {
        this.emiLabel = emiLabel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public List<Gratification> getGratifications() {
        return gratifications;
    }

    public void setGratifications(List<Gratification> gratifications) {
        this.gratifications = gratifications;
    }

    public List<ItemBreakUp> getItemBreakUp() {
        return itemBreakUp;
    }

    public void setItemBreakUp(List<ItemBreakUp> itemBreakUp) {
        this.itemBreakUp = itemBreakUp;
    }

    @Override
    public String toString() {
        return "EdcLinkEmiSubventionDetail{" + "status='" + status + '\'' + ", error='" + error + '\'' + ", bankId='"
                + bankId + '\'' + ", bankName='" + bankName + '\'' + ", bankCode='" + bankCode + '\'' + ", cardType='"
                + cardType + '\'' + ", bankLogoUrl='" + bankLogoUrl + '\'' + ", planId='" + planId + '\''
                + ", pgPlanId='" + pgPlanId + '\'' + ", rate='" + rate + '\'' + ", interval='" + interval + '\''
                + ", emi='" + emi + '\'' + ", interest='" + interest + '\'' + ", emiLabel='" + emiLabel + '\''
                + ", message='" + message + '\'' + ", emiType='" + emiType + '\'' + ", gratifications="
                + gratifications + ", itemBreakUp=" + itemBreakUp + '}';
    }
}
