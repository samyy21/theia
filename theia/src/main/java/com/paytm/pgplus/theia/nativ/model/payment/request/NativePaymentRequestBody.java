package com.paytm.pgplus.theia.nativ.model.payment.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.models.CoftConsent;
import com.paytm.pgplus.models.EmiSubventionInfo;
import com.paytm.pgplus.models.TwoFAConfig;

import java.io.Serializable;
import java.util.Map;

/**
 * @author kartik
 * @date 07-May-2018
 */
@RedisEncrypt
@JsonIgnoreProperties(ignoreUnknown = true)
public class NativePaymentRequestBody implements Serializable {

    private static final long serialVersionUID = -9098790769397830135L;

    private String mid;
    private String orderId;
    private String channelId;
    private String authMode;

    private String paymentMode;

    @RedisEncryptAttribute
    @Mask
    private String cardInfo;
    private String storeInstrument;

    private String channelCode;
    private String planId;

    @Mask
    private String payerAccount;

    @Mask
    private String mpin;
    @Mask
    private String accountNumber;
    private String bankName;
    private String creditBlock;
    @Mask
    private String seqNumber;
    private String deviceId;

    private String paymentFlow;
    private String website;

    private String selectedPaymentModeId;
    private String retryErrorMsg;
    private String retryCount;

    private String emiType;

    private String appId;

    private Map<String, String> extendInfo;

    private String refUrl;

    private String txnNote;

    private String subscriptionId;

    private String upiAccRefId;

    private String originChannel;

    private String templateId;

    private String bankIfsc;

    private String userName;

    private String mandateAuthMode;

    private String accountType;

    private String checkoutJsConfig;

    private boolean dccSelectedByUser;

    private boolean paymentCallFromDccPage;
    // Adde new member for dataenrichment
    private String riskExtendInfo;

    private EmiSubventionInfo emiSubventionInfo;

    private EPreAuthType cardPreAuthType;

    private Long preAuthBlockSeconds;

    private Boolean convertToAddAndPayTxn;

    private CoftConsent coftConsent;

    private CardTokenInfo cardTokenInfo;

    private TwoFAConfig twoFAConfig;

    private Boolean addOneRupee;

    private Boolean variableLengthOtpSupported;

    public String getCheckoutJsConfig() {
        return checkoutJsConfig;
    }

    public void setCheckoutJsConfig(String checkoutJsConfig) {
        this.checkoutJsConfig = checkoutJsConfig;
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

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public String getStoreInstrument() {
        return storeInstrument;
    }

    public void setStoreInstrument(String storeInstrument) {
        this.storeInstrument = storeInstrument;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCreditBlock() {
        return creditBlock;
    }

    public void setCreditBlock(String creditBlock) {
        this.creditBlock = creditBlock;
    }

    public String getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(String seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(String paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSelectedPaymentModeId() {
        return selectedPaymentModeId;
    }

    public void setSelectedPaymentModeId(String selectedPaymentModeId) {
        this.selectedPaymentModeId = selectedPaymentModeId;
    }

    public String getRetryErrorMsg() {
        return retryErrorMsg;
    }

    public void setRetryErrorMsg(String retryErrorMsg) {
        this.retryErrorMsg = retryErrorMsg;
    }

    public String getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(String retryCount) {
        this.retryCount = retryCount;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    public String getTxnNote() {
        return txnNote;
    }

    public void setTxnNote(String txnNote) {
        this.txnNote = txnNote;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getUpiAccRefId() {
        return upiAccRefId;
    }

    public void setUpiAccRefId(String upiAccRefId) {
        this.upiAccRefId = upiAccRefId;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(String originChannel) {
        this.originChannel = originChannel;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMandateAuthMode() {
        return mandateAuthMode;
    }

    public void setMandateAuthMode(String mandateAuthMode) {
        this.mandateAuthMode = mandateAuthMode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBankIfsc() {
        return bankIfsc;
    }

    public void setBankIfsc(String bankIfsc) {
        this.bankIfsc = bankIfsc;
    }

    public boolean getDccSelectedByUser() {
        return dccSelectedByUser;
    }

    public void setDccSelectedByUser(boolean dccSelectedByUser) {
        this.dccSelectedByUser = dccSelectedByUser;
    }

    public boolean isPaymentCallFromDccPage() {
        return paymentCallFromDccPage;
    }

    public void setPaymentCallFromDccPage(boolean paymentCallFromDccPage) {
        this.paymentCallFromDccPage = paymentCallFromDccPage;
    }

    public String getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setRiskExtendInfo(String riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
    }

    public EmiSubventionInfo getEmiSubventionInfo() {
        return emiSubventionInfo;
    }

    public void setEmiSubventionInfo(EmiSubventionInfo emiSubventionInfo) {
        this.emiSubventionInfo = emiSubventionInfo;
    }

    public EPreAuthType getCardPreAuthType() {
        return cardPreAuthType;
    }

    public void setCardPreAuthType(EPreAuthType cardPreAuthType) {
        this.cardPreAuthType = cardPreAuthType;
    }

    public Long getPreAuthBlockSeconds() {
        return preAuthBlockSeconds;
    }

    public void setPreAuthBlockSeconds(Long preAuthBlockSeconds) {
        this.preAuthBlockSeconds = preAuthBlockSeconds;
    }

    public Boolean getConvertToAddAndPayTxn() {
        return convertToAddAndPayTxn;
    }

    public void setConvertToAddAndPayTxn(Boolean convertToAddAndPayTxn) {
        this.convertToAddAndPayTxn = convertToAddAndPayTxn;
    }

    public CoftConsent getCoftConsent() {
        return coftConsent;
    }

    public void setCoftConsent(CoftConsent coftConsent) {
        this.coftConsent = coftConsent;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    public TwoFAConfig getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfigObj(TwoFAConfig twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public Boolean getAddOneRupee() {
        return addOneRupee;
    }

    public void setAddOneRupee(Boolean addOneRupee) {
        this.addOneRupee = addOneRupee;
    }

    public Boolean isVariableLengthOtpSupported() {
        return variableLengthOtpSupported;
    }

    public void setVariableLengthOtpSupported(Boolean variableLengthOtpSupported) {
        this.variableLengthOtpSupported = variableLengthOtpSupported;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
