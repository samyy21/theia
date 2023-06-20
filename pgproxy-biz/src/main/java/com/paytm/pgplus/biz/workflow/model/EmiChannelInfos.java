package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.util.Map;

public class EmiChannelInfos implements Serializable {

    private static final long serialVersionUID = -791637979485831669L;

    private String planId;
    private String bankName;
    private String interestRate;
    private String emiMonths;
    private Money emiAmount;
    private Money totalAmount;
    private Money interestAmount;
    private String message;
    private String templateId;
    private String bankVerificationCode;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
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

    public String getBankVerificationCode() {
        return bankVerificationCode;
    }

    public void setBankVerificationCode(String bankVerificationCode) {
        this.bankVerificationCode = bankVerificationCode;
    }

    @Override
    public String toString() {
        return "EmiChannelInfos{" + "planId='" + planId + '\'' + ", bankName='" + bankName + '\'' + ", interestRate='"
                + interestRate + '\'' + ", emiMonths='" + emiMonths + '\'' + ", emiAmount=" + emiAmount
                + ", totalAmount=" + totalAmount + ", interestAmount=" + interestAmount + ", message='" + message
                + '\'' + ", templateId='" + templateId + '\'' + ", bankVerificationCode='" + bankVerificationCode
                + '\'' + '}';
    }
}
