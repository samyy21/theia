/**
 * 
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;

/**
 * @author amit.dubey
 *
 */
public class DoPaymentResponse implements Serializable {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = -8880660694265587778L;

    private CashierPaymentStatus paymentStatus;
    private CashierTransactionStatus transactionStatus;
    private CashierFundOrderStatus fundOrderStatus;

    /**
     * @return the paymentStatus
     */
    public CashierPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * @param paymentStatus
     *            the paymentStatus to set
     */
    public void setPaymentStatus(CashierPaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /**
     * @return the transactionStatus
     */
    public CashierTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * @param transactionStatus
     *            the transactionStatus to set
     */
    public void setTransactionStatus(CashierTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public CashierFundOrderStatus getFundOrderStatus() {
        return fundOrderStatus;
    }

    public void setFundOrderStatus(CashierFundOrderStatus fundOrderStatus) {
        this.fundOrderStatus = fundOrderStatus;
    }

}
