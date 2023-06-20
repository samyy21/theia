package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.enums.CardAcquiringMode;
import com.paytm.pgplus.models.Money;

import java.io.Serializable;

/**
 * Created by rahulverma on 28/8/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EMIChannelInfo implements Serializable {

    private static final long serialVersionUID = 5649411063053317863L;
    private String planId;
    private String tenureId;
    private String interestRate;
    private String ofMonths;
    private Money minAmount;
    private Money maxAmount;
    private CardAcquiringMode cardAcquiringMode;
    private String perInstallment;

    public EMIChannelInfo() {
    }

    public EMIChannelInfo(String planId, String tenureId, String interestRate, String ofMonths, Money minAmount,
            Money maxAmount, CardAcquiringMode cardAcquiringMode, String perInstallment) {
        this.planId = planId;
        this.tenureId = tenureId;
        this.interestRate = interestRate;
        this.ofMonths = ofMonths;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.cardAcquiringMode = cardAcquiringMode;
        this.perInstallment = perInstallment;
    }

    public String getPlanId() {
        return this.planId;
    }

    public String getTenureId() {
        return this.tenureId;
    }

    public String getInterestRate() {
        return this.interestRate;
    }

    public String getOfMonths() {
        return this.ofMonths;
    }

    public Money getMinAmount() {
        return this.minAmount;
    }

    public Money getMaxAmount() {
        return this.maxAmount;
    }

    public CardAcquiringMode getCardAcquiringMode() {
        return this.cardAcquiringMode;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public void setTenureId(String tenureId) {
        this.tenureId = tenureId;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public void setOfMonths(String ofMonths) {
        this.ofMonths = ofMonths;
    }

    public void setMinAmount(Money minAmount) {
        this.minAmount = minAmount;
    }

    public void setMaxAmount(Money maxAmount) {
        this.maxAmount = maxAmount;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EMIChannelInfo{");
        sb.append("planId='").append(planId).append('\'');
        sb.append(", tenureId='").append(tenureId).append('\'');
        sb.append(", interestRate='").append(interestRate).append('\'');
        sb.append(", ofMonths='").append(ofMonths).append('\'');
        sb.append(", minAmount=").append(minAmount);
        sb.append(", maxAmount=").append(maxAmount);
        sb.append(", cardAcquiringMode=").append(cardAcquiringMode);
        sb.append(", perInstallment=").append(perInstallment);
        sb.append('}');
        return sb.toString();
    }
}
