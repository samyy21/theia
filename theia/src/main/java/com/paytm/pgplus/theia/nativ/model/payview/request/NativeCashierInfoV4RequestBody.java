package com.paytm.pgplus.theia.nativ.model.payview.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeCashierInfoV4RequestBody implements Serializable {

    private final static long serialVersionUID = 508338953026217847L;

    @JsonProperty("enablePaymentMode")
    private List<PaymentMode> enablePaymentMode = null;

    @JsonProperty("disablePaymentMode")
    private List<PaymentMode> disablePaymentMode = null;

    @JsonProperty("fetchAllNB")
    private String fetchAllNB;

    @JsonProperty("aggMid")
    private String aggMid;

    @JsonProperty("postpaidOnboardingSupported")
    private boolean postpaidOnboardingSupported;

    @JsonProperty("appVersion")
    private String appVersion;

    @JsonProperty("sdkVersion")
    private String sdkVersion;

    @JsonProperty("mid")
    private String mid;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("generateOrderId")
    private String generateOrderId;

    @JsonProperty("emiOption")
    private String emiOption;

    @JsonProperty("fetchAllPaymentOffers")
    private String fetchAllPaymentOffers;

    private String orderAmount;

    private String applyPaymentOffer;

    private String paymentPromocode;

    @JsonProperty("eightDigitBinRequired")
    private boolean eightDigitBinRequired;

    @JsonProperty("cardHashRequired")
    private boolean cardHashRequired;

    @JsonProperty("items")
    private List<Item> items;

    @JsonProperty("isEmiSubventionRequired")
    private boolean isEmiSubventionRequired;

    private String emiSubventedTransactionAmount;

    private String emiSubventionCustomerId;

    private boolean isInternalFetchPaymentOptions;

    // for FPO v2 request from push sdk
    @JsonProperty("origin-channel")
    private String originChannel;

    // for FPO v2 request from push sdk
    private String deviceId;

    @JsonProperty("productCode")
    private String productCode;

    // for FetchChannelDetailsTask
    private boolean mlvSupported;
    private QRCodeInfoResponseData qRCodeInfo;

    // For TokenType CHECKSUM and ACCESS
    private String referenceId;

    // For TokenType CHECKSUM only
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String paytmSsoToken;

    // not sent by external party , used internally
    private String accessToken;

    private boolean isExternalFetchPaymentOptions;

    private boolean addMoneyFeeAppliedOnWallet;

    private String initialAddMoneyAmount;
    /**
     * This is userdetails against ssoToken
     */
    private UserDetails userDetails;
    /**
     * THis is merchantUserInfo
     */
    private UserInfo merchantUserInfo;

    private String queryParams;

    @JsonProperty("subscriptionTransactionRequestBody")
    private SubscriptionTransactionRequestBody subscriptionTransactionRequestBody;

    @JsonProperty("requestType")
    private String requestType;

    public boolean isAddMoneyFeeAppliedOnWallet() {
        return addMoneyFeeAppliedOnWallet;
    }

    public void setAddMoneyFeeAppliedOnWallet(boolean addMoneyFeeAppliedOnWallet) {
        this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getApplyPaymentOffer() {
        return applyPaymentOffer;
    }

    public void setApplyPaymentOffer(String applyPaymentOffer) {
        this.applyPaymentOffer = applyPaymentOffer;
    }

    public String getPaymentPromocode() {
        return paymentPromocode;
    }

    public void setPaymentPromocode(String paymentPromocode) {
        this.paymentPromocode = paymentPromocode;
    }

    public String getFetchAllPaymentOffers() {
        return fetchAllPaymentOffers;
    }

    public void setFetchAllPaymentOffers(String fetchAllPaymentOffers) {
        this.fetchAllPaymentOffers = fetchAllPaymentOffers;
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

    public String getFetchAllNB() {
        return fetchAllNB;
    }

    public void setFetchAllNB(String fetchAllNB) {
        this.fetchAllNB = fetchAllNB;
    }

    public String getAggMid() {
        return aggMid;
    }

    public void setAggMid(String aggMid) {
        this.aggMid = aggMid;
    }

    public boolean isPostpaidOnboardingSupported() {
        return postpaidOnboardingSupported;
    }

    public void setPostpaidOnboardingSupported(boolean postpaidOnboardingSupported) {
        this.postpaidOnboardingSupported = postpaidOnboardingSupported;
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

    public String getGenerateOrderId() {
        return generateOrderId;
    }

    public void setGenerateOrderId(String generateOrderId) {
        this.generateOrderId = generateOrderId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getEmiOption() {
        return emiOption;
    }

    public void setEmiOption(String emiOption) {
        this.emiOption = emiOption;
    }

    public boolean isEightDigitBinRequired() {
        return eightDigitBinRequired;
    }

    public void setEightDigitBinRequired(boolean eightDigitBinRequired) {
        this.eightDigitBinRequired = eightDigitBinRequired;
    }

    public boolean isCardHashRequired() {
        return cardHashRequired;
    }

    public void setHashCardRequired(boolean cardHashRequired) {
        this.cardHashRequired = cardHashRequired;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean isEmiSubventionRequired() {
        return isEmiSubventionRequired;
    }

    public void setEmiSubventionRequired(boolean emiSubventionRequired) {
        isEmiSubventionRequired = emiSubventionRequired;
    }

    public String getEmiSubventedTransactionAmount() {
        return emiSubventedTransactionAmount;
    }

    public void setEmiSubventedTransactionAmount(String emiSubventedTransactionAmount) {
        this.emiSubventedTransactionAmount = emiSubventedTransactionAmount;
    }

    public String getEmiSubventionCustomerId() {
        return emiSubventionCustomerId;
    }

    public void setEmiSubventionCustomerId(String emiSubventionCustomerId) {
        this.emiSubventionCustomerId = emiSubventionCustomerId;
    }

    public boolean isInternalFetchPaymentOptions() {
        return isInternalFetchPaymentOptions;
    }

    public void setInternalFetchPaymentOptions(boolean internalFetchPaymentOptions) {
        isInternalFetchPaymentOptions = internalFetchPaymentOptions;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(String originChannel) {
        this.originChannel = originChannel;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public boolean isMlvSupported() {
        return mlvSupported;
    }

    public void setMlvSupported(boolean mlvSupported) {
        this.mlvSupported = mlvSupported;
    }

    public QRCodeInfoResponseData getqRCodeInfo() {
        return qRCodeInfo;
    }

    public void setqRCodeInfo(QRCodeInfoResponseData qRCodeInfo) {
        this.qRCodeInfo = qRCodeInfo;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPaytmSsoToken() {
        return paytmSsoToken;
    }

    public void setPaytmSsoToken(String paytmSsoToken) {
        this.paytmSsoToken = paytmSsoToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isExternalFetchPaymentOptions() {
        return isExternalFetchPaymentOptions;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setExternalFetchPaymentOptions(boolean externalFetchPaymentOptions) {
        isExternalFetchPaymentOptions = externalFetchPaymentOptions;
    }

    public String getInitialAddMoneyAmount() {
        return initialAddMoneyAmount;
    }

    public void setInitialAddMoneyAmount(String initialAddMoneyAmount) {
        this.initialAddMoneyAmount = initialAddMoneyAmount;
    }

    public UserInfo getMerchantUserInfo() {
        return merchantUserInfo;
    }

    public void setMerchantUserInfo(UserInfo merchantUserInfo) {
        this.merchantUserInfo = merchantUserInfo;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public SubscriptionTransactionRequestBody getSubscriptionTransactionRequestBody() {
        return subscriptionTransactionRequestBody;
    }

    public void setSubscriptionTransactionRequestBody(
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        this.subscriptionTransactionRequestBody = subscriptionTransactionRequestBody;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
