package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoV2;
import com.paytm.pgplus.cache.model.MerchantStaticConfig;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.user.models.NpciHealthData;
import com.paytm.pgplus.models.MandateAccountDetails;
import com.paytm.pgplus.models.SimplifiedPaymentOffers;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.theia.nativ.model.payment.request.NativePaymentRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.MerchantLimitDetail;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativePromoCodeData;
import com.paytm.pgplus.theia.nativ.model.payview.response.QrDetail;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.nativ.model.subscription.SubscriptionDetail;
import com.paytm.pgplus.theia.offline.model.request.SubventionDetails;
import com.paytm.pgplus.theia.paymentoffer.model.response.ConvertToAddNPayOfferDetails;
import com.paytm.pgplus.theia.paymentoffer.model.response.PaymentOffersData;
import com.paytm.pgplus.theia.supercashoffer.model.SuperCashOffers;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RedisEncrypt
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPage implements Serializable {

    private static final long serialVersionUID = -2208233432014974929L;

    private String txnToken;
    private EnhancedCashierPageWalletInfo wallet;
    private List<EnhancedCashierPagePayModeBase> merchantPayModes;
    private List<EnhancedCashierPagePayModeBase> addMoneyPayModes;
    private EnhancedCashierPageMerchantInfo merchant;
    private EnhancedCashierTxnInfo txn;
    private EnhancedCashierPageLoginInfo loginInfo;
    private EnhancedCashierLocalalizedText i18n;
    private String callbackUrl;
    private String languageCode;
    private Map<String, Object> localePushAppData;

    @RedisEncryptAttribute
    private NativePaymentRequestBody retryData;
    private NativePromoCodeData promoCodeData;
    private boolean zeroCostEmi;
    private UserInfo userInfo;
    // For showing storecard option in Enhanced flow.
    private boolean showStoreCardEnabled;
    private LinkAppPushData link;
    private SubscriptionDetail subscriptionDetail;
    private EnhancedCashierPageDynamicQR qr;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String oldPGBaseUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String newPGBaseUrl;

    private HashMap<String, String> upiHandleMap;

    private Map<String, String> upiAppNamesRegional;

    private MerchantPreferenceInfoV2 merchantPreferenceInfoExt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private NpciHealthData npciHealth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UpiMetaData upiMetaData;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> upiLinkedBanks;

    private boolean preLoginTheme;

    private MandateAccountDetails mandateAccountDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SimplifiedPaymentOffers simplifiedPaymentOffers;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PaymentOffersData> paymentOffers;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean pwpEnabled;

    private PreAuthDetails preAuthDetails;

    private Boolean collectAppInvoke;

    private Boolean addnPayWithUPIIntentOnly;

    private int allowedRetryCountsForMerchant;

    private Boolean disableBackButton;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> uiConfig;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String serverName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cashierAccountNumber;

    private Boolean locationPermission;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("merchantLimitInfo")
    private MerchantLimitDetail merchantLimitDetail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ConvertToAddNPayOfferDetails convertToAddNPayOfferDetails;

    public String getCashierAccountNumber() {
        return cashierAccountNumber;
    }

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    public void setCashierAccountNumber(String cashierAccountNumber) {
        this.cashierAccountNumber = cashierAccountNumber;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SubventionDetails subventionDetails;

    private long txnTokenTTL;

    private Map<String, Object> merchantGroupedPayModes;
    private Map<String, Object> addMoneyGroupedPayModes;
    private Boolean postpaidOnlyMerchant;
    private String postpaidOnlyUserMessage;
    private boolean isPostpaidEnabledOnMerchantAndDisabledOnUser;
    private boolean merchantUPIVerified;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MerchantStaticConfig merchantStaticConfig;
    private Map<String, Integer> groupPayOptionsPriorities;
    private QrDetail qrDetail;
    private String deepLink;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean ccOnUPIAllowed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isNativeAddMoney;

    private String productCode;

    public Map<String, Integer> getGroupPayOptionsPriorities() {
        return groupPayOptionsPriorities;
    }

    public void setGroupPayOptionsPriorities(Map<String, Integer> priorities) {
        this.groupPayOptionsPriorities = priorities;
    }

    public boolean getMerchantUPIVerified() {
        return merchantUPIVerified;
    }

    public void setMerchantUPIVerified(boolean merchantVerified) {
        merchantUPIVerified = merchantVerified;
    }

    private SuperCashOffers superCashOffers;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disableCustomVPAInUPICollect;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean merchantAcceptPostpaid;

    public Map<String, Object> getAddMoneyGroupedPayModes() {
        return addMoneyGroupedPayModes;
    }

    public void setAddMoneyGroupedPayModes(Map<String, Object> addMoneyGroupedPayModes) {
        this.addMoneyGroupedPayModes = addMoneyGroupedPayModes;
    }

    public Map<String, Object> getMerchantGroupedPayModes() {
        return merchantGroupedPayModes;
    }

    public void setMerchantGroupedPayModes(Map<String, Object> merchantGroupedPayModes) {
        this.merchantGroupedPayModes = merchantGroupedPayModes;
    }

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public EnhancedCashierPageMerchantInfo getMerchant() {
        return merchant;
    }

    public void setMerchant(EnhancedCashierPageMerchantInfo merchant) {
        this.merchant = merchant;
    }

    public EnhancedCashierPageWalletInfo getWallet() {
        return wallet;
    }

    public void setWallet(EnhancedCashierPageWalletInfo wallet) {
        this.wallet = wallet;
    }

    public EnhancedCashierPageLoginInfo getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(EnhancedCashierPageLoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public List<EnhancedCashierPagePayModeBase> getMerchantPayModes() {
        return merchantPayModes;
    }

    public List<EnhancedCashierPagePayModeBase> getAddMoneyPayModes() {
        return addMoneyPayModes;
    }

    public void setMerchantPayModes(List<EnhancedCashierPagePayModeBase> merchantPayModes) {
        this.merchantPayModes = merchantPayModes;
    }

    public void setAddMoneyPayModes(List<EnhancedCashierPagePayModeBase> addMoneyPayModes) {
        this.addMoneyPayModes = addMoneyPayModes;
    }

    public EnhancedCashierLocalalizedText getI18n() {
        return i18n;
    }

    public void setI18n(EnhancedCashierLocalalizedText i18n) {
        this.i18n = i18n;
    }

    public EnhancedCashierTxnInfo getTxn() {
        return txn;
    }

    public void setTxn(EnhancedCashierTxnInfo txn) {
        this.txn = txn;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Map<String, Object> getLocalePushAppData() {
        return localePushAppData;
    }

    public void setLocalePushAppData(Map<String, Object> localePushAppData) {
        this.localePushAppData = localePushAppData;
    }

    public NativePaymentRequestBody getRetryData() {
        return retryData;
    }

    public void setRetryData(NativePaymentRequestBody retryData) {
        this.retryData = retryData;
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

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setShowStoreCardEnabled(boolean showStoreCardEnabled) {
        this.showStoreCardEnabled = showStoreCardEnabled;
    }

    public boolean isShowStoreCardEnabled() {
        return showStoreCardEnabled;
    }

    public LinkAppPushData getLink() {
        return link;
    }

    public void setLink(LinkAppPushData link) {
        this.link = link;
    }

    public SubscriptionDetail getSubscriptionDetail() {
        return subscriptionDetail;
    }

    public void setSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        this.subscriptionDetail = subscriptionDetail;
    }

    public EnhancedCashierPageDynamicQR getQr() {
        return qr;
    }

    public void setQr(EnhancedCashierPageDynamicQR qr) {
        this.qr = qr;
    }

    public boolean isPreLoginTheme() {
        return preLoginTheme;
    }

    public void setPreLoginTheme(boolean preLoginTheme) {
        this.preLoginTheme = preLoginTheme;
    }

    public String getOldPGBaseUrl() {
        return oldPGBaseUrl;
    }

    public void setOldPGBaseUrl(String oldPGBaseUrl) {
        this.oldPGBaseUrl = oldPGBaseUrl;
    }

    public String getNewPGBaseUrl() {
        return newPGBaseUrl;
    }

    public void setNewPGBaseUrl(String newPGBaseUrl) {
        this.newPGBaseUrl = newPGBaseUrl;
    }

    public HashMap<String, String> getUpiHandleMap() {
        return upiHandleMap;
    }

    public void setUpiHandleMap(HashMap<String, String> upiHandleMap) {
        this.upiHandleMap = upiHandleMap;
    }

    public Map<String, String> getUpiAppNamesRegional() {
        return upiAppNamesRegional;
    }

    public void setUpiAppNamesRegional(Map<String, String> upiAppNamesRegional) {
        this.upiAppNamesRegional = upiAppNamesRegional;
    }

    public MerchantPreferenceInfoV2 getMerchantPreferenceInfoExt() {
        return merchantPreferenceInfoExt;
    }

    public void setMerchantPreferenceInfoExt(MerchantPreferenceInfoV2 merchantPreferenceInfoExt) {
        this.merchantPreferenceInfoExt = merchantPreferenceInfoExt;
    }

    public NpciHealthData getNpciHealth() {
        return npciHealth;
    }

    public void setNpciHealth(NpciHealthData npciHealth) {
        this.npciHealth = npciHealth;
    }

    public UpiMetaData getUpiMetaData() {
        return upiMetaData;
    }

    public void setUpiMetaData(UpiMetaData upiMetaData) {
        this.upiMetaData = upiMetaData;
    }

    public List<String> getUpiLinkedBanks() {
        return upiLinkedBanks;
    }

    public void setUpiLinkedBanks(List<String> upiLinkedBanks) {
        this.upiLinkedBanks = upiLinkedBanks;
    }

    public MandateAccountDetails getMandateAccountDetails() {
        return mandateAccountDetails;
    }

    public void setMandateAccountDetails(MandateAccountDetails mandateAccountDetails) {
        this.mandateAccountDetails = mandateAccountDetails;
    }

    public SimplifiedPaymentOffers getSimplifiedPaymentOffers() {
        return simplifiedPaymentOffers;
    }

    public void setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers) {
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
    }

    public List<PaymentOffersData> getPaymentOffers() {
        return paymentOffers;
    }

    public void setPaymentOffers(List<PaymentOffersData> paymentOffers) {
        this.paymentOffers = paymentOffers;
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

    public Boolean getCollectAppInvoke() {
        return collectAppInvoke;
    }

    public void setCollectAppInvoke(Boolean collectAppInvoke) {
        this.collectAppInvoke = collectAppInvoke;
    }

    public Boolean getAddnPayWithUPIIntentOnly() {
        return addnPayWithUPIIntentOnly;
    }

    public void setAddnPayWithUPIIntentOnly(Boolean addnPayWithUPIIntentOnly) {
        this.addnPayWithUPIIntentOnly = addnPayWithUPIIntentOnly;
    }

    public int getAllowedRetryCountsForMerchant() {
        return allowedRetryCountsForMerchant;
    }

    public void setAllowedRetryCountsForMerchant(int allowedRetryCountsForMerchant) {
        this.allowedRetryCountsForMerchant = allowedRetryCountsForMerchant;
    }

    public Boolean getDisableBackButton() {
        return disableBackButton;
    }

    public void setDisableBackButton(Boolean disableBackButton) {
        this.disableBackButton = disableBackButton;
    }

    public Map<String, String> getUiConfig() {
        return uiConfig;
    }

    public void setUiConfig(Map<String, String> uiConfig) {
        this.uiConfig = uiConfig;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Boolean getLocationPermission() {
        return locationPermission;
    }

    public void setLocationPermission(Boolean locationPermission) {
        this.locationPermission = locationPermission;
    }

    public SubventionDetails getSubventionDetails() {
        return subventionDetails;
    }

    public void setSubventionDetails(SubventionDetails subventionDetails) {
        this.subventionDetails = subventionDetails;
    }

    public long gettxnTokenTTL() {
        return txnTokenTTL;
    }

    public void settxnTokenTTL(long txnTokenTTL) {
        this.txnTokenTTL = txnTokenTTL;
    }

    public ConvertToAddNPayOfferDetails getConvertToAddNPayOfferDetails() {
        return convertToAddNPayOfferDetails;
    }

    public void setConvertToAddNPayOfferDetails(ConvertToAddNPayOfferDetails convertToAddNPayOfferDetails) {
        this.convertToAddNPayOfferDetails = convertToAddNPayOfferDetails;
    }

    public Boolean getDisableCustomVPAInUPICollect() {
        return disableCustomVPAInUPICollect;
    }

    public void setDisableCustomVPAInUPICollect(Boolean disableCustomVPAInUPICollect) {
        this.disableCustomVPAInUPICollect = disableCustomVPAInUPICollect;
    }

    public Boolean getMerchantAcceptPostpaid() {
        return merchantAcceptPostpaid;
    }

    public void setMerchantAcceptPostpaid(Boolean merchantAcceptPostpaid) {
        this.merchantAcceptPostpaid = merchantAcceptPostpaid;
    }

    public MerchantStaticConfig getMerchantStaticConfig() {
        return merchantStaticConfig;
    }

    public void setMerchantStaticConfig(MerchantStaticConfig merchantStaticConfig) {
        this.merchantStaticConfig = merchantStaticConfig;
    }

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public QrDetail getQrDetail() {
        return qrDetail;
    }

    public void setQrDetail(QrDetail qrDetail) {
        this.qrDetail = qrDetail;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BanksResponse emiSubventionBanks;

    public BanksResponse getEmiSubventionBanks() {
        return emiSubventionBanks;
    }

    public void setEmiSubventionBanks(BanksResponse emiSubventionBanks) {
        this.emiSubventionBanks = emiSubventionBanks;
    }

    public MerchantLimitDetail getMerchantLimitDetail() {
        return merchantLimitDetail;
    }

    public void setMerchantLimitDetail(MerchantLimitDetail merchantLimitDetail) {
        this.merchantLimitDetail = merchantLimitDetail;
    }

    public Boolean getCcOnUPIAllowed() {
        return ccOnUPIAllowed;
    }

    public void setCcOnUPIAllowed(Boolean ccOnUPIAllowed) {
        this.ccOnUPIAllowed = ccOnUPIAllowed;
    }

    @JsonProperty(value = "isNativeAddMoney")
    public boolean getNativeAddMoney() {
        return isNativeAddMoney;
    }

    public void setNativeAddMoney(boolean nativeAddMoney) {
        isNativeAddMoney = nativeAddMoney;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
