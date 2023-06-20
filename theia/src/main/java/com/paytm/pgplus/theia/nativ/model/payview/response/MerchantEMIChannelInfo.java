package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.models.Money;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantEMIChannelInfo implements Serializable {

    private static final long serialVersionUID = -6126329370405179313L;

    private String emiId;
    private String planId;
    private String interestRate;
    private String ofMonths;
    private Money minAmount;
    private Money maxAmount;
    private String bankId;

    public String getEmiId() {
        return emiId;
    }

    public void setEmiId(String emiId) {
        this.emiId = emiId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getOfMonths() {
        return ofMonths;
    }

    public void setOfMonths(String ofMonths) {
        this.ofMonths = ofMonths;
    }

    public Money getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Money minAmount) {
        this.minAmount = minAmount;
    }

    public Money getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Money maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
