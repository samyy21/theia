package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.theia.models.UPIHandleInfo;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIContent implements Serializable {

    private static final long serialVersionUID = -3956916579578396066L;

    @JsonProperty("merchantId")
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
    private String upiStatusUrl;

    @JsonProperty("MERCHANT_VPA")
    private String merchantVPA;

    @JsonIgnore
    private MerchantVpaTxnInfo merchantVpaTxnInfo;

    private UPIHandleInfo upiHandleInfo;

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

    public String getUpiStatusUrl() {
        return upiStatusUrl;
    }

    public void setUpiStatusUrl(String upiStatusUrl) {
        this.upiStatusUrl = upiStatusUrl;
    }

    public String getMerchantVPA() {
        return merchantVPA;
    }

    public void setMerchantVPA(String merchantVPA) {
        this.merchantVPA = merchantVPA;
    }

    public MerchantVpaTxnInfo getMerchantVpaTxnInfo() {
        return merchantVpaTxnInfo;
    }

    public void setMerchantVpaTxnInfo(MerchantVpaTxnInfo merchantVpaTxnInfo) {
        this.merchantVpaTxnInfo = merchantVpaTxnInfo;
    }

    public UPIHandleInfo getUpiHandleInfo() {
        return upiHandleInfo;
    }

    public void setUpiHandleInfo(UPIHandleInfo upiHandleInfo) {
        this.upiHandleInfo = upiHandleInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UPIContent{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", txnId='").append(txnId).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", timeInterval='").append(timeInterval).append('\'');
        sb.append(", timeOut='").append(timeOut).append('\'');
        sb.append(", vpaID='").append(vpaID).append('\'');
        sb.append(", isSelfPush=").append(isSelfPush);
        sb.append(", upiAccepted=").append(upiAccepted);
        sb.append(", upiStatusUrl='").append(upiStatusUrl).append('\'');
        sb.append(", merchantVPA='").append(merchantVPA).append('\'');
        sb.append(", upiHandleInfo='").append(upiHandleInfo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
