package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

public class EnhancedResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 3679144691766075678L;

    @JsonProperty("MID")
    private String mid;
    @JsonProperty("ORDER_ID")
    private String orderId;
    @JsonProperty("transId")
    private String txnId;
    @JsonProperty("TXN_AMOUNT")
    private String txnAmount;
    private String cashierRequestId;
    private String paymentMode;
    @JsonProperty("STATUS_INTERVAL")
    private String timeInterval;
    @JsonProperty("STATUS_TIMEOUT")
    private String timeOut;
    private String vpaID;
    private boolean isSelfPush;
    private boolean upiAccepted;
    private String callbackUrl;
    private String upiStatusUrl;

    public EnhancedResponseBody() {
        ResultInfo resultInfo = new ResultInfo("S", "SUCCESS", "Success", true);
        super.setResultInfo(resultInfo);
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

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getVpaID() {
        return vpaID;
    }

    public void setVpaID(String vpaID) {
        this.vpaID = vpaID;
    }

    public boolean isSelfPush() {
        return isSelfPush;
    }

    public void setSelfPush(boolean selfPush) {
        isSelfPush = selfPush;
    }

    public boolean isUpiAccepted() {
        return upiAccepted;
    }

    public void setUpiAccepted(boolean upiAccepted) {
        this.upiAccepted = upiAccepted;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getUpiStatusUrl() {
        return upiStatusUrl;
    }

    public void setUpiStatusUrl(String upiStatusUrl) {
        this.upiStatusUrl = upiStatusUrl;
    }

    @Override
    public String toString() {
        return "EnhancedResponseBody{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\'' + ", txnId='" + txnId
                + '\'' + ", txnAmount='" + txnAmount + '\'' + ", cashierRequestId='" + cashierRequestId + '\''
                + ", paymentMode='" + paymentMode + '\'' + ", timeInterval='" + timeInterval + '\'' + ", timeOut='"
                + timeOut + '\'' + ", vpaID='" + vpaID + '\'' + ", isSelfPush=" + isSelfPush + ", upiAccepted="
                + upiAccepted + ", callbackUrl='" + callbackUrl + '\'' + ", upiStatusUrl='" + upiStatusUrl + '\'' + '}';
    }
}
