package com.paytm.pgplus.biz.core.risk;

import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponseBody;

import java.io.Serializable;

public class RiskVerifierPayload implements Serializable {

    private static final long serialVersionUID = 62197949411538866L;

    private String acquirementId;

    private String mid;

    private String orderId;

    private RiskVerifierDoViewResponseBody riskVerifierDoViewResponseBody;

    private String txnToken;

    public RiskVerifierPayload(String acquirementId, String mid, String orderId,
            RiskVerifierDoViewResponseBody riskVerifierDoViewResponseBody) {
        this.acquirementId = acquirementId;
        this.mid = mid;
        this.orderId = orderId;
        this.riskVerifierDoViewResponseBody = riskVerifierDoViewResponseBody;
    }

    public String getAcquirementId() {
        return acquirementId;
    }

    public String getMid() {
        return mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public RiskVerifierDoViewResponseBody getRiskVerifierDoViewResponseBody() {
        return riskVerifierDoViewResponseBody;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }
}
