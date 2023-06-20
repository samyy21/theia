/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.biz.core.model.ActiveSubscriptionBeanBiz;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.MidCustIdCardBizDetails;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.ppb.AccountBalanceResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultFeeResponse;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.TokenizedCardsResponseBizBean;
import com.paytm.pgplus.biz.core.risk.RiskConvenienceFee;
import com.paytm.pgplus.biz.core.validator.service.IValidator;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.coft.model.CoftSavedCards;
import com.paytm.pgplus.facade.common.model.RiskResult;
import com.paytm.pgplus.facade.emisubvention.models.response.BanksResponse;
import com.paytm.pgplus.facade.merchantlimit.models.response.*;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.RecurringMandateBank;
import com.paytm.pgplus.facade.upi.model.RecurringBanksAndPspModel.RecurringMandatePsp;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.mappingserviceclient.model.MerchantLimit;
import com.paytm.pgplus.models.TwoFARespData;
import com.paytm.pgplus.payloadvault.merchant.status.response.BankResultInfo;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.pgproxycommon.models.QueryTransactionStatus;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;

/**
 * @author namanjain
 *
 */
public class WorkFlowTransactionBean implements Serializable {

    private static final long serialVersionUID = 154435242525252L;

    private WorkFlowRequestBean workFlowBean;
    private UserDetailsBiz userDetails;
    private String transID;
    private ConsultPayViewResponseBizBean merchantViewConsult;
    private ConsultPayViewResponseBizBean addAndPayViewConsult;
    private LitePayviewConsultResponseBizBean merchantLiteViewConsult;
    private TokenizedCardsResponseBizBean tokenizedCards;
    private LitePayviewConsultResponseBizBean addAndPayLiteViewConsult;
    private String cacheCardToken;
    private String cashierRequestId;
    private String webFormContext;
    private String paymentStatus;
    private CardBeanBiz savedCard;
    private EPayMode allowedPayMode;
    private String invoiceLink;
    private ConsultFeeResponse consultFeeResponse;
    private boolean isPostConvenienceFeeModel;
    private QueryPaymentStatus queryPaymentStatus;
    private QueryTransactionStatus queryTransactionStatus;
    private InvoiceResponseBean invoiceResponseBean;
    private boolean paymentDone;
    private String productCode;
    private SubscriptionResponse subscriptionServiceResponse;
    private String subscriptionPayMode;
    private EnvInfoRequestBean envInfoReqBean;
    private String transCreatedTime;
    private String goodsInfo;
    private String shippingInfo;
    private Map<String, String> channelInfo;
    private Map<String, String> extendInfo;
    private MidCustIdCardBizDetails midCustIdCardBizDetails;
    private AccountBalanceResponse accountBalanceResponse;
    private String addMoneyDestination;
    private UserProfileSarvatra sarvatraUserProfile;
    private List<String> sarvatraVpa;
    private boolean isOnTheFlyKYCRequired;
    private BankResultInfo bankResultInfo;

    // UPI Push Cashier flow
    private boolean merchantUpiPushEnabled;
    private boolean addUpiPushEnabled;
    // UPI EXPRESS Cashier flow
    private boolean merchantUpiPushExpressEnabled;
    private boolean addUpiPushExpressEnabled;
    // Hack to not filter PPB in offline/native
    private boolean defaultLiteViewFlow;
    private IValidator validator;

    // for riskExtendedInfo
    private String trustFactor;

    private List<ActiveSubscriptionBeanBiz> activeSubscriptions;
    private List<RiskConvenienceFee> riskConvenienceFee;

    private List<LimitAccumulateVo> limitAccumulateVos;
    private List<MerchantLimitInfo> merchantLimitInfos;
    private EmiSubventionPaymentInstrument emiSubventionPaymentInstruments;

    private BanksResponse emiSubventionPlans;
    private String cardIndexNo;

    private UserProfileSarvatraV4 sarvatraUserProfileV4;
    private Object channelDetails;

    private boolean assetReconcilationStatus = true;

    private Date debitDate;

    private String pwpEnabled;

    private List<RecurringMandateBank> bankList;
    private List<RecurringMandatePsp> pspList;

    private Integer merchantLimitType;

    private List<MerchantLimit> merchantPaymodesLimits;

