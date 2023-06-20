package com.paytm.pgplus.theia.nativ.model.bin;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeBinDetailRequestBody implements Serializable {

    private static final long serialVersionUID = -274575179046736657L;

    @Mask(prefixNoMaskLen = 6)
    @NotBlank(message = "bin cannot be blank")
    @Length(min = 6, message = "bin cannot be less than 6 characters")
    private String bin;

    private String isEMIDetail;

    @JsonProperty("txnType")
    private String txnType;

    @JsonProperty("channelCode")
    private String channelCode;

    @JsonProperty("emiType")
    private String emiType;

    @JsonProperty("paymentMode")
    private String paymentMode;

    @JsonProperty("requestType")
    private ERequestType requestType;

    @JsonProperty
    private String mid;

    @JsonProperty
    private String txnAmount;

    @JsonProperty
    private String referenceId;

    @JsonProperty("enablePaymentMode")
    private List<PaymentMode> enablePaymentMode = null;

    @JsonProperty("disablePaymentMode")
    private List<PaymentMode> disablePaymentMode = null;

    @JsonIgnore
    private Boolean superGwApiHit;

    @JsonIgnore
    private UserDetails userDetails;

    @JsonIgnore
    private String custId;

    @JsonProperty
    private EPreAuthType cardPreAuthType;

    private SubscriptionTransactionRequestBody subscriptionTransactionRequestBody;

    public NativeBinDetailRequestBody() {
    }

    public NativeBinDetailRequestBody(NativeBinDetailV4RequestBody body) {
        this.bin = body.getBin();
        this.txnType = body.getTxnType();
        this.channelCode = body.getChannelCode();
        this.emiType = body.getEmiType();
        this.paymentMode = body.getPaymentMode();
        this.requestType = body.getRequestType();
        this.referenceId = body.getReferenceId();
        this.txnAmount = body.getTxnAmount();
        this.mid = body.getMid();
    }

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

    public String isEMIDetail() {
        return isEMIDetail;
    }

    public void setisEMIDetail(String isEMIDetail) {
        this.isEMIDetail = isEMIDetail;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getIsEMIDetail() {
        return isEMIDetail;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
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

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Boolean isSuperGwApiHit() {
        return superGwApiHit;
    }

    public void setSuperGwApiHit(Boolean superGwApiHit) {
        this.superGwApiHit = superGwApiHit;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public EPreAuthType getCardPreAuthType() {
        return cardPreAuthType;
    }

    public void setCardPreAuthType(EPreAuthType cardPreAuthType) {
        this.cardPreAuthType = cardPreAuthType;
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
