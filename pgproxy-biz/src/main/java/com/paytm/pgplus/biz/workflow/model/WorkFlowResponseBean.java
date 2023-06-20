/**
 *
 */
package com.paytm.pgplus.biz.workflow.model;

import com.paytm.pgplus.biz.core.model.ActiveSubscriptionBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.CacheCardResponseBean;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.wallet.QRCodeDetailsResponse;
import com.paytm.pgplus.biz.core.risk.RiskConvenienceFee;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.cache.model.EMIDetailList;
import com.paytm.pgplus.facade.common.model.RiskResult;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitAccumulateVo;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitDetail;
import com.paytm.pgplus.facade.merchantlimit.models.response.MerchantLimitInfo;
import com.paytm.pgplus.facade.notification.response.SendMessageResponse;
import com.paytm.pgplus.facade.notification.response.SendPushNotificationResponse;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.ppb.models.FetchAccountBalanceResponse;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.RecurringMandateBank;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.RecurringMandatePsp;
import com.paytm.pgplus.facade.urlshortner.model.response.ShortUrlAPIResponse;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.mappingserviceclient.model.MerchantLimit;
import com.paytm.pgplus.facade.acquiring.models.UltimateBeneficiaryDetails;
import com.paytm.pgplus.payloadvault.merchant.status.response.BankResultInfo;
import com.paytm.pgplus.payloadvault.theia.response.RetryInfo;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author namanjain
 *
 */
public class WorkFlowResponseBean implements Serializable {

    private static final long serialVersionUID = 3855969219892637502L;

    private ConsultPayViewResponseBizBean merchnatViewResponse;
    private ConsultPayViewResponseBizBean addAndPayViewResponse;
    private LitePayviewConsultResponseBizBean merchnatLiteViewResponse;
    private LitePayviewConsultResponseBizBean addAndPayLiteViewResponse;
    private UserDetailsBiz userDetails;
    private InvoiceResponseBean invoiceResponseBean;
    private ConsultFeeResponse consultFeeResponse;
    private String transID;
    private QueryPaymentStatus queryPaymentStatus;
    private QueryTransactionStatus queryTransactionStatus;
    private EPayMode allowedPayMode = EPayMode.NONE;
    private String subscriptionID;
    private String savedCardID;
    private boolean paymentDone;
    private Map<String, String> extendedInfo;
    private SubsTypes subsType;
    private SubscriptionRenewalResponse subscriptionRenewalResponse;
    private AutoDebitResponse autoDebitResponse;
    private CacheCardResponseBean cacheCardResponseBean;
    private String cashierRequestId;
    private String transCreatedTime;
    private SeamlessACSPaymentResponse seamlessACSPaymentResponse;
    private AccountBalanceResponse accountBalanceResponse;
    private MotoResponse motoResponse;
    private QRCodeDetailsResponse qrCodeDetails;
    private MidCustIdCardBizDetails mIdCustIdCardBizDetails;
    private DynamicQrFastForwardResponse dynamicQrFastForwardResponse;
    private PaymentS2SResponse paymentS2SResponse;
    private UPIPSPResponseBody upiPSPResponse;
    private String addMoneyDestination;
    private UserProfileSarvatra sarvatraUserProfile;
    private List<String> sarvatraVpa;
    private boolean isOnTheFlyKYCRequired;
    private BankResultInfo bankResultInfo;
    // UPI Push Cashier flow
    private boolean merchantUpiPushEnabled;
    private boolean addUpiPushEnabled;

    private PromoCodeResponse promoCodeResponse;

    // UPI EXPRESS Cashier flow
    private boolean merchantUpiPushExpressEnabled;
    private boolean addUpiPushExpressEnabled;

    // UPI intent deep link
    private FetchDeepLinkResponseBody fetchDeepLinkResponseBody;

    private WorkFlowRequestBean workFlowRequestBean;

    private String merchantVpa;

    private boolean isZestOnPG2;

    private boolean isZestOnAddMoneyPG2;

    private String mcc;
    private String targetPhoneNo;

    private List<ActiveSubscriptionBeanBiz> activeSubscriptions;
    private boolean isPostpaidEnabledOnMerchantAndDisabledOnUser;
    private WalletLimits walletLimits;

