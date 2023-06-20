/**
 *
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.common.model.ConsultDetails;

/**
 * @author amit.dubey
 *
 */
/**
 * @author amit.dubey
 *
 */
public class InitiatePaymentResponse implements Serializable {

    private static final long serialVersionUID = -8517264175144979144L;

    private String cacheCardTokenId;
    private String cashierRequestId;
    private CashierPaymentStatus cashierPaymentStatus;
    private ConsultDetails consultDetails;

    /**
     * @return the cacheCardTokenId
     */
    public String getCacheCardTokenId() {
        return cacheCardTokenId;
    }

    /**
     * @param cacheCardTokenId
     *            the cacheCardTokenId to set
     */
    public void setCacheCardTokenId(String cacheCardTokenId) {
        this.cacheCardTokenId = cacheCardTokenId;
    }

    /**
     * @return the cashierRequestId
     */
    public String getCashierRequestId() {
        return cashierRequestId;
    }

    /**
     * @param cashierRequestId
     *            the cashierRequestId to set
     */
    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    /**
     * @return the cashierPaymentStatus
     */
    public CashierPaymentStatus getCashierPaymentStatus() {
        return cashierPaymentStatus;
    }

    /**
     * @param cashierPaymentStatus
     *            the cashierPaymentStatus to set
     */
    public void setCashierPaymentStatus(CashierPaymentStatus cashierPaymentStatus) {
        this.cashierPaymentStatus = cashierPaymentStatus;
    }

    /**
     * @return the consultDetails
     */
    public ConsultDetails getConsultDetails() {
        return consultDetails;
    }

    /**
     * @param consultDetails
     *            the consultDetails to set
     */
    public void setConsultDetails(ConsultDetails consultDetails) {
        this.consultDetails = consultDetails;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InitiatePaymentResponse [cacheCardTokenId=").append(cacheCardTokenId)
                .append(", cashierRequestId=").append(cashierRequestId).append(", cashierPaymentStatus=")
                .append(cashierPaymentStatus).append(", consultDetails=").append(consultDetails).append("]");
        return builder.toString();
    }
}