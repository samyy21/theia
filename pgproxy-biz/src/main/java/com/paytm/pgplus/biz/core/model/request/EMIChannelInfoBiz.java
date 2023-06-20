/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class EMIChannelInfoBiz implements Serializable {

    private static final long serialVersionUID = 14L;

    private String planId;
    private String tenureId;
    private String interestRate;
    private String ofMonths;
    private String minAmount;
    private String maxAmount;
    private String cardAcquiringMode;
    private String perInstallment;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(final String planId) {
        this.planId = planId;
    }

    public String getTenureId() {
        return tenureId;
    }

    public void setTenureId(final String tenureId) {
        this.tenureId = tenureId;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(final String interestRate) {
        this.interestRate = interestRate;
    }

    public String getOfMonths() {
        return ofMonths;
    }

    public void setOfMonths(final String ofMonths) {
        this.ofMonths = ofMonths;
    }

    public String getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(final String minAmount) {
        this.minAmount = minAmount;
    }

    public String getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(final String maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getCardAcquiringMode() {
        return cardAcquiringMode;
    }

    public void setCardAcquiringMode(final String cardAcquiringMode) {
        this.cardAcquiringMode = cardAcquiringMode;
    }

    /**
     * @return the perInstallment
     */
    public String getPerInstallment() {
        return perInstallment;
    }

    /**
     * @param perInstallment
     *            the perInstallment to set
     */
    public void setPerInstallment(String perInstallment) {
        this.perInstallment = perInstallment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EMIChannelInfoBiz [planId=").append(planId).append(", tenureId=").append(tenureId)
                .append(", interestRate=").append(interestRate).append(", ofMonths=").append(ofMonths)
                .append(", minAmount=").append(minAmount).append(", maxAmount=").append(maxAmount)
                .append(", cardAcquiringMode=").append(cardAcquiringMode).append("]");
        return builder.toString();
    }
}
