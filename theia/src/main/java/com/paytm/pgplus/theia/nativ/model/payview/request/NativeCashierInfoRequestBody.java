package com.paytm.pgplus.theia.nativ.model.payview.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.enums.ProductType;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyItemOffers;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.PRODUCT_CODE;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeCashierInfoRequestBody implements Serializable {

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

    private FetchAllItemOffer fetchAllItemOffers;

    private ApplyItemOffers applyItemOffers;

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

    @JsonProperty(PRODUCT_CODE)
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

    @JsonIgnore
    private String custId;

    private boolean upiRecurringSupport;

    private String subwalletAmount;

    private Map<String, String> extendInfo;

    private boolean offlineFlow;

    @JsonIgnore
    private boolean returnDisabledChannels;

    /**
     * This flag is for app to support backward Compatibilty
     */
    private boolean upiPayConfirmSupport;

    private List<GoodsInfo> goods = null;

    private boolean addNPayOnUPIPushSupported;

    private EPreAuthType cardPreAuthType;

    private Long preAuthBlockSeconds;

    private boolean fetchAddMoneyOptions;

    private ProductType productType;

    private boolean addNPayOnPostpaidSupported;

    private boolean fetchPaytmInstrumentsBalance;

    private boolean deepLinkRequired;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    private String supportedPayModesForAddNPay;

    @JsonProperty("twoFADetails")
    private TwoFADetails twoFADetails;

    private Map<String, String> affordabilityInfo;

    public Map<String, String> getAffordabilityInfo() {
        return affordabilityInfo;
    }

    public void setAffordabilityInfo(Map<String, String> affordabilityInfo) {
        this.affordabilityInfo = affordabilityInfo;
    }

    private SubscriptionTransactionRequestBody subscriptionTransactionRequestBody;

    private String requestType;

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public EPreAuthType getCardPreAuthType() {

        return cardPreAuthType;

    }

    public boolean isAddNPayOnUPIPushSupported() {
        return addNPayOnUPIPushSupported;
    }

    public void setAddNPayOnUPIPushSupported(boolean addNPayOnUPIPushSupported) {
        this.addNPayOnUPIPushSupported = addNPayOnUPIPushSupported;
    }

    public boolean isReturnDisabledChannels() {
        return returnDisabledChannels;
    }

    public void setReturnDisabledChannels(boolean returnDisabledChannels) {
        this.returnDisabledChannels = returnDisabledChannels;
    }

    private boolean blinkCheckoutAccessSupport;

    private UserInfo userInfo;

    @JsonIgnore
    private PaymodeSequenceEnum paymodeSequenceEnum;
    @JsonIgnore
    private String paymodeSequence;
    private boolean returnToken;

    private boolean isStaticQrCode;

    @JsonProperty("isLiteEligible")
    private boolean upiLiteEligible;

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

    public FetchAllItemOffer getFetchAllItemOffers() {
        return fetchAllItemOffers;
    }

    public void setFetchAllItemOffers(FetchAllItemOffer fetchAllItemOffers) {
        this.fetchAllItemOffers = fetchAllItemOffers;
    }

    public ApplyItemOffers getApplyItemOffers() {
        return applyItemOffers;
    }

    public void setApplyItemOffers(ApplyItemOffers applyItemOffers) {
        this.applyItemOffers = applyItemOffers;
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

    public void setExternalFetchPaymentOptions(boolean externalFetchPaymentOptions) {
        isExternalFetchPaymentOptions = externalFetchPaymentOptions;
    }

    public String getInitialAddMoneyAmount() {
        return initialAddMoneyAmount;
    }

    public void setInitialAddMoneyAmount(String initialAddMoneyAmount) {
        this.initialAddMoneyAmount = initialAddMoneyAmount;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public boolean isUpiRecurringSupport() {
        return upiRecurringSupport;
    }

    public void setUpiRecurringSupport(boolean upiRecurringSupport) {
        this.upiRecurringSupport = upiRecurringSupport;
    }

    public String getSubwalletAmount() {
        return subwalletAmount;
    }

    public void setSubwalletAmount(String subwalletAmount) {
        this.subwalletAmount = subwalletAmount;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isOfflineFlow() {
        return offlineFlow;
    }

    public void setOfflineFlow(boolean offlineFlow) {
        this.offlineFlow = offlineFlow;
    }

    public boolean isUpiPayConfirmSupport() {
        return upiPayConfirmSupport;
    }

    public void setUpiPayConfirmSupport(boolean upiPayConfirmSupport) {
        this.upiPayConfirmSupport = upiPayConfirmSupport;
    }

    public boolean isBlinkCheckoutAccessSupport() {
        return blinkCheckoutAccessSupport;
    }

    public void setBlinkCheckoutAccessSupport(boolean blinkCheckoutAccessSupport) {
        this.blinkCheckoutAccessSupport = blinkCheckoutAccessSupport;
    }

    public List<GoodsInfo> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo> goods) {
        this.goods = goods;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public PaymodeSequenceEnum getPaymodeSequenceEnum() {
        return paymodeSequenceEnum;
    }

    public void setPaymodeSequenceEnum(PaymodeSequenceEnum paymodeSequenceEnum) {
        this.paymodeSequenceEnum = paymodeSequenceEnum;
    }

    public String getPaymodeSequence() {
        return paymodeSequence;
    }

    public void setPaymodeSequence(String paymodeSequence) {
        this.paymodeSequence = paymodeSequence;
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

    public boolean isFetchAddMoneyOptions() {
        return fetchAddMoneyOptions;
    }

    public void setFetchAddMoneyOptions(boolean fetchAddMoneyOptions) {
        this.fetchAddMoneyOptions = fetchAddMoneyOptions;
    }

    public boolean isAddNPayOnPostpaidSupported() {
        return addNPayOnPostpaidSupported;
    }

    public void setAddNPayOnPostpaidSupported(boolean addNPayOnPostpaidSupported) {
        this.addNPayOnPostpaidSupported = addNPayOnPostpaidSupported;
    }

    public boolean isFetchPaytmInstrumentsBalance() {
        return fetchPaytmInstrumentsBalance;
    }

    public void setFetchPaytmInstrumentsBalance(boolean fetchPaytmInstrumentsBalance) {
        this.fetchPaytmInstrumentsBalance = fetchPaytmInstrumentsBalance;
    }

    public boolean isReturnToken() {
        return returnToken;
    }

    public void setReturnToken(boolean returnToken) {
        this.returnToken = returnToken;
    }

    public boolean isDeepLinkRequired() {
        return deepLinkRequired;
    }

    public void setDeepLinkRequired(boolean deepLinkRequired) {
        this.deepLinkRequired = deepLinkRequired;
    }

    public boolean isStaticQrCode() {
        return isStaticQrCode;
    }

    public void setStaticQrCode(boolean staticQrCode) {
        isStaticQrCode = staticQrCode;
    }

    public String getSupportedPayModesForAddNPay() {
        return supportedPayModesForAddNPay;
    }

    public void setSupportedPayModesForAddNPay(String supportedPayModesForAddNPay) {
        this.supportedPayModesForAddNPay = supportedPayModesForAddNPay;
    }

    public TwoFADetails getTwoFADetails() {
        return twoFADetails;
    }

    public void setTwoFADetails(TwoFADetails twoFADetails) {
        this.twoFADetails = twoFADetails;
    }

    public boolean isUpiLiteEligible() {
        return upiLiteEligible;
    }

    public void setUpiLiteEligible(boolean upiLiteEligible) {
        this.upiLiteEligible = upiLiteEligible;
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
