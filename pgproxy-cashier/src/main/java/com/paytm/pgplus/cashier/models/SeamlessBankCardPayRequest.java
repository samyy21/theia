/**
 * 
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;
import java.util.List;

import com.paytm.pgplus.cashier.pay.model.CashierPayOptionBill;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;

/**
 * @author amitdubey
 * @date Dec 2, 2016
 */
public class SeamlessBankCardPayRequest implements Serializable {
    private static final long serialVersionUID = 576934579249007038L;

    private String transactionid;
    private String cashierRequestId;
    private PaymentRequest paymentRequest;
    private List<CashierPayOptionBill> cashierPayOptionBills;

    /**
     * @param transactionid
     * @param cashierRequestId
     * @param paymentRequest
     * @param cashierPayOptionBills
     */
    public SeamlessBankCardPayRequest(String transactionid, String cashierRequestId, PaymentRequest paymentRequest,
            List<CashierPayOptionBill> cashierPayOptionBills) {
        this.transactionid = transactionid;
        this.cashierRequestId = cashierRequestId;
        this.paymentRequest = paymentRequest;
        this.cashierPayOptionBills = cashierPayOptionBills;
    }

    /**
     * @param cashierRequestId
     */
    public SeamlessBankCardPayRequest(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    /**
     * @return the transactionid
     */
    public String getTransactionid() {
        return transactionid;
    }

    /**
     * @return the cashierRequestId
     */
    public String getCashierRequestId() {
        return cashierRequestId;
    }

    /**
     * @return the paymentRequest
     */
    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    /**
     * @return the cashierPayOptionBills
     */
    public List<CashierPayOptionBill> getCashierPayOptionBills() {
        return cashierPayOptionBills;
    }

    public String getRedisKey() {
        StringBuilder sb = new StringBuilder("DIRECT_BANK_CARD_PAYMENT_").append(cashierRequestId);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SeamlessBankCardPayRequest [transactionid=").append(transactionid)
                .append(", cashierRequestId=").append(cashierRequestId).append(", paymentRequest=")
                .append(paymentRequest).append(", cashierPayOptionBills=").append(cashierPayOptionBills).append("]");
        return builder.toString();
    }
}
