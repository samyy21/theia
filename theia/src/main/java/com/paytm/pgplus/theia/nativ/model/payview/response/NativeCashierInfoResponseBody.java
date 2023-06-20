package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.risk.RiskConvenienceFee;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WalletLimits;
import com.paytm.pgplus.enums.AppInvokeType;
import com.paytm.pgplus.facade.deals.models.response.DealsResult;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.SearchPaymentOffersServiceResponseV2;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.models.MandateAccountDetails;
import com.paytm.pgplus.models.SimplifiedPaymentOffers;
import com.paytm.pgplus.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.models.MerchantDetails;
import com.paytm.pgplus.theia.nativ.model.common.LinkAppPushData;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.nativ.model.subscription.SubscriptionDetail;
import com.paytm.pgplus.theia.offline.model.request.SubventionDetails;
import com.paytm.pgplus.theia.paymentoffer.model.response.ConvertToAddNPayOfferDetails;
import com.paytm.pgplus.theia.paymentoffer.model.response.PaymentOffersData;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOffers;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class NativeCashierInfoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -7136451177876077749L;

    @JsonProperty("paymentFlow")
    private EPayMode paymentFlow;

    @JsonProperty("merchantPayOption")
    private PayOption merchantPayOption;

    @JsonProperty("addMoneyPayOption")
    private PayOption addMoneyPayOption;

    private Map<String, Integer> groupPayOptionsPriorities;

    private Map<String, Object> groupedMerchantPayOption;

    private Map<String, Object> groupedAddMoneyPayOption;

    private MerchantDetails merchantDetails;

    private MerchantDetails addMoneyMerchantDetails;

    private boolean walletOnly;

    private boolean zeroCostEmi;

    private Boolean upiAppsPayModeEnabled;

    @JsonProperty("merchantOfferMessage")
    private String merchantOfferMessage;

    private String infoButtonUpdateMessage;

    private String displayMessage;

    private boolean pcfEnabled;

    private Boolean nativeJsonRequestSupported;

    private NativePromoCodeData promoCodeData;

    private ConsultFeeResponse consultFeeResponse;

    private boolean isOnTheFlyKYCRequired;

    private String orderId;

    private List<PaymentOffersData> paymentOffers;

    private List<RiskConvenienceFee> riskConvenienceFee;

    private QRCodeDetailsResponse qrCodeDetailsResponse;

    private boolean activeMerchant;

    private String addMoneyDestination;
    private String oneClickMaxAmount;
    private String prepaidCardMaxAmount;

    private String productCode;

    @JsonProperty("merchantLimitInfo")
    private MerchantLimitDetail merchantLimitDetail;

    @JsonProperty("guestToken")
    private String guestToken;

    private UserDetails userDetails;

    private UserInfo userInfo;

    private LoginInfo loginInfo;

    private List<MandateSupportedApps> mandateSupportedApps;

    private QrDetail qrDetail;

    private BanksResponse emiSubventionBanks;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String iconBaseUrl;

    private SubscriptionDetail subscriptionDetail;

    @JsonIgnore
    @JsonProperty("appInvokeDevice")
    private AppInvokeType appInvokeDevice;

    @JsonIgnore
    private Object channelDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accessToken;

    private boolean addDescriptionMandatory;

    private String descriptionTextFormat;

    private MandateAccountDetails mandateAccountDetails;

    private SimplifiedPaymentOffers simplifiedPaymentOffers;

    private Boolean pwpEnabled;

    private PreAuthDetails preAuthDetails;

    private int allowedRetryCountsForMerchant;

    /**
     * changes for PGP-26686
     */
    @JsonProperty("isLocalStorageAllowedForLastPayMode")
    private Boolean isLocalStorageAllowedForLastPayMode;

    private String cashierAccountNumber;

    private LinkAppPushData link;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "isSavedCardsAvailable")
    private Boolean savedCardsAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "isAddMoneyPayOptionsAvailable")
    private Boolean addMoneyPayOptionsAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disableCustomVPAInUPICollect;

    public LinkAppPushData getLink() {
        return link;
    }

    public void setLink(LinkAppPushData link) {
        this.link = link;
    }

    public String getCashierAccountNumber() {
        return cashierAccountNumber;
    }

    public void setCashierAccountNumber(String cashierAccountNumber) {
        this.cashierAccountNumber = cashierAccountNumber;
    }

    @JsonProperty("isHtmlToBeRenderedForBlinkCheckout")
    private Boolean isHtmlToBeRenderedForBlinkCheckout;

    private boolean otpAuthorised;

    private SubventionDetails subventionDetails;

    private String orderAmount;

    private SearchPaymentOffersServiceResponseV2 paymentOffersV2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ConvertToAddNPayOfferDetails convertToAddNPayOfferDetails;

    @JsonIgnore
    private Boolean isAddAndPayMerchant;

    private boolean locationPermission;

    @JsonIgnore
    private Map<String, Boolean> channelCoftPayment;

    private Boolean postpaidOnlyMerchant;

    private String postpaidOnlyUserMessage;

    private boolean isPostpaidEnabledOnMerchantAndDisabledOnUser;

    private boolean merchantUPIVerified;

    private SuperCashOffers superCashOffers;

    private WalletLimits walletLimits;

    private String postpaid2FAThresholdValue;

    private Boolean postpaid2FAEnabled;

    private String deepLink;

    private Boolean bffLayerEnabled;

    @JsonProperty(value = "deals")
    private DealsResult dealsResult;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean ccOnUPIAllowed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isAddMoneyPcfEnabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isNativeAddMoney;

    public Map<String, Integer> getGroupPayOptionsPriorities() {
        return groupPayOptionsPriorities;
    }

    public void setGroupPayOptionsPriorities(Map<String, Integer> priorities) {
        this.groupPayOptionsPriorities = priorities;
    }

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    public boolean getMerchantUPIVerified() {
        return merchantUPIVerified;
    }

    public SuperCashOffers getSuperCashOffers() {
        return superCashOffers;
    }

    public void setSuperCashOffers(SuperCashOffers superCashOffers) {
        this.superCashOffers = superCashOffers;
    }

    public boolean getIsPostpaidEnabledOnMerchantAndDisabledOnUser() {
        return isPostpaidEnabledOnMerchantAndDisabledOnUser;
    }

    public void setIsPostpaidEnabledOnMerchantAndDisabledOnUser(boolean postpaidEnabledOnMerchantAndDisabledOnUser) {
        isPostpaidEnabledOnMerchantAndDisabledOnUser = postpaidEnabledOnMerchantAndDisabledOnUser;
    }

    public String getPostpaidOnlyUserMessage() {
        return postpaidOnlyUserMessage;
    }

    public void setPostpaidOnlyUserMessage(String postpaidOnlyUserMessage) {
        this.postpaidOnlyUserMessage = postpaidOnlyUserMessage;
    }

    public Boolean isPostpaidOnlyMerchant() {
        return postpaidOnlyMerchant;
    }

    public void setPostpaidOnlyMerchant(Boolean postpaidOnlyMerchant) {
        this.postpaidOnlyMerchant = postpaidOnlyMerchant;
    }

    public boolean isLocationPermission() {
        return locationPermission;
    }

    public void setLocationPermission(boolean locationPermission) {
        this.locationPermission = locationPermission;
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public MerchantLimitDetail getMerchantLimitDetail() {
        return merchantLimitDetail;
    }

    public void setMerchantLimitDetail(MerchantLimitDetail merchantLimitDetail) {
        this.merchantLimitDetail = merchantLimitDetail;
    }

    public List<PaymentOffersData> getPaymentOffers() {
        return paymentOffers;
    }

    public void setPaymentOffers(List<PaymentOffersData> paymentOffers) {
        this.paymentOffers = paymentOffers;
    }

    public boolean isPcfEnabled() {
        return pcfEnabled;
    }

    public void setPcfEnabled(boolean pcfEnabled) {
        this.pcfEnabled = pcfEnabled;
    }

    public String getInfoButtonUpdateMessage() {
        return infoButtonUpdateMessage;
    }

    public void setInfoButtonUpdateMessage(String infoButtonUpdateMessage) {
        this.infoButtonUpdateMessage = infoButtonUpdateMessage;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public EPayMode getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(EPayMode paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public PayOption getMerchantPayOption() {
        return merchantPayOption;
    }

    public void setMerchantPayOption(PayOption merchantPayOption) {
        this.merchantPayOption = merchantPayOption;
    }

    public PayOption getAddMoneyPayOption() {
        return addMoneyPayOption;
    }

    public void setAddMoneyPayOption(PayOption addMoneyPayOption) {
        this.addMoneyPayOption = addMoneyPayOption;
    }

    public MerchantDetails getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(MerchantDetails merchantDetails) {
        this.merchantDetails = merchantDetails;
    }

    public MerchantDetails getAddMoneyMerchantDetails() {
        return addMoneyMerchantDetails;
    }

    public void setAddMoneyMerchantDetails(MerchantDetails addMoneyMerchantDetails) {
        this.addMoneyMerchantDetails = addMoneyMerchantDetails;
    }

    public boolean isWalletOnly() {
        return walletOnly;
    }

    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    public void setMerchantOfferMessage(String merchantOfferMessage) {
        this.merchantOfferMessage = merchantOfferMessage;
    }

    public NativePromoCodeData getPromoCodeData() {
        return promoCodeData;
    }

    public void setPromoCodeData(NativePromoCodeData promoCodeData) {
        this.promoCodeData = promoCodeData;
    }

    public boolean isZeroCostEmi() {
        return zeroCostEmi;
    }

    public void setZeroCostEmi(boolean zeroCostEmi) {
        this.zeroCostEmi = zeroCostEmi;
    }

    public ConsultFeeResponse getConsultFeeResponse() {
        return consultFeeResponse;
    }

    public void setConsultFeeResponse(ConsultFeeResponse consultFeeResponse) {
        this.consultFeeResponse = consultFeeResponse;
    }

    public boolean isOnTheFlyKYCRequired() {
        return isOnTheFlyKYCRequired;
    }

    public void setOnTheFlyKYCRequired(boolean onTheFlyKYCRequired) {
        isOnTheFlyKYCRequired = onTheFlyKYCRequired;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isNativeJsonRequestSupported() {
        return nativeJsonRequestSupported;
    }

    public void setNativeJsonRequestSupported(boolean nativeJsonRequestSupported) {
        this.nativeJsonRequestSupported = nativeJsonRequestSupported;
    }

    public List<RiskConvenienceFee> getRiskConvenienceFee() {
        return riskConvenienceFee;
    }

    public void setRiskConvenienceFee(List<RiskConvenienceFee> riskConvenienceFee) {
        this.riskConvenienceFee = riskConvenienceFee;
    }

    public SubscriptionDetail getSubscriptionDetail() {
        return subscriptionDetail;
    }

    public void setSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        this.subscriptionDetail = subscriptionDetail;
    }

    public AppInvokeType getAppInvokeDevice() {
        return appInvokeDevice;
    }

    public void setAppInvokeDevice(AppInvokeType appInvokeDevice) {
        this.appInvokeDevice = appInvokeDevice;
    }

    public QRCodeDetailsResponse getQrCodeDetailsResponse() {
        return qrCodeDetailsResponse;
    }

    public void setQrCodeDetailsResponse(QRCodeDetailsResponse qrCodeDetailsResponse) {
        this.qrCodeDetailsResponse = qrCodeDetailsResponse;
    }

    public boolean isActiveMerchant() {
        return activeMerchant;
    }

    public void setActiveMerchant(boolean activeMerchant) {
        this.activeMerchant = activeMerchant;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public String getOneClickMaxAmount() {
        return oneClickMaxAmount;
    }

    public void setOneClickMaxAmount(String oneClickMaxAmount) {
        this.oneClickMaxAmount = oneClickMaxAmount;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public BanksResponse getEmiSubventionBanks() {
        return emiSubventionBanks;
    }

    public void setEmiSubventionBanks(BanksResponse emiSubventionBanks) {
        this.emiSubventionBanks = emiSubventionBanks;
    }

    public String getIconBaseUrl() {
        return iconBaseUrl;
    }

    public void setIconBaseUrl(String iconBaseUrl) {
        this.iconBaseUrl = iconBaseUrl;
    }

    public void setChannelDetails(Object channelDetails) {
        this.channelDetails = channelDetails;
    }

    public Object getChannelDetails() {
        return channelDetails;
    }

    public String getPrepaidCardMaxAmount() {
        return prepaidCardMaxAmount;
    }

    public void setPrepaidCardMaxAmount(String prepaidCardMaxAmount) {
        this.prepaidCardMaxAmount = prepaidCardMaxAmount;
    }

    public boolean getAddDescriptionMandatory() {
        return addDescriptionMandatory;
    }

    public void setAddDescriptionMandatory(boolean addDescriptionMandatory) {
        this.addDescriptionMandatory = addDescriptionMandatory;
    }

    public String getDescriptionTextFormat() {
        return descriptionTextFormat;
    }

    public void setDescriptionTextFormat(String descriptionTextFormat) {
        this.descriptionTextFormat = descriptionTextFormat;
    }

    public MandateAccountDetails getMandateAccountDetails() {
        return mandateAccountDetails;
    }

    public void setMandateAccountDetails(MandateAccountDetails mandateAccountDetails) {
        this.mandateAccountDetails = mandateAccountDetails;
    }

    public List<MandateSupportedApps> getMandateSupportedApps() {
        return mandateSupportedApps;
    }

    public void setMandateSupportedApps(List<MandateSupportedApps> mandateSupportedApps) {
        this.mandateSupportedApps = mandateSupportedApps;
    }

    public SimplifiedPaymentOffers getSimplifiedPaymentOffers() {
        return simplifiedPaymentOffers;
    }

    public void setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers) {
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
    }

    public QrDetail getQrDetail() {
        return qrDetail;
    }

    public void setQrDetail(QrDetail qrDetail) {
        this.qrDetail = qrDetail;
    }

    public Boolean getPwpEnabled() {
        return pwpEnabled;
    }

    public void setPwpEnabled(Boolean pwpEnabled) {
        this.pwpEnabled = pwpEnabled;
    }

    public PreAuthDetails getPreAuthDetails() {
        return preAuthDetails;
    }

    public void setPreAuthDetails(PreAuthDetails preAuthDetails) {
        this.preAuthDetails = preAuthDetails;
    }

    public int getAllowedRetryCountsForMerchant() {
        return allowedRetryCountsForMerchant;
    }

    public void setAllowedRetryCountsForMerchant(int allowedRetryCountsForMerchant) {
        this.allowedRetryCountsForMerchant = allowedRetryCountsForMerchant;
    }

    public Boolean isUpiAppsPayModeEnabled() {
        return upiAppsPayModeEnabled;
    }

    public void setUpiAppsPayModeEnabled(boolean upiAppsPayModeEnabled) {
        this.upiAppsPayModeEnabled = upiAppsPayModeEnabled;
    }

    @JsonProperty("isLocalStorageAllowedForLastPayMode")
    public Boolean getLocalStorageAllowedForLastPayMode() {
        return isLocalStorageAllowedForLastPayMode;
    }

    public void setLocalStorageAllowedForLastPayMode(Boolean localStorageAllowedForLastPayMode) {
        isLocalStorageAllowedForLastPayMode = localStorageAllowedForLastPayMode;
    }

    @JsonProperty("isHtmlToBeRenderedForBlinkCheckout")
    public Boolean getHtmlToBeRenderedForBlinkCheckout() {
        return isHtmlToBeRenderedForBlinkCheckout;
    }

    public void setHtmlToBeRenderedForBlinkCheckout(Boolean htmlToBeRenderedForBlinkCheckout) {
        isHtmlToBeRenderedForBlinkCheckout = htmlToBeRenderedForBlinkCheckout;
    }

    public boolean isOtpAuthorised() {
        return otpAuthorised;
    }

    public void setOtpAuthorised(boolean otpAuthorised) {
        this.otpAuthorised = otpAuthorised;
    }

    public SubventionDetails getSubventionDetails() {
        return subventionDetails;
    }

    public void setSubventionDetails(SubventionDetails subventionDetails) {
        this.subventionDetails = subventionDetails;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public SearchPaymentOffersServiceResponseV2 getPaymentOffersV2() {
        return paymentOffersV2;
    }

    public void setPaymentOffersV2(SearchPaymentOffersServiceResponseV2 paymentOffersV2) {
        this.paymentOffersV2 = paymentOffersV2;
    }

    public ConvertToAddNPayOfferDetails getConvertToAddNPayOfferDetails() {
        return convertToAddNPayOfferDetails;
    }

    public void setConvertToAddNPayOfferDetails(ConvertToAddNPayOfferDetails convertToAddNPayOfferDetails) {
        this.convertToAddNPayOfferDetails = convertToAddNPayOfferDetails;
    }

    @JsonIgnore
    @JsonProperty(value = "isAddAndPayMerchant")
    public Boolean getAddAndPayMerchant() {
        return isAddAndPayMerchant;
    }

    public void setAddAndPayMerchant(Boolean addAndPayMerchant) {
        isAddAndPayMerchant = addAndPayMerchant;
    }

    public Map<String, Boolean> getChannelCoftPayment() {
        return channelCoftPayment;
    }

    public void setChannelCoftPayment(Map<String, Boolean> channelCoftPayment) {
        this.channelCoftPayment = channelCoftPayment;
    }

    public Boolean getDisableCustomVPAInUPICollect() {
        return disableCustomVPAInUPICollect;
    }

    public void setDisableCustomVPAInUPICollect(Boolean disableCustomVPAInUPICollect) {
        this.disableCustomVPAInUPICollect = disableCustomVPAInUPICollect;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public WalletLimits getWalletLimits() {
        return walletLimits;
    }

    public void setWalletLimits(WalletLimits walletLimits) {
        this.walletLimits = walletLimits;
    }

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public Boolean IsAddMoneyPayOptionsAvailable() {
        return addMoneyPayOptionsAvailable;
    }

    public void setAddMoneyPayOptionsAvailable(Boolean addMoneyPayOptionsAvailable) {
        this.addMoneyPayOptionsAvailable = addMoneyPayOptionsAvailable;
    }

    public Boolean IsSavedCardsAvailable() {
        return savedCardsAvailable;
    }

    public void setSavedCardsAvailable(Boolean savedCardsAvailable) {
        this.savedCardsAvailable = savedCardsAvailable;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public Boolean getCcOnUPIAllowed() {
        return ccOnUPIAllowed;
    }

    public void setCcOnUPIAllowed(Boolean ccOnUPIAllowed) {
        this.ccOnUPIAllowed = ccOnUPIAllowed;
    }

    @JsonProperty(value = "isAddMoneyPcfEnabled")
    public boolean getAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    @JsonProperty(value = "isNativeAddMoney")
    public boolean getNativeAddMoney() {
        return isNativeAddMoney;
    }

    public void setNativeAddMoney(boolean nativeAddMoney) {
        isNativeAddMoney = nativeAddMoney;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeCashierInfoResponseBody{");
        sb.append("paymentFlow=").append(paymentFlow);
        sb.append(", merchantPayOption=").append(merchantPayOption);
        sb.append(", addMoneyPayOption=").append(addMoneyPayOption);
        sb.append(", merchantDetails=").append(merchantDetails);
        sb.append(", addMoneyMerchantDetails=").append(addMoneyMerchantDetails);
        sb.append(", walletOnly=").append(walletOnly);
        sb.append(", zeroCostEmi=").append(zeroCostEmi);
        sb.append(", merchantOfferMessage='").append(merchantOfferMessage).append('\'');
        sb.append(", infoButtonUpdateMessage='").append(infoButtonUpdateMessage).append('\'');
        sb.append(", displayMessage='").append(displayMessage).append('\'');
        sb.append(", pcfEnabled=").append(pcfEnabled);
        sb.append(", isAddMoneyPcfEnabled=").append(isAddMoneyPcfEnabled);
        sb.append(", isNativeAddMoney=").append(isNativeAddMoney);
        sb.append(", nativeJsonRequestSupported=").append(nativeJsonRequestSupported);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", promoCodeData=").append(promoCodeData);
        sb.append(", consultFeeResponse=").append(consultFeeResponse);
        sb.append(", isOnTheFlyKYCRequired=").append(isOnTheFlyKYCRequired);
        sb.append(", paymentOffers=").append(paymentOffers);
        sb.append(", activeMerchant=").append(activeMerchant);
        sb.append(", addMoneyDestination=").append(addMoneyDestination);
        sb.append(", riskConvenienceFee").append(riskConvenienceFee);
        sb.append(", merchantLimitDetail").append(merchantLimitDetail);
        sb.append(", guestToken=").append(guestToken);
        sb.append(", loginInfo=").append(loginInfo);
        sb.append(", emiSubventionBanks").append(emiSubventionBanks);
        sb.append(", iconBaseUrl").append(iconBaseUrl);
        sb.append(", mandateSupportedApps").append(mandateSupportedApps);
        sb.append(", channelDetails").append(channelDetails);
        sb.append(", mandateAccountDetails").append(mandateAccountDetails);
        sb.append(", simplifiedPaymentOffers").append(simplifiedPaymentOffers);
        sb.append(", pwpEnabled").append(pwpEnabled);
        sb.append(", preAuthDetails").append(preAuthDetails);
        sb.append(", allowedRetryCountsForMerchant").append(allowedRetryCountsForMerchant);
        sb.append(", upiAppsPayModeEnabled").append(upiAppsPayModeEnabled);
        sb.append(", isLocalStorageAllowedForLastPayMode").append(isLocalStorageAllowedForLastPayMode);
        sb.append(",isHtmlToBeRenderedForBlinkCheckout").append(isHtmlToBeRenderedForBlinkCheckout);
        sb.append(",subventionDetails").append(subventionDetails);
        sb.append(",orderAmount").append(orderAmount);
        sb.append(", convertToAddNPayOfferDetails").append(convertToAddNPayOfferDetails);
        sb.append(", walletLimits").append(walletLimits);
        sb.append(", ultimateBeneficiaryDetails").append(ultimateBeneficiaryDetails);
        sb.append(", postpaid2FAThresholdValue").append(postpaid2FAThresholdValue);
        sb.append(", postpaid2FAEnabled").append(postpaid2FAEnabled);
        sb.append(", deepLink").append(deepLink);
        sb.append(", qrDetail").append(qrDetail);
        sb.append(", isSavedCardsAvailable").append(savedCardsAvailable);
        sb.append(", isAddMoneyPayOptionsAvailable").append(addMoneyPayOptionsAvailable);
        sb.append(", bffLayerEnabled").append(bffLayerEnabled);
        sb.append(", ccOnUPIAllowed").append(ccOnUPIAllowed);
        sb.append(", productCode=").append(productCode);
        sb.append('}');
        return sb.toString();
    }

}
