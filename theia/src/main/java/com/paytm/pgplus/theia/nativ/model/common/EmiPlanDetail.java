package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.Money;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiPlanDetail implements Serializable {

    private static final long serialVersionUID = -5345999164305294581L;

    private String planId;
    private int ofMonths;
    private String interestRate;
    private String emiFee;
    private Money minAmount;
    private Money maxAmount;
    private Money emiAmount;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public int getOfMonths() {
        return ofMonths;
    }

    public void setOfMonths(int ofMonths) {
        this.ofMonths = ofMonths;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getEmiFee() {
        return emiFee;
    }

    public void setEmiFee(String emiFee) {
        this.emiFee = emiFee;
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

    public Money getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Money emiAmount) {
        this.emiAmount = emiAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmiPlanDetail{");
        sb.append("planId='").append(planId).append('\'');
        sb.append(", ofMonths=").append(ofMonths);
        sb.append(", interestRate='").append(interestRate).append('\'');
        sb.append(", emiFee='").append(emiFee).append('\'');
        sb.append(", minAmount=").append(minAmount);
        sb.append(", maxAmount=").append(maxAmount);
        sb.append(", emiAmount=").append(emiAmount);
        sb.append('}');
        return sb.toString();
    }

}
