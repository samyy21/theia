/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Map;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.consume.models.Money;

public class ConsultFeeResponse implements Serializable {

    private static final long serialVersionUID = -254980292839816905L;

    private Map<EPayMethod, ConsultDetails> consultDetails;
    private boolean isSuccessfullyProcessed;
    private String failureReason;
    private Money chargeAmount;

    public Money getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(Money chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public Map<EPayMethod, ConsultDetails> getConsultDetails() {
        return consultDetails;
    }

    public void setConsultDetails(final Map<EPayMethod, ConsultDetails> consultDetails) {
        this.consultDetails = consultDetails;
    }

    /**
     * @return the isSuccessfullyProcessed
     */
    public boolean isSuccessfullyProcessed() {
        return isSuccessfullyProcessed;
    }

    /**
     * @return the failureReason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * @param consultDetails
     */
    public ConsultFeeResponse(final Map<EPayMethod, ConsultDetails> consultDetails) {
        this.consultDetails = consultDetails;
        this.isSuccessfullyProcessed = true;
    }

    public ConsultFeeResponse(final Map<EPayMethod, ConsultDetails> consultDetails, Money chargeAmount) {
        this.consultDetails = consultDetails;
        this.isSuccessfullyProcessed = true;
        this.chargeAmount = chargeAmount;
    }

    /**
     * @param failureReason
     */
    public ConsultFeeResponse(final String failureReason) {
        this.failureReason = failureReason;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ConsultFeeResponse [consultDetails=").append(consultDetails)
                .append(", isSuccessfullyProcessed=").append(isSuccessfullyProcessed).append(", failureReason=")
                .append(failureReason).append("]");
        return builder.toString();
    }

}