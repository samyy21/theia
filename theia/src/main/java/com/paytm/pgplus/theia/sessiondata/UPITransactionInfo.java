package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;
import java.util.Map;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.common.model.MerchantVpaTxnInfo;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.theia.models.UPIHandleInfo;

/**
 * Created by prashant on 1/17/17.
 */
public class UPITransactionInfo implements Serializable {

    private static final long serialVersionUID = -988315876758827296L;

    @Tag(value = 1)
    private String baseUrl;
    @Tag(value = 2)
    private String statusInterval;
    @Tag(value = 3)
    private String statusTimeOut;

    @Tag(value = 4)
    private String acquirementId;
    @Tag(value = 5)
    private String merchantTransId;
    @Tag(value = 6)
    private String cashierRequestId;
    @Tag(value = 7)
    private String paymentMode;

    @Tag(value = 8)
    private String alipayMerchantId;
    @Tag(value = 9)
    private String paytmMerchantId;

    @Tag(value = 10)
    private String transactionAmount;
    @Tag(value = 11)
    private String vpaID;
    @Tag(value = 12)
    private boolean isSelfPush;
    @Tag(value = 13)
    private boolean paytmVpa;
    @Tag(value = 14)
    private MerchantVpaTxnInfo merchantVpaTxnInfo;
    @Tag(value = 15)
    private String upiPollBaseUrl;
    @Tag(value = 16)
    private String handlerName;
    @LocaleField
    @Tag(value = 17)
    private String displayName;
    @Tag(value = 18)
    private UPIHandleInfo upiHandleInfo;
    @Tag(value = 19)
    private Map<String, Object> localePushAppData;

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getAlipayMerchantId() {
        return alipayMerchantId;
    }

    public void setAlipayMerchantId(String alipayMerchantId) {
        this.alipayMerchantId = alipayMerchantId;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public String getMerchantTransId() {
        return merchantTransId;
    }

    public void setMerchantTransId(String merchantTransId) {
        this.merchantTransId = merchantTransId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getStatusInterval() {
        return statusInterval;
    }

    public void setStatusInterval(String statusInterval) {
        this.statusInterval = statusInterval;
    }

    public String getStatusTimeOut() {
        return statusTimeOut;
    }

    public void setStatusTimeOut(String statusTimeOut) {
        this.statusTimeOut = statusTimeOut;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getVpaID() {
        return vpaID;
    }

    public void setVpaID(String vpaID) {
        this.vpaID = vpaID;
    }

    public boolean getSelfPush() {
        return isSelfPush;
    }

    public void setSelfPush(boolean isSelfPush) {
        this.isSelfPush = isSelfPush;
    }

    public boolean isPaytmVpa() {
        return paytmVpa;
    }

    public void setPaytmVpa(boolean paytmVpa) {
        this.paytmVpa = paytmVpa;
    }

    public MerchantVpaTxnInfo getMerchantVpaTxnInfo() {
        return merchantVpaTxnInfo;
    }

    public void setMerchantVpaTxnInfo(MerchantVpaTxnInfo merchantVpaTxnInfo) {
        this.merchantVpaTxnInfo = merchantVpaTxnInfo;
    }

    public String getUpiPollBaseUrl() {
        return upiPollBaseUrl;
    }

    public void setUpiPollBaseUrl(String upiPollBaseUrl) {
        this.upiPollBaseUrl = upiPollBaseUrl;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UPIHandleInfo getUpiHandleInfo() {
        return upiHandleInfo;
    }

    public void setUpiHandleInfo(UPIHandleInfo upiHandleInfo) {
        this.upiHandleInfo = upiHandleInfo;
    }

    public Map<String, Object> getLocalePushAppData() {
        return localePushAppData;
    }

    public void setLocalePushAppData(Map<String, Object> localePushAppData) {
        this.localePushAppData = localePushAppData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UPITransactionInfo{");
        sb.append("acquirementId='").append(acquirementId).append('\'');
        sb.append("baseUrl='").append(baseUrl).append('\'');
        sb.append(", statusInterval='").append(statusInterval).append('\'');
        sb.append(", statusTimeOut='").append(statusTimeOut).append('\'');
        sb.append(", acquirementId='").append(acquirementId).append('\'');
        sb.append(", merchantTransId='").append(merchantTransId).append('\'');
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", alipayMerchantId='").append(alipayMerchantId).append('\'');
        sb.append(", paytmMerchantId='").append(paytmMerchantId).append('\'');
        sb.append(", transactionAmount='").append(transactionAmount).append('\'');
        sb.append(", vpaID='").append(vpaID).append('\'');
        sb.append(", isSelfPush=").append(isSelfPush);
        sb.append(", paytmVpa=").append(paytmVpa);
        sb.append(", upiPollBaseUrl=").append(upiPollBaseUrl);
        sb.append(", upiHandleInfo=").append(upiHandleInfo);
        sb.append(", localePushAppData=").append(localePushAppData);
        sb.append('}');
        return sb.toString();
    }
}