    private RiskResult riskResult;

    private String paymentPromoCheckoutDataPromoCode;

    private boolean modifyOrderRequired;
    private boolean failTxnIfModifyOrderFails;

    private boolean isIdempotent;

    private boolean isWalletInactive;

    private List<CoftSavedCards> platformAndTokenCards;

    private String customLooperTimeout;

    private boolean isFastForwardRequest;

    private boolean isWalletNewUser;
    private WalletLimits walletLimits;

    private List<LimitDetail> limitDetails;

    private TwoFARespData twoFAConfig;

    private boolean isAddMoneyPcfEnabled;

    private String feeRateCode;

    public List<LimitDetail> getLimitDetails() {
        return limitDetails;
    }

    public void setLimitDetails(List<LimitDetail> limitDetails) {
        this.limitDetails = limitDetails;
    }

    public boolean isModifyOrderRequired() {
        return modifyOrderRequired;
    }

    public void setModifyOrderRequired(boolean modifyOrderRequired) {
        this.modifyOrderRequired = modifyOrderRequired;
    }

    public boolean isFailTxnIfModifyOrderFails() {
        return failTxnIfModifyOrderFails;
    }

    public void setFailTxnIfModifyOrderFails(boolean failTxnIfModifyOrderFails) {
        this.failTxnIfModifyOrderFails = failTxnIfModifyOrderFails;
    }

    public WorkFlowTransactionBean() {
    }

    public WorkFlowTransactionBean(WorkFlowRequestBean workFlowBean) {
        this.workFlowBean = workFlowBean;
    }

    public WorkFlowRequestBean getWorkFlowBean() {
        return workFlowBean;
    }

    public void setWorkFlowBean(WorkFlowRequestBean workFlowBean) {
        this.workFlowBean = workFlowBean;
    }

