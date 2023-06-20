package com.paytm.pgplus.theia.nativ.model.token;

/**
 * Created by chitrasinghal on 30/4/18.
 */
public class TrxInfoResponse {

    private String txntoken;

    private String subscriptionId;

    private boolean idempotent;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getTxntoken() {
        return txntoken;
    }

    public void setTxntoken(String txntoken) {
        this.txntoken = txntoken;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }
}
