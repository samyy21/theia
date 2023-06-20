package com.paytm.pgplus.theia.models;

public class GvConsentPagePayload {

    private String mid;

    private String orderId;

    private String callbackUrl;

    private String cancelTxnUrl;

    private String token;

    private boolean expressAddMoneyToGv;

    private String gvConsentFlowKey;

    public GvConsentPagePayload(String mid, String orderId, String callbackUrl, String cancelTxnUrl, String token,
            Boolean expressAddMoneyToGv, String gvConsentFlowKey) {
        this.mid = mid;
        this.orderId = orderId;
        this.callbackUrl = callbackUrl;
        this.cancelTxnUrl = cancelTxnUrl;
        this.token = token;
        this.expressAddMoneyToGv = expressAddMoneyToGv;
        this.gvConsentFlowKey = gvConsentFlowKey;
    }

    public String getMid() {
        return mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getToken() {
        return token;
    }

    public boolean isExpressAddMoneyToGv() {
        return expressAddMoneyToGv;
    }

    public String getCancelTxnUrl() {
        return cancelTxnUrl;
    }

    public String getGvConsentFlowKey() {
        return gvConsentFlowKey;
    }

    @Override
    public String toString() {
        return "GvConsentPagePayload{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\'' + ", callbackUrl='"
                + callbackUrl + '\'' + ", cancelTxnUrl='" + cancelTxnUrl + '\'' + ", token='" + token + '\''
                + ", expressAddMoneyToGv=" + expressAddMoneyToGv + ", gvConsentFlowKey='" + gvConsentFlowKey + '\''
                + '}';
    }
}
