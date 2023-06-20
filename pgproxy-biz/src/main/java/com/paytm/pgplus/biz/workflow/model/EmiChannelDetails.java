package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class EmiChannelDetails implements Serializable {

    private static final long serialVersionUID = 3941287197604544885L;

    private String itemId;
    private String planId;
    private String pgPlanId;
    private String bankName;
    private String interestRate;
    private String emiMonths;
    private Money emiAmount;
    private Money totalAmount;
    private Money interestAmount;
    private String subventionType;
    private List<OfferDetails> offerDetails;
    private List<BankOfferDetails> bankOfferDetails;
    private String message;
    private String templateId;
    private EmiTypeChange emiTypeChangeObj;
    private String bankVerificationCode;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getEmiMonths() {
        return emiMonths;
    }

    public void setEmiMonths(String emiMonths) {
        this.emiMonths = emiMonths;
    }

    public Money getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Money emiAmount) {
        this.emiAmount = emiAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Money getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(Money interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getSubventionType() {
        return subventionType;
    }

    public void setSubventionType(String subventionType) {
        this.subventionType = subventionType;
    }

    public List<OfferDetails> getOfferDetails() {
        return offerDetails;
    }

    public void setOfferDetails(List<OfferDetails> offerDetails) {
        this.offerDetails = offerDetails;
    }

    public List<BankOfferDetails> getBankOfferDetails() {
        return bankOfferDetails;
    }

    public void setBankOfferDetails(List<BankOfferDetails> bankOfferDetails) {
        this.bankOfferDetails = bankOfferDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public EmiTypeChange getEmiTypeChangeObj() {
        return emiTypeChangeObj;
    }

    public void setEmiTypeChangeObj(EmiTypeChange emiTypeChangeObj) {
        this.emiTypeChangeObj = emiTypeChangeObj;
    }

    public String getBankVerificationCode() {
        return bankVerificationCode;
    }

    public void setBankVerificationCode(String bankVerificationCode) {
        this.bankVerificationCode = bankVerificationCode;
    }

    @Override
    public String toString() {
        return "EmiChannelDetails{" + "itemId='" + itemId + '\'' + ", planId='" + planId + '\'' + ", pgPlanId='"
                + pgPlanId + '\'' + ", bankName='" + bankName + '\'' + ", interestRate='" + interestRate + '\''
                + ", emiMonths='" + emiMonths + '\'' + ", emiAmount=" + emiAmount + ", totalAmount=" + totalAmount
                + ", interestAmount=" + interestAmount + ", subventionType='" + subventionType + '\''
                + ", offerDetails=" + offerDetails + ", bankOfferDetails=" + bankOfferDetails + ", message='" + message
                + '\'' + ", templateId='" + templateId + '\'' + ", emiTypeChangeObj=" + emiTypeChangeObj + '\''
                + ", bankVerificationCode='" + bankVerificationCode + '}';
    }
}