package com.paytm.pgplus.theia.nativ.model.risk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponseBody;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskVerificationPageData implements Serializable {

    private static final long serialVersionUID = 2048860049157868170L;

    private String token;

    private String mid;

    private String orderId;

    private DoViewResponseBody firstDoviewResponse;

    private String callbackUrl;

    private String cancelTransactionUrl;

    private String txnToken;

    private String iframeUrl;

    private boolean openIframe;

    public RiskVerificationPageData() {
    }

    public RiskVerificationPageData(String txnToken, String mid, String orderId,
            DoViewResponseBody firstDoviewResponse, String callbackUrl, String cancelTransactionUrl) {
        this.token = txnToken;
        this.mid = mid;
        this.orderId = orderId;
        this.firstDoviewResponse = firstDoviewResponse;
        this.callbackUrl = callbackUrl;
        this.cancelTransactionUrl = cancelTransactionUrl;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public DoViewResponseBody getFirstDoviewResponse() {
        return firstDoviewResponse;
    }

    public void setFirstDoviewResponse(DoViewResponseBody firstDoviewResponse) {
        this.firstDoviewResponse = firstDoviewResponse;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCancelTransactionUrl() {
        return cancelTransactionUrl;
    }

    public void setCancelTransactionUrl(String cancelTransactionUrl) {
        this.cancelTransactionUrl = cancelTransactionUrl;
    }

    public String getIframeUrl() {
        return iframeUrl;
    }

    public void setIframeUrl(String iframeUrl) {
        this.iframeUrl = iframeUrl;
    }

    public boolean isOpenIframe() {
        return openIframe;
    }

    public void setOpenIframe(boolean openIframe) {
        this.openIframe = openIframe;
    }

    @Override
    public String toString() {
        return "RiskVerificationPageData{" + "token='" + token + '\'' + ", mid='" + mid + '\'' + ", orderId='"
                + orderId + '\'' + ", firstDoviewResponse=" + firstDoviewResponse + ", callbackUrl='" + callbackUrl
                + '\'' + ", cancelTransactionUrl='" + cancelTransactionUrl + '\'' + ", openIframe='" + openIframe + '}';
    }
}
