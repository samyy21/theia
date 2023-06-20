package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.models.Money;

import java.io.Serializable;

public class EnhancedCashierEmiPlanData implements Serializable {

    private static final long serialVersionUID = -4422385670088735274L;
    private String planId;

    @JsonIgnore
    private String tenureId;
    private String interestRate;
    private String ofMonths;

    private Money minAmount;
    private Money maxAmount;

    @JsonIgnore
    private CardAcquiringMode cardAcquiringMode;

    @JsonIgnore
    private String perInstallment;

    private Money emiAmount;

    private boolean selected;

    private Money totalAmount;

    public EnhancedCashierEmiPlanData(String planId, String tenureId, String interestRate, String ofMonths,
            Money minAmount, Money maxAmount, CardAcquiringMode cardAcquiringMode, String perInstallment,
            Money emiAmount, Money totalAmount) {
        this.planId = planId;
        this.tenureId = tenureId;
        this.interestRate = interestRate;
        this.ofMonths = ofMonths;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.cardAcquiringMode = cardAcquiringMode;
        this.perInstallment = perInstallment;
        this.emiAmount = emiAmount;
        this.totalAmount = totalAmount;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getTenureId() {
        return tenureId;
    }

    public void setTenureId(String tenureId) {
        this.tenureId = tenureId;
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

    public CardAcquiringMode getCardAcquiringMode() {
        return cardAcquiringMode;
    }

    public void setCardAcquiringMode(CardAcquiringMode cardAcquiringMode) {
        this.cardAcquiringMode = cardAcquiringMode;
    }

    public String getPerInstallment() {
        return perInstallment;
    }

    public void setPerInstallment(String perInstallment) {
        this.perInstallment = perInstallment;
    }

    public Money getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Money emiAmount) {
        this.emiAmount = emiAmount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "EnhancedCashierEmiPlanData{" + "planId='" + planId + '\'' + ", tenureId='" + tenureId + '\''
                + ", interestRate='" + interestRate + '\'' + ", ofMonths='" + ofMonths + '\'' + ", minAmount="
                + minAmount + ", maxAmount=" + maxAmount + ", cardAcquiringMode=" + cardAcquiringMode
                + ", perInstallment='" + perInstallment + '\'' + ", emiAmount=" + emiAmount + ", selected=" + selected
                + '}';
    }
}