    public UserDetailsBiz getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsBiz userDetails) {
        this.userDetails = userDetails;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String transID) {
        this.transID = transID;
    }

    public ConsultPayViewResponseBizBean getMerchantViewConsult() {
        return merchantViewConsult;
    }

    public void setMerchantViewConsult(ConsultPayViewResponseBizBean merchantViewConsult) {
        this.merchantViewConsult = merchantViewConsult;
    }

    public ConsultPayViewResponseBizBean getAddAndPayViewConsult() {
        return addAndPayViewConsult;
    }

    public void setAddAndPayViewConsult(ConsultPayViewResponseBizBean addAndPayViewConsult) {
        this.addAndPayViewConsult = addAndPayViewConsult;
    }

    public LitePayviewConsultResponseBizBean getMerchantLiteViewConsult() {
        return merchantLiteViewConsult;
    }

    public void setMerchantLiteViewConsult(LitePayviewConsultResponseBizBean merchantLiteViewConsult) {
        this.merchantLiteViewConsult = merchantLiteViewConsult;
    }

    public TokenizedCardsResponseBizBean getTokenizedCards() {
        return tokenizedCards;
    }

    public void setTokenizedCards(TokenizedCardsResponseBizBean tokenizedCards) {
        this.tokenizedCards = tokenizedCards;
    }

    public LitePayviewConsultResponseBizBean getAddAndPayLiteViewConsult() {
        return addAndPayLiteViewConsult;
    }

    public void setAddAndPayLiteViewConsult(LitePayviewConsultResponseBizBean addAndPayLiteViewConsult) {
        this.addAndPayLiteViewConsult = addAndPayLiteViewConsult;
    }

    public String getCacheCardToken() {
        return cacheCardToken;
    }

    public void setCacheCardToken(String cacheCardToken) {
        this.cacheCardToken = cacheCardToken;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public String getWebFormContext() {
        return webFormContext;
    }

    public void setWebFormContext(String webFormContext) {
        this.webFormContext = webFormContext;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public CardBeanBiz getSavedCard() {
        return savedCard;
    }

    public void setSavedCard(CardBeanBiz savedCard) {
        this.savedCard = savedCard;
    }

    public EPayMode getAllowedPayMode() {
        return allowedPayMode;
    }

    public void setAllowedPayMode(EPayMode allowedPayMode) {
        this.allowedPayMode = allowedPayMode;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public void setInvoiceLink(String invoiceLink) {
        this.invoiceLink = invoiceLink;
    }

    public ConsultFeeResponse getConsultFeeResponse() {
        return consultFeeResponse;
    }

    public void setConsultFeeResponse(ConsultFeeResponse consultFeeResponse) {
        this.consultFeeResponse = consultFeeResponse;
    }

    public boolean isPostConvenienceFeeModel() {
        return isPostConvenienceFeeModel;
    }

    public void setPostConvenienceFeeModel(boolean postConvenienceFeeModel) {
        isPostConvenienceFeeModel = postConvenienceFeeModel;
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

    public InvoiceResponseBean getInvoiceResponseBean() {
        return invoiceResponseBean;
    }

    public void setInvoiceResponseBean(InvoiceResponseBean invoiceResponseBean) {
        this.invoiceResponseBean = invoiceResponseBean;
    }

    public boolean isPaymentDone() {
        return paymentDone;
    }

    public void setPaymentDone(boolean paymentDone) {
        this.paymentDone = paymentDone;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public SubscriptionResponse getSubscriptionServiceResponse() {
        return subscriptionServiceResponse;
    }

    public void setSubscriptionServiceResponse(SubscriptionResponse subscriptionServiceResponse) {
        this.subscriptionServiceResponse = subscriptionServiceResponse;
    }

    public String getSubscriptionPayMode() {
        return subscriptionPayMode;
    }

    public void setSubscriptionPayMode(String subscriptionPayMode) {
        this.subscriptionPayMode = subscriptionPayMode;
    }

    public EnvInfoRequestBean getEnvInfoReqBean() {
        return envInfoReqBean;
    }

    public void setEnvInfoReqBean(EnvInfoRequestBean envInfoReqBean) {
        this.envInfoReqBean = envInfoReqBean;
    }

    public String getTransCreatedTime() {
        return transCreatedTime;
    }

    public void setTransCreatedTime(String transCreatedTime) {
        this.transCreatedTime = transCreatedTime;
    }

    public String getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(String goodsInfo) {
        this.goodsInfo = goodsInfo;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(Map<String, String> channelInfo) {
        this.channelInfo = channelInfo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public MidCustIdCardBizDetails getMidCustIdCardBizDetails() {
        return midCustIdCardBizDetails;
    }

    public void setMidCustIdCardBizDetails(MidCustIdCardBizDetails midCustIdCardBizDetails) {
        this.midCustIdCardBizDetails = midCustIdCardBizDetails;
    }

    public AccountBalanceResponse getAccountBalanceResponse() {
        return accountBalanceResponse;
    }

    public void setAccountBalanceResponse(AccountBalanceResponse accountBalanceResponse) {
        this.accountBalanceResponse = accountBalanceResponse;
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

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
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

    public boolean isDefaultLiteViewFlow() {
        return defaultLiteViewFlow;
    }

    public void setDefaultLiteViewFlow(boolean defaultLiteViewFlow) {
        this.defaultLiteViewFlow = defaultLiteViewFlow;
    }

    public IValidator getValidator() {
        return validator;
    }

    public void setValidator(IValidator validator) {
        this.validator = validator;
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

    public List<RiskConvenienceFee> getRiskConvenienceFee() {
        return riskConvenienceFee;
    }

    public void setRiskConvenienceFee(List<RiskConvenienceFee> riskConvenienceFee) {
        this.riskConvenienceFee = riskConvenienceFee;
    }

    public List<LimitAccumulateVo> getLimitAccumulateVos() {
        return limitAccumulateVos;
    }

    public void setLimitAccumulateVos(List<LimitAccumulateVo> limitAccumulateVos) {
        this.limitAccumulateVos = limitAccumulateVos;
    }

    public List<MerchantLimitInfo> getMerchantLimitInfos() {
        return merchantLimitInfos;
    }

    public void setMerchantLimitInfos(List<MerchantLimitInfo> merchantLimitInfos) {
        this.merchantLimitInfos = merchantLimitInfos;
    }

    public EmiSubventionPaymentInstrument getEmiSubventionPaymentInstruments() {
        return emiSubventionPaymentInstruments;
    }

    public void setEmiSubventionPaymentInstruments(EmiSubventionPaymentInstrument emiSubventionPaymentInstruments) {
        this.emiSubventionPaymentInstruments = emiSubventionPaymentInstruments;
    }

    public BanksResponse getEmiSubventionPlans() {
        return emiSubventionPlans;
    }

    public void setEmiSubventionPlans(BanksResponse emiSubventionPlans) {
        this.emiSubventionPlans = emiSubventionPlans;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public UserProfileSarvatraV4 getSarvatraUserProfileV4() {
        return sarvatraUserProfileV4;
    }

    public void setSarvatraUserProfileV4(UserProfileSarvatraV4 sarvatraUserProfileV4) {
        this.sarvatraUserProfileV4 = sarvatraUserProfileV4;
    }

    public void setChannelDetails(Object channelDetails) {
        this.channelDetails = channelDetails;
    }

    public Object getChannelDetails() {
        return channelDetails;
    }

    public boolean isAssetReconcilationStatus() {
        return assetReconcilationStatus;
    }

    public void setAssetReconcilationStatus(boolean assetReconcilationStatus) {
        this.assetReconcilationStatus = assetReconcilationStatus;
    }

    public Date getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(Date debitDate) {
        this.debitDate = debitDate;
    }

    public String getPwpEnabled() {
        return pwpEnabled;
    }

    public void setPwpEnabled(String pwpEnabled) {
        this.pwpEnabled = pwpEnabled;
    }

    public List<RecurringMandateBank> getBankList() {
        return bankList;
    }

    public void setBankList(List<RecurringMandateBank> bankList) {
        this.bankList = bankList;
    }

    public List<RecurringMandatePsp> getPspList() {
        return pspList;
    }

    public void setPspList(List<RecurringMandatePsp> pspList) {
        this.pspList = pspList;
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

    public RiskResult getRiskResult() {
        return riskResult;
    }

    public void setRiskResult(RiskResult riskResult) {
        this.riskResult = riskResult;
    }

    public String getPaymentPromoCheckoutDataPromoCode() {
        return paymentPromoCheckoutDataPromoCode;
    }

    public void setPaymentPromoCheckoutDataPromoCode(String paymentPromoCheckoutDataPromoCode) {
        this.paymentPromoCheckoutDataPromoCode = paymentPromoCheckoutDataPromoCode;
    }

    public boolean isIdempotent() {
        return isIdempotent;
    }

    public void setIdempotent(boolean idempotent) {
        isIdempotent = idempotent;
    }

    public BankResultInfo getBankResultInfo() {
        return bankResultInfo;
    }

    public List<CoftSavedCards> getPlatformAndTokenCards() {
        return platformAndTokenCards;
    }

    public void setPlatformAndTokenCards(List<CoftSavedCards> platformAndTokenCards) {
        this.platformAndTokenCards = platformAndTokenCards;
    }

    public boolean isWalletInactive() {
        return isWalletInactive;
    }

    public void setWalletInactive(boolean walletInactive) {
        isWalletInactive = walletInactive;
    }

    public String getCustomLooperTimeout() {
        return customLooperTimeout;
    }

    public void setCustomLooperTimeout(String customLooperTimeout) {
        this.customLooperTimeout = customLooperTimeout;
    }

    public boolean isFastForwardRequest() {
        return isFastForwardRequest;
    }

    public void setFastForwardRequest(boolean fastForwardRequest) {
        isFastForwardRequest = fastForwardRequest;
    }

    public boolean isWalletNewUser() {
        return isWalletNewUser;
    }

    public void setWalletNewUser(boolean walletNewUser) {
        isWalletNewUser = walletNewUser;
    }

    public WalletLimits getWalletLimits() {
        return walletLimits;
    }

    public void setWalletLimits(WalletLimits walletLimits) {
        this.walletLimits = walletLimits;
    }

    public TwoFARespData getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFARespData twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    public boolean isAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    public String getFeeRateCode() {
        return feeRateCode;
    }

    public void setFeeRateCode(String feeRateCode) {
        this.feeRateCode = feeRateCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkFlowTransactionBean{");
        sb.append("accountBalanceResponse=").append(accountBalanceResponse);
        sb.append(", addAndPayLiteViewConsult=").append(addAndPayLiteViewConsult);
        sb.append(", addAndPayViewConsult=").append(addAndPayViewConsult);
        sb.append(", addMoneyDestination='").append(addMoneyDestination).append('\'');
        sb.append(", allowedPayMode=").append(allowedPayMode);
        sb.append(", cacheCardToken='").append(cacheCardToken).append('\'');
        sb.append(", cashierRequestId='").append(cashierRequestId).append('\'');
        sb.append(", channelInfo=").append(channelInfo);
        sb.append(", consultFeeResponse=").append(consultFeeResponse);
        sb.append(", envInfoReqBean=").append(envInfoReqBean);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append(", goodsInfo='").append(goodsInfo).append('\'');
        sb.append(", invoiceLink='").append(invoiceLink).append('\'');
        sb.append(", invoiceResponseBean=").append(invoiceResponseBean);
        sb.append(", isOnTheFlyKYCRequired=").append(isOnTheFlyKYCRequired);
        sb.append(", isPostConvenienceFeeModel=").append(isPostConvenienceFeeModel);
        sb.append(", merchantLiteViewConsult=").append(merchantLiteViewConsult);
        sb.append(", merchantViewConsult=").append(merchantViewConsult);
        sb.append(", midCustIdCardBizDetails=").append(midCustIdCardBizDetails);
        sb.append(", paymentDone=").append(paymentDone);
        sb.append(", paymentStatus='").append(paymentStatus).append('\'');
        sb.append(", productCode='").append(productCode).append('\'');
        sb.append(", queryPaymentStatus=").append(queryPaymentStatus);
        sb.append(", queryTransactionStatus=").append(queryTransactionStatus);
        sb.append(", sarvatraUserProfile=").append(sarvatraUserProfile);
        sb.append(", sarvatraVpa=").append(sarvatraVpa);
        sb.append(", savedCard=").append(savedCard);
        sb.append(", shippingInfo='").append(shippingInfo).append('\'');
        sb.append(", subscriptionPayMode='").append(subscriptionPayMode).append('\'');
        sb.append(", subscriptionServiceResponse=").append(subscriptionServiceResponse);
        sb.append(", transCreatedTime='").append(transCreatedTime).append('\'');
        sb.append(", transID='").append(transID).append('\'');
        sb.append(", userDetails=").append(userDetails);
        sb.append(", webFormContext='").append(webFormContext).append('\'');
        sb.append(", merchantUpiPushEnabled='").append(merchantUpiPushEnabled).append('\'');
        sb.append(", addUpiPushEnabled='").append(addUpiPushEnabled).append('\'');
        sb.append(", merchantUpiPushExpressEnabled='").append(merchantUpiPushExpressEnabled).append('\'');
        sb.append(", addUpiPushExpressEnabled='").append(addUpiPushExpressEnabled).append('\'');
        sb.append(", defaultLiteViewFlow'").append(defaultLiteViewFlow).append('\'');
        sb.append(", activeSubscriptions'").append(activeSubscriptions).append('\'');
        sb.append(", emiSubventionPlans'").append(emiSubventionPlans).append('\'');
        sb.append(", workFlowBean=").append(workFlowBean);
        sb.append(", sarvatraUserProfileV4=").append(sarvatraUserProfileV4);
        sb.append(", channelDetails=").append(channelDetails);
        sb.append(", debitDate=").append(debitDate);
        sb.append(", pwpEnabled=").append(pwpEnabled);
        sb.append(", bankList=").append(bankList);
        sb.append(", pspList=").append(pspList);
        sb.append(", merchantLimitType=").append(merchantLimitType);
        sb.append(", merchantPaymodesLimits=").append(merchantPaymodesLimits);
        sb.append(", bankResultInfo=").append(bankResultInfo);
        sb.append(", platformAndTokenCards=").append(platformAndTokenCards);
        sb.append(", isWalletNewUser=").append(isWalletNewUser);
        sb.append(", walletLimits=").append(walletLimits);
        sb.append(", twoFAConfig=").append(twoFAConfig);
        sb.append(", isAddMoneyPcfEnabled=").append(isAddMoneyPcfEnabled);
        sb.append(", feeRateCode=").append(feeRateCode);
        sb.append('}');
        return sb.toString();
    }
}