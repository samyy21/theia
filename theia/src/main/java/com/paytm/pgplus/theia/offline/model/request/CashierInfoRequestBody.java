package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.models.*;
import com.paytm.pgplus.request.SubscriptionTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashierInfoRequestBody extends RequestBody {

    private static final long serialVersionUID = -464558353399985924L;
    @ApiModelProperty(notes = "Default is ALL")
    private List<InstrumentType> instrumentTypes;

    @ApiModelProperty(notes = "Default is ALL")
    private List<InstrumentType> savedInstrumentsTypes;

    private List<InstrumentType> disabledInstrumentTypes;

    private Money orderAmount;

    private String orderId;

    private String custId;

    private String emiOption;

    private String subwalletAmount;

    private String promoCode;

    private String website;

    private String requestType;

    private boolean isNativeAddMoney;

    @JsonProperty("isMultiAccForVpaSupported")
    private boolean isMultiAccForVpaSupported;

    private List<GoodsInfo> goodsInfo;

    private String targetPhoneNo;

    // Backward compatible flag for postpaid onboarding
    private boolean postpaidOnboardingSupported;

    private String kycCode;

    private String kycVersion;

    /*
     * This is not expected to be in the api request, this is done to persist
     * data
     */
    private NativePersistData nativePersistData;

    private String subscriptionPaymentMode;

    private boolean eightDigitBinRequired;

    private boolean cardHashRequired;

    private String amountForWalletConsultInRisk;

    private String amountForPaymentFlow;

    private List<String> bankAccountNumbers;

    private SubscriptionTransactionRequestBody subscriptionTransactionRequestBody;

    private String appVersion;

    private MandateMode mandateType;

    private boolean isInternalFetchPaymentOptions;

    private List<Item> items;
    @JsonProperty("isEmiSubventionRequired")
    private boolean isEmiSubventionRequired;

    private String emiSubventedTransactionAmount;

    private String emiSubventionCustomerId;

    private SubventionDetails subventionDetails;

    private String originChannel;

    private String productCode;

    // for FetchChannelDetailsTask
    private boolean mlvSupported;
    private QRCodeInfoResponseData qRCodeInfo;

    private String accessToken;

    private boolean isExternalFetchPaymentOptions;

    private boolean addMoneyFeeAppliedOnWallet;

    private String initialAddMoneyAmount;

    private SimplifiedPaymentOffers simplifiedPaymentOffers;

    private boolean upiRecurringSupport;

    private boolean returnDisabledChannels;

    private boolean upiPayConfirmSupport;

    private boolean preAuth;

    private String referenceId;

    private UserInfo userInfo;

    private EPreAuthType cardPreAuthType;

    private Long preAuthBlockSeconds;

    private boolean fetchAddMoneyOptions;

    private boolean addNPayOnPostpaidSupported;

    private boolean appInvoke;
    private boolean fetchPaytmInstrumentsBalance;
    private boolean returnToken;

    private String accountNumber;
    private String validateAccountNumber;
    private String allowUnverifiedAccount;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    private boolean isStaticQrCode;

    private TwoFADetails twoFADetails;

    @JsonProperty("isLiteEligible")
    private boolean upiLiteEligible;

    private boolean isDealsFlow;

    public boolean isDealsFlow() {
        return isDealsFlow;
    }

    public void setDealsFlow(boolean dealsFlow) {
        isDealsFlow = dealsFlow;
    }

    private boolean superGwFpoApiHit;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getValidateAccountNumber() {
        return validateAccountNumber;
    }

    public void setValidateAccountNumber(String validateAccountNumber) {
        this.validateAccountNumber = validateAccountNumber;
    }

    public String getAllowUnverifiedAccount() {
        return allowUnverifiedAccount;
    }

    public void setAllowUnverifiedAccount(String allowUnverifiedAccount) {
        this.allowUnverifiedAccount = allowUnverifiedAccount;
    }

    public EPreAuthType getCardPreAuthType() {

        return cardPreAuthType;

    }

    public boolean isReturnDisabledChannels() {
        return returnDisabledChannels;
    }

    public void setReturnDisabledChannels(boolean returnDisabledChannels) {
        this.returnDisabledChannels = returnDisabledChannels;
    }

    public boolean isAddMoneyFeeAppliedOnWallet() {
        return addMoneyFeeAppliedOnWallet;
    }

    public void setAddMoneyFeeAppliedOnWallet(boolean addMoneyFeeAppliedOnWallet) {
        this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
    }

    public CashierInfoRequestBody() {
    }

    public List<InstrumentType> getInstrumentTypes() {
        return this.instrumentTypes;
    }

    public List<InstrumentType> getSavedInstrumentsTypes() {
        return this.savedInstrumentsTypes;
    }

    public void setInstrumentTypes(List<InstrumentType> instrumentTypes) {
        this.instrumentTypes = instrumentTypes;
    }

    public void setSavedInstrumentsTypes(List<InstrumentType> savedInstrumentsTypes) {
        this.savedInstrumentsTypes = savedInstrumentsTypes;
    }

    public Money getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Money orderAmount) {
        this.orderAmount = orderAmount;
    }

    public List<InstrumentType> getDisabledInstrumentTypes() {
        return disabledInstrumentTypes;
    }

    public void setDisabledInstrumentTypes(List<InstrumentType> disabledInstrumentTypes) {
        this.disabledInstrumentTypes = disabledInstrumentTypes;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getEmiOption() {
        return emiOption;
    }

    public void setEmiOption(String emiOption) {
        this.emiOption = emiOption;
    }

    public String getSubwalletAmount() {
        return subwalletAmount;
    }

    public void setSubwalletAmount(String subwalletAmount) {
        this.subwalletAmount = subwalletAmount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public boolean isMultiAccForVpaSupported() {
        return isMultiAccForVpaSupported;
    }

    public void setMultiAccForVpaSupported(boolean multiAccForVpaSupported) {
        isMultiAccForVpaSupported = multiAccForVpaSupported;
    }

    public String getSubscriptionPaymentMode() {
        return subscriptionPaymentMode;
    }

    public void setSubscriptionPaymentMode(String subscriptionPaymentMode) {
        this.subscriptionPaymentMode = subscriptionPaymentMode;
    }

    public List<GoodsInfo> getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(List<GoodsInfo> goodsInfo) {
        this.goodsInfo = goodsInfo;
    }

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    public boolean isPostpaidOnboardingSupported() {
        return postpaidOnboardingSupported;
    }

    public void setPostpaidOnboardingSupported(boolean postpaidOnboardingSupported) {
        this.postpaidOnboardingSupported = postpaidOnboardingSupported;
    }

    public String getKycCode() {
        return kycCode;
    }

    public void setKycCode(String kycCode) {
        this.kycCode = kycCode;
    }

    public String getKycVersion() {
        return kycVersion;
    }

    public void setKycVersion(String kycVersion) {
        this.kycVersion = kycVersion;
    }

    public NativePersistData getNativePersistData() {
        return nativePersistData;
    }

    public void setNativePersistData(NativePersistData nativePersistData) {
        this.nativePersistData = nativePersistData;
    }

    public boolean isNativeAddMoney() {
        return isNativeAddMoney;
    }

    public void setNativeAddMoney(boolean nativeAddMoney) {
        isNativeAddMoney = nativeAddMoney;
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

    public void setCardHashRequired(boolean cardHashRequired) {
        this.cardHashRequired = cardHashRequired;
    }

    public String getAmountForWalletConsultInRisk() {
        return amountForWalletConsultInRisk;
    }

    public void setAmountForWalletConsultInRisk(String amountForWalletConsultInRisk) {
        this.amountForWalletConsultInRisk = amountForWalletConsultInRisk;
    }

    public String getAmountForPaymentFlow() {
        return amountForPaymentFlow;
    }

    public void setAmountForPaymentFlow(String amountForPaymentFlow) {
        this.amountForPaymentFlow = amountForPaymentFlow;
    }

    public List<String> getBankAccountNumbers() {
        return bankAccountNumbers;
    }

    public void setBankAccountNumbers(List<String> bankAccountNumbers) {
        this.bankAccountNumbers = bankAccountNumbers;
    }

    public SubscriptionTransactionRequestBody getSubscriptionTransactionRequestBody() {
        return subscriptionTransactionRequestBody;
    }

    public void setSubscriptionTransactionRequestBody(
            SubscriptionTransactionRequestBody subscriptionTransactionRequestBody) {
        this.subscriptionTransactionRequestBody = subscriptionTransactionRequestBody;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public MandateMode getMandateType() {
        return mandateType;
    }

    public void setMandateType(MandateMode mandateType) {
        this.mandateType = mandateType;
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

    public SimplifiedPaymentOffers getSimplifiedPaymentOffers() {
        return simplifiedPaymentOffers;
    }

    public void setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers) {
        this.simplifiedPaymentOffers = simplifiedPaymentOffers;
    }

    public boolean isUpiRecurringSupport() {
        return upiRecurringSupport;
    }

    public void setUpiRecurringSupport(boolean upiRecurringSupport) {
        this.upiRecurringSupport = upiRecurringSupport;
    }

    public boolean isUpiPayConfirmSupport() {
        return upiPayConfirmSupport;
    }

    public void setUpiPayConfirmSupport(boolean upiPayConfirmSupport) {
        this.upiPayConfirmSupport = upiPayConfirmSupport;
    }

    public boolean isPreAuth() {
        return preAuth;
    }

    public void setPreAuth(boolean preAuth) {
        this.preAuth = preAuth;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public SubventionDetails getSubventionDetails() {
        return subventionDetails;
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

    public void setSubventionDetails(SubventionDetails subventionDetails) {
        this.subventionDetails = subventionDetails;

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

    public boolean isAppInvoke() {
        return appInvoke;
    }

    public void setAppInvoke(boolean appInvoke) {
        this.appInvoke = appInvoke;
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

    public UltimateBeneficiaryDetails getUltimateBeneficiaryDetails() {
        return ultimateBeneficiaryDetails;
    }

    public void setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
        this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
    }

    public boolean isStaticQrCode() {
        return isStaticQrCode;
    }

    public void setStaticQrCode(boolean staticQrCode) {
        isStaticQrCode = staticQrCode;
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

    public boolean isSuperGwFpoApiHit() {
        return superGwFpoApiHit;
    }

    public void setSuperGwFpoApiHit(boolean superGwFpoApiHit) {
        this.superGwFpoApiHit = superGwFpoApiHit;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).setExcludeFieldNames("bankAccountNumbers").toString();
    }
}
