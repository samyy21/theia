/**
 * 
 */
package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class EMIInfo implements Serializable {

    private static final long serialVersionUID = -2404958635753334628L;
    @Tag(value = 1)
    private String instId;
    @Tag(value = 2)
    private String planId;
    @Tag(value = 3)
    private String tenureId;
    @Tag(value = 4)
    private String aggregatorPlanId;
    @Tag(value = 5)
    private String interestRate;
    @Tag(value = 6)
    private String ofMonths;
    @Tag(value = 7)
    private String minAmount;
    @Tag(value = 8)
    private String maxAmount;
    @Tag(value = 9)
    private String cardAcquiringMode;
    @Tag(value = 10)
    private String emiAmount;
    @Tag(value = 11)
    private boolean aggregator;
    @Tag(value = 12)
    private boolean ajaxRequired;

    public boolean isAggregator() {
        return aggregator;
    }

    public void setAggregator(boolean aggregator) {
        this.aggregator = aggregator;
    }

    public boolean isAjaxRequired() {
        return ajaxRequired;
    }

    public void setAjaxRequired(boolean ajaxRequired) {
        this.ajaxRequired = ajaxRequired;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

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
     * @return the emiAmount
     */
    public String getEmiAmount() {
        return emiAmount;
    }

    /**
     * @param emiAmount
     *            the emiAmount to set
     */
    public void setEmiAmount(String emiAmount) {
        this.emiAmount = emiAmount;
    }

    public String getAggregatorPlanId() {
        return aggregatorPlanId;
    }

    public void setAggregatorPlanId(String aggregatorPlanId) {
        this.aggregatorPlanId = aggregatorPlanId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EMIInfo [instId=").append(instId).append(", planId=").append(planId).append(", tenureId=")
                .append(tenureId).append(", aggregatorPlanId=").append(aggregatorPlanId).append(", interestRate=")
                .append(interestRate).append(", ofMonths=").append(ofMonths).append(", minAmount=").append(minAmount)
                .append(", maxAmount=").append(maxAmount).append(", cardAcquiringMode=").append(cardAcquiringMode)
                .append(", emiAmount=").append(emiAmount).append(", aggregator=").append(aggregator)
                .append(", ajaxRequired=").append(ajaxRequired).append("]");
        return builder.toString();
    }
}