    private UltimateBeneficiaryDetails ultimateBeneficiaryDetails;

    public String getTargetPhoneNo() {
        return targetPhoneNo;
    }

    public void setTargetPhoneNo(String targetPhoneNo) {
        this.targetPhoneNo = targetPhoneNo;
    }

    // for riskExtendedInfo
    private String trustFactor;

    private boolean pcfEnabled;
    /**
     * This field is for showing advance Deposit as a Paymode for travelCorp
     */
    private boolean advanceDepositAvailable;

    private boolean isWalletInactive;

    // ToDo: [PGP-37964] Handling of new to wallet/GV user to be compliant with
    // RBI guidelines
    private boolean isWalletNewUser;
    private boolean offlineFlow;

    private List<RiskConvenienceFee> riskConvenienceFee;

    private EMIDetailList emiDetailList;

    private List<MerchantLimitInfo> merchantLimitInfos;
    private List<LimitAccumulateVo> limitAccumulateVos;

    private UserProfileSarvatraV4 upiProfileV4;

    private String apiVersion;

    private BanksResponse emiSubventionPlans;

    private RetryInfo retryInfo;

    private String productCode;

    private Object channelDetails;

    private String accessToken;

    private String pwpEnabled;

    private boolean prepaidEnabledOnAnyInstrument;

    private List<RecurringMandatePsp> pspList;

    private List<RecurringMandateBank> bankList;

    private Integer merchantLimitType;

    private List<MerchantLimit> merchantPaymodesLimits;

    private boolean bankFormFetchFailed;

    private RiskResult riskResult; /*
                                    * adding risk result to send event link id
                                    * bank response
                                    */

    private ShortUrlAPIResponse shortUrlAPIResponse;

    private SendMessageResponse sendMessageResponse;

    private SendPushNotificationResponse sendPushNotificationResponse;

    private boolean isIdempotent;

    private FetchAccountBalanceResponse ppblAccountResponse;

    private PaytmDigitalCreditResponse paytmCCResponse;

    private List<LimitDetail> limitDetails;

    private boolean bankFormOptimizedFlow;

    private boolean isAddMoneyPcfEnabled;

    private GenerateEsnResponseBody generateEsnResponseBody;

    public List<LimitDetail> getLimitDetails() {
        return limitDetails;
    }

    public void setLimitDetails(List<LimitDetail> limitDetails) {
        this.limitDetails = limitDetails;
    }

    private boolean fetchLRNDetails;

    public boolean isFetchLRNDetails() {
        return fetchLRNDetails;
    }

