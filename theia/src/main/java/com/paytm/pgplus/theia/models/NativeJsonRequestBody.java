package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.EcomTokenInfo;
import com.paytm.pgplus.common.model.OneClickInfo;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.models.CoftConsent;
import com.paytm.pgplus.models.EmiSubventionInfo;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.TwoFAConfig;
import com.paytm.pgplus.payloadvault.theia.request.UpiLiteRequestData;

import java.io.Serializable;
import java.util.Map;

public class NativeJsonRequestBody implements Serializable {

    private final static long serialVersionUID = 2416675216687811800L;

    private String mid;
    private String orderId;

    private String authMode;
    private String paymentMode;

    @Mask
    private String cardInfo;
    private String storeInstrument;
    private String channelCode;
    private String planId;

    private String payerAccount;
    private String mpin;

    @Mask
    private String accountNumber;

    private String bankName;
    private String creditBlock;
    private String seqNumber;
    private String deviceId;
    private String appId;

    private String paymentFlow;

    private String selectedPaymentModeId;

    private String emiType;

    private Map<String, String> additionalInfo;

    private Map<String, String> extendInfo;

    private String aggMid;

    private String refUrl;

    private String txnNote;

    private String subscriptionId;

    private String callbackUrl;

    private Money txnAmount;

    private Money tipAmount;

    private String riskExtendInfo;

    private String website;

    private String custId;

    private String templateId;

    private String refId;

    private OneClickInfo oneClickInfo;

    private String guestToken;

    private String upiAccRefId;

    private String originChannel;

    private EcomTokenInfo ecomTokenInfo;

    private String osType;

    private String pspApp;

    private String checkoutJsConfig;

    private String accessToken;

    // for Bank Mandates
    private String bankIfsc;

    private String userName;

    private String mandateAuthMode;
    private String mandateType;
    private String accountType;
    private String dccSelectedByUser;

    private String paymentCallFromDccPage;

    private EmiSubventionInfo emiSubventionInfo;

    public EPreAuthType CardPreAuthType;

    public Long PreAuthBlockSeconds;

    private String preferredOtpPage;

    private Boolean convertToAddAndPayTxn;

    private CoftConsent coftConsent;

    private CardTokenInfo cardTokenInfo;

    private TwoFAConfig twoFAConfig;

    private String qrCodeId;

    private Boolean addOneRupee;

    private boolean variableLengthOtpSupported;

    private String merchantVpa;

    private UpiLiteRequestData upiLiteRequestData;

    public UpiLiteRequestData getUpiLiteRequestData() {
        return upiLiteRequestData;
    }

    public void setUpiLiteRequestData(UpiLiteRequestData upiLiteRequestData) {
        this.upiLiteRequestData = upiLiteRequestData;
    }

    private String simSubscriptionId;

    public String getCheckoutJsConfig() {
        return checkoutJsConfig;
    }

    public void setCheckoutJsConfig(String checkoutJsConfig) {
        this.checkoutJsConfig = checkoutJsConfig;
    }

    public String getAggMid() {
        return aggMid;
    }

    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
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

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
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

    public String getSelectedPaymentModeId() {
        return selectedPaymentModeId;
    }

    public void setSelectedPaymentModeId(String selectedPaymentModeId) {
        this.selectedPaymentModeId = selectedPaymentModeId;
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

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Money getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(Money txnAmount) {
        this.txnAmount = txnAmount;
    }

    public Money getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Money tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setRiskExtendInfo(String riskExtendInfo) {
        this.riskExtendInfo = riskExtendInfo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public OneClickInfo getOneClickInfo() {
        return oneClickInfo;
    }

    public void setOneClickInfo(OneClickInfo oneClickInfo) {
        this.oneClickInfo = oneClickInfo;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
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

    public EcomTokenInfo getEcomTokenInfo() {
        return ecomTokenInfo;
    }

    public void setEcomTokenInfo(EcomTokenInfo ecomTokenInfo) {
        this.ecomTokenInfo = ecomTokenInfo;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getPspApp() {
        return pspApp;
    }

    public void setPspApp(String pspApp) {
        this.pspApp = pspApp;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getBankIfsc() {
        return bankIfsc;
    }

    public void setBankIfsc(String bankIfsc) {
        this.bankIfsc = bankIfsc;
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

    public String getDccSelectedByUser() {
        return dccSelectedByUser;
    }

    public void setDccSelectedByUser(String dccSelectedByUser) {
        this.dccSelectedByUser = dccSelectedByUser;
    }

    public String getPaymentCallFromDccPage() {
        return paymentCallFromDccPage;
    }

    public void setPaymentCallFromDccPage(String paymentCallFromDccPage) {
        this.paymentCallFromDccPage = paymentCallFromDccPage;
    }

    public EmiSubventionInfo getEmiSubventionInfo() {
        return emiSubventionInfo;
    }

    public void setEmiSubventionInfo(EmiSubventionInfo emiSubventionInfo) {
        this.emiSubventionInfo = emiSubventionInfo;
    }

    public EPreAuthType getCardPreAuthType() {
        return CardPreAuthType;
    }

    public void setCardPreAuthType(EPreAuthType cardPreAuthType) {
        CardPreAuthType = cardPreAuthType;
    }

    public Long getPreAuthBlockSeconds() {
        return PreAuthBlockSeconds;
    }

    public void setPreAuthBlockSeconds(Long preAuthBlockSeconds) {
        PreAuthBlockSeconds = preAuthBlockSeconds;
    }

    public String getPreferredOtpPage() {
        return preferredOtpPage;
    }

    public void setPreferredOtpPage(String preferredOtpPage) {
        this.preferredOtpPage = preferredOtpPage;
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

    public String getMandateType() {
        return mandateType;
    }

    public void setMandateType(String mandateType) {
        this.mandateType = mandateType;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public TwoFAConfig getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfigObj(TwoFAConfig twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public Boolean getAddOneRupee() {
        return addOneRupee;
    }

    public void setAddOneRupee(Boolean addOneRupee) {
        this.addOneRupee = addOneRupee;
    }

    public boolean isVariableLengthOtpSupported() {
        return variableLengthOtpSupported;
    }

    public void setVariableLengthOtpSupported(boolean variableLengthOtpSupported) {
        this.variableLengthOtpSupported = variableLengthOtpSupported;
    }

    public String getMerchantVpa() {
        return merchantVpa;
    }

    public void setMerchantVpa(String merchantVpa) {
        this.merchantVpa = merchantVpa;
    }

    public String getSimSubscriptionId() {
        return simSubscriptionId;
    }

    public void setSimSubscriptionId(String simSubscriptionId) {
        this.simSubscriptionId = simSubscriptionId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
