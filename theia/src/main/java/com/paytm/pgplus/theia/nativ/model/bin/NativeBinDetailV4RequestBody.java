package com.paytm.pgplus.theia.nativ.model.bin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

public class NativeBinDetailV4RequestBody implements Serializable {

    private static final long serialVersionUID = 9217356638767536380L;

    @Mask(prefixNoMaskLen = 6)
    @NotBlank(message = "bin cannot be blank")
    @Length(min = 6, message = "bin cannot be less than 6 characters")
    private String bin;

    private boolean isEMIDetail;

    @JsonProperty("txnType")
    private String txnType;

    @JsonProperty("bankCode")
    private String channelCode;

    @JsonProperty("emiCardType")
    private String emiType;

    @JsonProperty("paymentMode")
    private String paymentMode;

    @JsonProperty("requestType")
    private ERequestType requestType;

    @JsonProperty
    private String mid;

    @JsonProperty("proposedTxnAmount")
    private String txnAmount;

    @JsonProperty
    private String referenceId;

    @JsonProperty("enablePaymentMode")
    private List<PaymentMode> enablePaymentMode = null;

    @JsonProperty("disablePaymentMode")
    private List<PaymentMode> disablePaymentMode = null;

    private UserDetails userDetails;

    private UserInfo merchantUserInfo;

    private SubscriptionTransactionRequestBody subscriptionTransactionRequestBody;

    public ERequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ERequestType requestType) {
        this.requestType = requestType;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public boolean isEMIDetail() {
        return isEMIDetail;
    }

    public void setisEMIDetail(boolean isEMIDetail) {
        this.isEMIDetail = isEMIDetail;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getEmiType() {
        return emiType;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public List<PaymentMode> getEnablePaymentMode() {
        return enablePaymentMode;
    }

    public void setEnablePaymentMode(List<PaymentMode> enablePaymentMode) {
        this.enablePaymentMode = enablePaymentMode;
    }

    public List<PaymentMode> getDisablePaymentMode() {
        return disablePaymentMode;
    }

    public void setDisablePaymentMode(List<PaymentMode> disablePaymentMode) {
        this.disablePaymentMode = disablePaymentMode;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public UserInfo getMerchantUserInfo() {
        return merchantUserInfo;
    }

    public void setMerchantUserInfo(UserInfo merchantUserInfo) {
        this.merchantUserInfo = merchantUserInfo;
    }

    public SubscriptionTransactionRequestBody getSubscriptionTransactionRequestBody() {
        return subscriptionTransactionRequestBody;
    }

    public void setSubscriptionTransactionRequestBody(
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        this.subscriptionTransactionRequestBody = subscriptionTransactionRequestBody;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