    public void setFetchLRNDetails(boolean fetchLRNDetails) {
        this.fetchLRNDetails = fetchLRNDetails;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isPcfEnabled() {
        return pcfEnabled;
    }

    public void setPcfEnabled(boolean pcfEnabled) {
        this.pcfEnabled = pcfEnabled;
    }

    public Map<String, String> getExtendedInfo() {
        return extendedInfo;
    }

    public void setExtendedInfo(Map<String, String> extendedInfo) {
        this.extendedInfo = extendedInfo;
    }

    public boolean isPaymentDone() {
        return paymentDone;
    }

    public void setPaymentDone(boolean paymentDone) {
        this.paymentDone = paymentDone;
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public String getSavedCardID() {
        return savedCardID;
    }

    public void setSavedCardID(String savedCardID) {
        this.savedCardID = savedCardID;
    }

    public EPayMode getAllowedPayMode() {
        return allowedPayMode;
    }

    public void setAllowedPayMode(final EPayMode allowedPayMode) {
        this.allowedPayMode = allowedPayMode;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(final String transID) {
        this.transID = transID;
    }

    public QueryPaymentStatus getQueryPaymentStatus() {
        return queryPaymentStatus;
    }

    public void setQueryPaymentStatus(QueryPaymentStatus queryPaymentStatus) {
        this.queryPaymentStatus = queryPaymentStatus;
    }

    public QueryTransactionStatus getQueryTransactionStatus() {
        return queryTransactionStatus;
    }

    public void setQueryTransactionStatus(QueryTransactionStatus queryTransactionStatus) {
        this.queryTransactionStatus = queryTransactionStatus;
    }

    public ConsultFeeResponse getConsultFeeResponse() {
        return consultFeeResponse;
    }

    public void setConsultFeeResponse(final ConsultFeeResponse consultFeeResponse) {
        this.consultFeeResponse = consultFeeResponse;
    }

    public ConsultPayViewResponseBizBean getMerchnatViewResponse() {
        return merchnatViewResponse;
    }

    public void setMerchnatViewResponse(final ConsultPayViewResponseBizBean merchnatViewResponse) {
        this.merchnatViewResponse = merchnatViewResponse;
    }

    public ConsultPayViewResponseBizBean getAddAndPayViewResponse() {
        return addAndPayViewResponse;
    }

    public void setAddAndPayViewResponse(final ConsultPayViewResponseBizBean addAndPayViewResponse) {
        this.addAndPayViewResponse = addAndPayViewResponse;
    }

    public UserDetailsBiz getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(final UserDetailsBiz userDetails) {
        this.userDetails = userDetails;
    }

    public InvoiceResponseBean getInvoiceResponseBean() {
        return invoiceResponseBean;
    }

    public void setInvoiceResponseBean(InvoiceResponseBean invoiceResponseBean) {
        this.invoiceResponseBean = invoiceResponseBean;
    }

    /**
     * @return the subsType
     */
    public SubsTypes getSubsType() {
        return subsType;
    }

    /**
     * @param subsType
     *            the subsType to set
     */
    public void setSubsType(SubsTypes subsType) {
        this.subsType = subsType;
    }

    public SubscriptionRenewalResponse getSubscriptionRenewalResponse() {
        return subscriptionRenewalResponse;
    }

    public void setSubscriptionRenewalResponse(SubscriptionRenewalResponse subscriptionRenewalResponse) {
        this.subscriptionRenewalResponse = subscriptionRenewalResponse;
    }

    public AutoDebitResponse getAutoDebitResponse() {
        return autoDebitResponse;
    }

    public void setAutoDebitResponse(AutoDebitResponse autoDebitResponse) {
        this.autoDebitResponse = autoDebitResponse;
    }

    public CacheCardResponseBean getCacheCardResponseBean() {
        return cacheCardResponseBean;
    }

    public void setCacheCardResponseBean(CacheCardResponseBean cacheCardResponseBean) {
        this.cacheCardResponseBean = cacheCardResponseBean;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getTransCreatedTime() {
        return transCreatedTime;
    }

    public void setTransCreatedTime(String transCreatedTime) {
        this.transCreatedTime = transCreatedTime;
    }

    public SeamlessACSPaymentResponse getSeamlessACSPaymentResponse() {
        return seamlessACSPaymentResponse;
    }

    public void setSeamlessACSPaymentResponse(SeamlessACSPaymentResponse seamlessACSPaymentResponse) {
        this.seamlessACSPaymentResponse = seamlessACSPaymentResponse;
    }

    public AccountBalanceResponse getAccountBalanceResponse() {
        return accountBalanceResponse;
    }

    public void setAccountBalanceResponse(AccountBalanceResponse savingsAccountBalanceResponse) {
        this.accountBalanceResponse = savingsAccountBalanceResponse;
    }

    public LitePayviewConsultResponseBizBean getMerchnatLiteViewResponse() {
        return merchnatLiteViewResponse;
    }

    public void setMerchnatLiteViewResponse(LitePayviewConsultResponseBizBean merchnatLiteViewResponse) {
        this.merchnatLiteViewResponse = merchnatLiteViewResponse;
    }

    public LitePayviewConsultResponseBizBean getAddAndPayLiteViewResponse() {
        return addAndPayLiteViewResponse;
    }

    public void setAddAndPayLiteViewResponse(LitePayviewConsultResponseBizBean addAndPayLiteViewResponse) {
        this.addAndPayLiteViewResponse = addAndPayLiteViewResponse;
    }

    public MotoResponse getMotoResponse() {
        return motoResponse;
    }

    public void setMotoResponse(MotoResponse motoResponse) {
        this.motoResponse = motoResponse;
    }

    public QRCodeDetailsResponse getQrCodeDetails() {
        return qrCodeDetails;
    }

    public void setQrCodeDetails(QRCodeDetailsResponse qrCodeDetails) {
        this.qrCodeDetails = qrCodeDetails;
    }

    public MidCustIdCardBizDetails getmIdCustIdCardBizDetails() {
        return mIdCustIdCardBizDetails;
    }

    public void setmIdCustIdCardBizDetails(MidCustIdCardBizDetails mIdCustIdCardDetails) {
        this.mIdCustIdCardBizDetails = mIdCustIdCardDetails;
    }

    public DynamicQrFastForwardResponse getDynamicQrFastForwardResponse() {
        return dynamicQrFastForwardResponse;
    }

    public void setDynamicQrFastForwardResponse(DynamicQrFastForwardResponse dynamicQrFastForwardResponse) {
        this.dynamicQrFastForwardResponse = dynamicQrFastForwardResponse;
    }

    public PaymentS2SResponse getPaymentS2SResponse() {
        return paymentS2SResponse;
    }

    public void setPaymentS2SResponse(PaymentS2SResponse paymentS2SResponse) {
        this.paymentS2SResponse = paymentS2SResponse;
    }

    public UPIPSPResponseBody getUpiPSPResponse() {
        return upiPSPResponse;
    }

    public void setUpiPSPResponse(UPIPSPResponseBody upiPSPResponse) {
        this.upiPSPResponse = upiPSPResponse;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public UserProfileSarvatra getSarvatraUserProfile() {
        return sarvatraUserProfile;
    }

    public void setSarvatraUserProfile(UserProfileSarvatra sarvatraUserProfile) {
        this.sarvatraUserProfile = sarvatraUserProfile;
    }

    public List<String> getSarvatraVpa() {
        return sarvatraVpa;
    }

    public void setSarvatraVpa(List<String> sarvatraVpa) {
        this.sarvatraVpa = sarvatraVpa;
    }

    public boolean isOnTheFlyKYCRequired() {
        return isOnTheFlyKYCRequired;
    }

    public void setOnTheFlyKYCRequired(boolean onTheFlyKYCRequired) {
        this.isOnTheFlyKYCRequired = onTheFlyKYCRequired;
    }

    public boolean isMerchantUpiPushEnabled() {
        return merchantUpiPushEnabled;
    }

    public void setMerchantUpiPushEnabled(boolean merchantUpiPushEnabled) {
        this.merchantUpiPushEnabled = merchantUpiPushEnabled;
    }

    public boolean isAddUpiPushEnabled() {
        return addUpiPushEnabled;
    }

    public void setAddUpiPushEnabled(boolean addUpiPushEnabled) {
        this.addUpiPushEnabled = addUpiPushEnabled;
    }

    public PromoCodeResponse getPromoCodeResponse() {
        return promoCodeResponse;
    }

    public void setPromoCodeResponse(PromoCodeResponse promoCodeResponse) {
        this.promoCodeResponse = promoCodeResponse;
    }

    public boolean isMerchantUpiPushExpressEnabled() {
        return merchantUpiPushExpressEnabled;
    }

    public void setMerchantUpiPushExpressEnabled(boolean merchantUpiPushExpressEnabled) {
        this.merchantUpiPushExpressEnabled = merchantUpiPushExpressEnabled;
    }

    public boolean isAddUpiPushExpressEnabled() {
        return addUpiPushExpressEnabled;
    }

    public void setAddUpiPushExpressEnabled(boolean addUpiPushExpressEnabled) {
        this.addUpiPushExpressEnabled = addUpiPushExpressEnabled;
    }

    public WorkFlowRequestBean getWorkFlowRequestBean() {
        return workFlowRequestBean;
    }

    public void setWorkFlowRequestBean(WorkFlowRequestBean workFlowRequestBean) {
        this.workFlowRequestBean = workFlowRequestBean;
    }

    public FetchDeepLinkResponseBody getFetchDeepLinkResponseBody() {
        return fetchDeepLinkResponseBody;
    }

    public void setFetchDeepLinkResponseBody(FetchDeepLinkResponseBody fetchDeepLinkResponseBody) {
        this.fetchDeepLinkResponseBody = fetchDeepLinkResponseBody;
    }

    public String getMerchantVpa() {
        return merchantVpa;
    }

    public void setMerchantVpa(String merchantVpa) {
        this.merchantVpa = merchantVpa;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getTrustFactor() {
        return trustFactor;
    }

    public void setTrustFactor(String trustFactor) {
        this.trustFactor = trustFactor;
    }

    public List<ActiveSubscriptionBeanBiz> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(List<ActiveSubscriptionBeanBiz> activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public boolean isAdvanceDepositAvailable() {
        return advanceDepositAvailable;
    }

    public void setAdvanceDepositAvailable(boolean advanceDepositAvailable) {
        this.advanceDepositAvailable = advanceDepositAvailable;
    }

    public List<RiskConvenienceFee> getRiskConvenienceFee() {
        return riskConvenienceFee;
    }

    public void setRiskConvenienceFee(List<RiskConvenienceFee> riskConvenienceFee) {
        this.riskConvenienceFee = riskConvenienceFee;
    }

    public EMIDetailList getEmiDetailList() {
        return emiDetailList;
    }

    public void setEmiDetailList(EMIDetailList emiDetailList) {
        this.emiDetailList = emiDetailList;
    }

    public List<LimitAccumulateVo> getLimitAccumulateVos() {
        return limitAccumulateVos;
    }

    public List<MerchantLimitInfo> getMerchantLimitInfos() {
        return merchantLimitInfos;
    }

    public void setMerchantLimitInfos(List<MerchantLimitInfo> merchantLimitInfos) {
        this.merchantLimitInfos = merchantLimitInfos;
    }

    public void setLimitAccumulateVos(List<LimitAccumulateVo> limitAccumulateVos) {
        this.limitAccumulateVos = limitAccumulateVos;
    }

    public BanksResponse getEmiSubventionPlans() {
        return emiSubventionPlans;
    }

    public void setEmiSubventionPlans(BanksResponse emiSubventionPlans) {
        this.emiSubventionPlans = emiSubventionPlans;
    }

    public UserProfileSarvatraV4 getUpiProfileV4() {
        return upiProfileV4;
    }

    public void setUpiProfileV4(UserProfileSarvatraV4 upiProfileV4) {
        this.upiProfileV4 = upiProfileV4;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public RetryInfo getRetryInfo() {
        return retryInfo;
    }

    public void setRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
    }

    public String getPwpEnabled() {
        return pwpEnabled;
    }

    public void setPwpEnabled(String pwpEnabled) {
        this.pwpEnabled = pwpEnabled;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public List<RecurringMandatePsp> getPspList() {
        return pspList;
    }

    public void setPspList(List<RecurringMandatePsp> pspList) {
        this.pspList = pspList;
    }

    public List<RecurringMandateBank> getBankList() {
        return bankList;
    }

    public void setBankList(List<RecurringMandateBank> bankList) {
        this.bankList = bankList;
    }

    public void setChannelDetails(Object channelDetails) {
        this.channelDetails = channelDetails;
    }

    public Object getChannelDetails() {
        return channelDetails;
    }

    public boolean isPrepaidEnabledOnAnyInstrument() {
        return prepaidEnabledOnAnyInstrument;
    }

    public void setPrepaidEnabledOnAnyInstrument(boolean prepaidEnabledOnAnyInstrument) {
        this.prepaidEnabledOnAnyInstrument = prepaidEnabledOnAnyInstrument;
    }

    public Integer getMerchantLimitType() {
        return merchantLimitType;
    }

    public void setMerchantLimitType(Integer merchantLimitType) {
        this.merchantLimitType = merchantLimitType;
    }

    public List<MerchantLimit> getmerchantPaymodesLimits() {
        return merchantPaymodesLimits;
    }

    public void setmerchantPaymodesLimits(List<MerchantLimit> merchantPaymodesLimits) {
        this.merchantPaymodesLimits = merchantPaymodesLimits;
    }

    public boolean isBankFormFetchFailed() {
        return bankFormFetchFailed;
    }

    public void setBankFormFetchFailed(boolean bankFormFetchFailed) {
        this.bankFormFetchFailed = bankFormFetchFailed;
    }

    public RiskResult getRiskResult() {
        return riskResult;
    }

    public void setRiskResult(RiskResult riskResult) {
        this.riskResult = riskResult;
    }

    public ShortUrlAPIResponse getShortUrlAPIResponse() {
        return shortUrlAPIResponse;
    }

    public void setShortUrlAPIResponse(ShortUrlAPIResponse shortUrlAPIResponse) {
        this.shortUrlAPIResponse = shortUrlAPIResponse;
    }

    public SendMessageResponse getSendMessageResponse() {
        return sendMessageResponse;
    }

    public void setSendMessageResponse(SendMessageResponse sendMessageResponse) {
        this.sendMessageResponse = sendMessageResponse;
    }

    public SendPushNotificationResponse getSendPushNotificationResponse() {
        return sendPushNotificationResponse;
    }

    public void setSendPushNotificationResponse(SendPushNotificationResponse sendPushNotificationResponse) {
        this.sendPushNotificationResponse = sendPushNotificationResponse;
    }

    public boolean isIdempotent() {
        return isIdempotent;
    }

    public void setIdempotent(boolean idempotent) {
        isIdempotent = idempotent;
    }

    public FetchAccountBalanceResponse getPpblAccountResponse() {
        return ppblAccountResponse;
    }

    public void setPpblAccountResponse(FetchAccountBalanceResponse ppblAccountResponse) {
        this.ppblAccountResponse = ppblAccountResponse;
    }

    public PaytmDigitalCreditResponse getPaytmCCResponse() {
        return paytmCCResponse;
    }

    public void setPaytmCCResponse(PaytmDigitalCreditResponse paytmCCResponse) {
        this.paytmCCResponse = paytmCCResponse;
    }

    public BankResultInfo getBankResultInfo() {
        return bankResultInfo;
    }

    public void setBankResultInfo(BankResultInfo bankResultInfo) {
        this.bankResultInfo = bankResultInfo;
    }

    public boolean isPostpaidEnabledOnMerchantAndDisabledOnUser() {
        return isPostpaidEnabledOnMerchantAndDisabledOnUser;
    }

    public void setPostpaidEnabledOnMerchantAndDisabledOnUser(boolean postpaidEnabledOnMerchantAndDisabledOnUser) {
        isPostpaidEnabledOnMerchantAndDisabledOnUser = postpaidEnabledOnMerchantAndDisabledOnUser;
    }

    public boolean isWalletInactive() {
        return isWalletInactive;
    }

    public void setWalletInactive(boolean walletInactive) {
        isWalletInactive = walletInactive;
    }

    public boolean isWalletNewUser() {
        return isWalletNewUser;
    }

    public void setWalletNewUser(boolean walletNewUser) {
        isWalletNewUser = walletNewUser;
    }

    public boolean isOfflineFlow() {
        return offlineFlow;
    }

    public void setOfflineFlow(boolean offlineFlow) {
        this.offlineFlow = offlineFlow;
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

    public boolean isZestOnPG2() {
        return isZestOnPG2;
    }

    public void setZestOnPG2(boolean zestOnPG2) {
        isZestOnPG2 = zestOnPG2;
    }

    public boolean isZestOnAddMoneyPG2() {
        return isZestOnAddMoneyPG2;
    }

    public void setZestOnAddMoneyPG2(boolean zestOnAddMoneyPG2) {
        isZestOnAddMoneyPG2 = zestOnAddMoneyPG2;
    }

    public boolean isBankFormOptimizedFlow() {
        return bankFormOptimizedFlow;
    }

    public void setBankFormOptimizedFlow(boolean bankFormOptimizedFlow) {
        this.bankFormOptimizedFlow = bankFormOptimizedFlow;
    }

    public boolean isAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    public GenerateEsnResponseBody getGenerateEsnResponseBody() {
        return generateEsnResponseBody;
    }

    public void setGenerateEsnResponseBody(GenerateEsnResponseBody generateEsnResponseBody) {
        this.generateEsnResponseBody = generateEsnResponseBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkFlowResponseBean{");
        sb.append("accountBalanceResponse=").append(accountBalanceResponse);
        sb.append(", addAndPayLiteViewResponse=").append(addAndPayLiteViewResponse);
        sb.append(", addAndPayViewResponse=").append(addAndPayViewResponse);
        sb.append(", addMoneyDestination='").append(addMoneyDestination).append('\'');
        sb.append(", allowedPayMode=").append(allowedPayMode);
        sb.append(", autoDebitResponse=").append(autoDebitResponse);
        sb.append(", cacheCardResponseBean=").append(cacheCardResponseBean);
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append(", consultFeeResponse=").append(consultFeeResponse);
        sb.append(", dynamicQrFastForwardResponse=").append(dynamicQrFastForwardResponse);
        sb.append(", extendedInfo=").append(extendedInfo);
        sb.append(", invoiceResponseBean=").append(invoiceResponseBean);
        sb.append(", isOnTheFlyKYCRequired=").append(isOnTheFlyKYCRequired);
        sb.append(", merchnatLiteViewResponse=").append(merchnatLiteViewResponse);
        sb.append(", merchnatViewResponse=").append(merchnatViewResponse);
        sb.append(", mIdCustIdCardBizDetails=").append(mIdCustIdCardBizDetails);
        sb.append(", motoResponse=").append(motoResponse);
        sb.append(", paymentDone=").append(paymentDone);
        sb.append(", paymentS2SResponse=").append(paymentS2SResponse);
        sb.append(", qrCodeDetails=").append(qrCodeDetails);
        sb.append(", queryPaymentStatus=").append(queryPaymentStatus);
        sb.append(", queryTransactionStatus=").append(queryTransactionStatus);
        sb.append(", sarvatraUserProfile=").append(sarvatraUserProfile);
        sb.append(", sarvatraVpa=").append(sarvatraVpa);
        sb.append(", savedCardID='").append(savedCardID).append('\'');
        sb.append(", seamlessACSPaymentResponse=").append(seamlessACSPaymentResponse);
        sb.append(", subscriptionID='").append(subscriptionID).append('\'');
        sb.append(", subscriptionRenewalResponse=").append(subscriptionRenewalResponse);
        sb.append(", subsType=").append(subsType);
        sb.append(", transCreatedTime='").append(transCreatedTime).append('\'');
        sb.append(", transID='").append(transID).append('\'');
        sb.append(", upiPSPResponse=").append(upiPSPResponse);
        sb.append(", userDetails=").append(userDetails);
        sb.append(", merchantUpiPushEnabled=").append(merchantUpiPushEnabled);
        sb.append(", addUpiPushEnabled=").append(addUpiPushEnabled);
        sb.append(", promoCodeResponse=").append(promoCodeResponse);
        sb.append(", merchantUpiPushExpressEnabled=").append(merchantUpiPushExpressEnabled);
        sb.append(", addUpiPushExpressEnabled=").append(addUpiPushExpressEnabled);
        sb.append(", fetchDeepLinkResponseBody=").append(fetchDeepLinkResponseBody);
        sb.append(", merchantVpa=").append(merchantVpa);
        sb.append(", mcc=").append(mcc);
        sb.append(", activeSubscriptions=").append(activeSubscriptions);
        sb.append(", advanceDepositAvailable=").append(advanceDepositAvailable);
        sb.append(", emiSubventionPlans=").append(emiSubventionPlans);
        sb.append(", emiDetailList=").append(emiDetailList);
        sb.append(", upiProfileV4=").append(upiProfileV4);
        sb.append(", apiVersion=").append(apiVersion);
        sb.append(", retryInfo=").append(retryInfo);
        sb.append(", productCode=").append(productCode);
        sb.append(", channelDetails=").append(channelDetails);
        sb.append(", pwpEnabled=").append(pwpEnabled);
        sb.append(", pspList=").append(pspList);
        sb.append(", bankList=").append(bankList);
        sb.append(", merchantLimitType=").append(merchantLimitType);
        sb.append(", merchantPaymodesLimits=").append(merchantPaymodesLimits);
        sb.append(", bankResultInfo=").append(bankResultInfo);
        sb.append(", isWalletNewUser=").append(isWalletNewUser);
        sb.append(", walletLimits=").append(walletLimits);
        sb.append(", ultimateBeneficiaryDetails").append(ultimateBeneficiaryDetails);
        sb.append(", isZestOnPG2=").append(isZestOnPG2);
        sb.append(", isZestOnAddMoneyPG2").append(isZestOnAddMoneyPG2);
        sb.append(", isBankFormOptimizedFlow").append(bankFormOptimizedFlow);
        sb.append(", isAddMoneyPcfEnabled=").append(isAddMoneyPcfEnabled);
        sb.append(",generateEsnResponseBody=").append(generateEsnResponseBody);
        sb.append('}');
        return sb.toString();
    }

}
