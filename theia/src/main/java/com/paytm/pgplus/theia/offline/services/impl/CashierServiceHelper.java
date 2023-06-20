package com.paytm.pgplus.theia.offline.services.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.*;
import com.paytm.pgplus.biz.core.user.service.ISarvatraUserProfile;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.FetchUserPaytmVpaRequest;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.models.SuccessRateCacheModel;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.PayMethodOnboardingUtil;
import com.paytm.pgplus.theia.offline.enums.AuthMode;
import com.paytm.pgplus.theia.offline.enums.CountryCode;
import com.paytm.pgplus.theia.offline.enums.InstrumentType;
import com.paytm.pgplus.theia.offline.enums.PaymentFlow;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.payview.UPI;
import com.paytm.pgplus.theia.offline.model.payview.*;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.services.IOfflinePaymentService;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.PrepaidCardValidationUtil;
import com.paytm.pgplus.theia.utils.helper.SuccessRateUtils;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.KYC_CODE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.KYC_VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.PrepaidCard.PREPAID_CARD_MAX_AMOUNT;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by rahulverma on 5/9/17.
 */
@Component
public class CashierServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CashierServiceHelper.class);

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    @Qualifier("offlinePaymentService")
    private IOfflinePaymentService offlinePaymentService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("successRateUtils")
    private SuccessRateUtils successRateUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    @Qualifier("userProfileSarvatra")
    private ISarvatraUserProfile sarvatraVpaDetails;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("vpaHelper")
    private VPAHelper vpaHelper;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("prepaidCardValidationUtil")
    protected PrepaidCardValidationUtil prepaidCardValidationUtil;

    public void validateRequestBean(CashierInfoRequest cashierInfoRequest) {
        offlinePaymentService.validateRequestBean(cashierInfoRequest);
    }

    public PaymentRequestBean cashierInfoRequestToPaymentRequestBean(CashierInfoRequest cashierInfoRequest) {
        HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
        makeBackwardCompatibleHttpServletRequest(httpServletRequest, cashierInfoRequest);
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean(httpServletRequest, true);
        paymentRequestBean.setOfflineFetchPayApi(true);
        paymentRequestBean.setExtraParamsMap(cashierInfoRequest.getBody().getExtendInfo());
        return paymentRequestBean;
    }

    public CashierInfoResponse processPaymentRequestAndMapResponse(PaymentRequestBean paymentRequestBean,
            CashierInfoRequest cashierInfoRequest) {
        WorkFlowResponseBean workFlowResponseBean = offlinePaymentService.processPaymentRequest(paymentRequestBean);
        CashierInfoResponse cashierInfoResponse = mapWorkFlowResponseBeanToCashierInfoResponse(workFlowResponseBean,
                cashierInfoRequest, paymentRequestBean);
        makePaytmPaymentBankAsSeparatePayMethod(cashierInfoResponse, workFlowResponseBean);
        return cashierInfoResponse;
    }

    // it removes digital credit if balance is not sufficient
    public void removeDigitalCreditIfBalanceInSufficient(CashierInfoResponse cashierInfoResponse,
            PaymentRequestBean requestBean) {
        if (cashierInfoResponse.getBody().getPayMethodViews() == null
                || cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods() == null) {
            return;
        }
        Iterator<PayMethod> payMethodIterator = cashierInfoResponse.getBody().getPayMethodViews()
                .getMerchantPayMethods().iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethod.getPayMethod())) {
                DigitalCredit digitalCredit = null;
                try {
                    digitalCredit = (DigitalCredit) payMethod.getPayChannelOptions().get(0);
                } catch (Exception e) {
                    LOGGER.error("Not able to cast payChannelBase to digital credit");
                    return;
                }
                /**
                 * Offline flow backend handling for App Failure in Handling
                 * Deactive and Frozen Cases
                 */
                /**
                 * Commented After Management Decision
                 */
                /*
                 * if (digitalCredit != null && digitalCredit.getExtendInfo() !=
                 * null &&
                 * digitalCredit.getExtendInfo().containsKey(ACCOUNT_STATUS)) {
                 * String accountStatus =
                 * digitalCredit.getExtendInfo().get(ACCOUNT_STATUS); if
                 * (ACCOUNT_STATUS_DEACTIVE.equals(accountStatus) ||
                 * ACCOUNT_STATUS_FROZEN.equals(accountStatus) ||
                 * ACCOUNT_STATUS_ON_HOLD.equals(accountStatus)) {
                 * LOGGER.info("Removing digital credit due to accountStatus {}"
                 * , accountStatus); payMethodIterator.remove(); return; } }
                 */
                if (digitalCredit != null && digitalCredit.getBalanceInfo() != null
                        && digitalCredit.getBalanceInfo().getAccountBalance() != null) {
                    String balance = digitalCredit.getBalanceInfo().getAccountBalance().getValue();
                    if (NumberUtils.isNumber(balance) && NumberUtils.isNumber(requestBean.getTxnAmount())) {
                        if (Double.parseDouble(balance) < Double.parseDouble(requestBean.getTxnAmount())) {
                            LOGGER.info("Removing digital credit due to insufficient balance");
                            payMethodIterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void makePaytmPaymentBankAsSeparatePayMethod(CashierInfoResponse cashierInfoResponse,
            WorkFlowResponseBean workFlowResponseBean) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null) {
            return;
        }
        PayMethodViews payMethodViews = cashierInfoResponse.getBody().getPayMethodViews();
        if (payMethodViews.getMerchantPayMethods() != null) {
            createPPBMethodFromPPBNetBanking(payMethodViews.getMerchantPayMethods(), workFlowResponseBean);
        }
        if (payMethodViews.getAddMoneyPayMethods() != null) {
            createPPBMethodFromPPBNetBanking(payMethodViews.getAddMoneyPayMethods(), workFlowResponseBean);
        }
    }

    private void createPPBMethodFromPPBNetBanking(List<PayMethod> payMethods, WorkFlowResponseBean workFlowResponseBean) {
        boolean paymentsBankEnabled = false;
        PayMethod payMethodPPBL = new PayMethod();
        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())) {
                for (Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator(); payChannelBaseIterator
                        .hasNext();) {
                    PayChannelBase nextPayChannelBase = payChannelBaseIterator.next();
                    if (EPayMethod.PPBL.getMethod().equals(nextPayChannelBase.getPayChannelOption())) {
                        paymentsBankEnabled = true;
                        if (payMethodPPBL != null) {
                            payMethodPPBL.setPayMethod(EPayMethod.PPBL.getMethod());
                            payMethodPPBL.setDisplayName(EPayMethod.PPBL.getDisplayName());
                            payMethodPPBL.setIsDisabled(nextPayChannelBase.getIsDisabled());
                            payMethodPPBL.setPayChannelOptions(new ArrayList<>());
                            payMethodPPBL.getPayChannelOptions().add(
                                    balanceChannelInfo(nextPayChannelBase,
                                            getBalanceInfoForPPBL(workFlowResponseBean.getUserDetails())));
                        }

                        payChannelBaseIterator.remove();
                        break;
                    }

                }
            }
        }

        if (paymentsBankEnabled) {
            payMethods.add(payMethodPPBL);
        }
    }

    private BalanceInfo getBalanceInfoForPPBL(UserDetailsBiz userDetailsBiz) {
        return commonFacade.getPaytmBankBalanceInfo(userDetailsBiz);
    }

    private BalanceChannel balanceChannelInfo(PayChannelBase payChannelBase, BalanceInfo balanceInfo) {
        if (payChannelBase == null)
            return new BalanceChannel();
        return new BalanceChannel(payChannelBase.getPayMethod(), payChannelBase.getPayChannelOption(),
                payChannelBase.getIsDisabled(), payChannelBase.getHasLowSuccess(), payChannelBase.getIconUrl(),
                balanceInfo);
    }

    private String getCustomizeCode(CashierInfoRequest cashierInfoRequest) {
        // TODO: prepare customize code based on instrument type and saved
        // instrument type
        String costomizeCode = "";
        return costomizeCode;
    }

    private void makeBackwardCompatibleHttpServletRequest(HttpServletRequest httpServletRequest,
            CashierInfoRequest cashierInfoRequest) {
        RequestHeader requestHeader = cashierInfoRequest.getHead();
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoRequest.getBody();
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.MID, requestHeader.getMid());
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.SSO_TOKEN, requestHeader.getToken());
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.INDUSTRY_TYPE_ID,
                cashierInfoRequestBody.getIndustryTypeId());
        // Todo:fix order id requirement
        String orderId = (StringUtils.isNotEmpty(cashierInfoRequestBody.getOrderId())) ? cashierInfoRequestBody
                .getOrderId() : "dummyOrderId";
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.ORDER_ID, orderId);
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.REQUEST_TYPE, TheiaConstant.RequestTypes.DEFAULT);
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.DEVICE_ID, cashierInfoRequestBody.getDeviceId());
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.CHANNEL_ID, cashierInfoRequestBody.getChannelId()
                .getValue());
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.CUSTOMIZE_CODE,
                getCustomizeCode(cashierInfoRequest));
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.TXN_AMOUNT,
                TheiaConstant.ExtraConstants.OFFLINE_TXN_AMOUNT);
        httpServletRequest.setAttribute(TheiaConstant.RequestParams.POSTPAID_ONBOARDING_SUPPORTED,
                cashierInfoRequestBody.isPostpaidOnboardingSupported());
        httpServletRequest.setAttribute(KYC_CODE, cashierInfoRequestBody.getKycCode());
        httpServletRequest.setAttribute(KYC_VERSION, cashierInfoRequestBody.getKycVersion());
    }

    private CashierInfoResponse mapWorkFlowResponseBeanToCashierInfoResponse(WorkFlowResponseBean workFlowResponseBean,
            CashierInfoRequest cashierInfoRequest, PaymentRequestBean paymentRequestBean) {
        LOGGER.debug("Mapping WorkflowResponseBean to CasheirInfo Response ...");

        CashierInfoResponseBody cashierInfoResponseBody = new CashierInfoResponseBody();
        cashierInfoResponseBody.setEnabledFlows(getEnabledFlows(workFlowResponseBean.getAllowedPayMode()));
        cashierInfoResponseBody.setOrderId(getOrderId(cashierInfoRequest));
        OfflinePaymentUtils.updateOrderIdInMDC(cashierInfoResponseBody.getOrderId());
        PayMethodViews payMethodViews = new PayMethodViews();
        LitePayviewConsultResponseBizBean merchnatLiteViewResponse = workFlowResponseBean.getMerchnatLiteViewResponse();
        LitePayviewConsultResponseBizBean addAndPayLiteViewResponse = workFlowResponseBean
                .getAddAndPayLiteViewResponse();
        UserDetailsBiz userDetailsBiz = workFlowResponseBean.getUserDetails();
        EChannelId eChannelId = cashierInfoRequest.getBody().getChannelId();
        List<PayMethod> merchantPayMethods = null;
        List<PayMethod> addMoneyPayMethods = null;

        boolean isPrepaidCardFeatureEnabled = iPgpFf4jClient.checkWithdefault(
                TheiaConstant.PrepaidCard.FF4J_PREPAID_CARD_STRING, new HashMap<>(), false);

        if (merchnatLiteViewResponse != null) {
            cashierInfoResponseBody.setPostConvenienceFee(new PostConvenienceFee(String
                    .valueOf(merchnatLiteViewResponse.isChargePayer())));
            merchantPayMethods = getPayMethods(merchnatLiteViewResponse.getPayMethodViews(), eChannelId,
                    userDetailsBiz, workFlowResponseBean.getAllowedPayMode(), workFlowResponseBean, cashierInfoRequest
                            .getBody().getOrderAmount(), false, isPrepaidCardFeatureEnabled);
            payMethodViews.setMerchantPayMethods(merchantPayMethods);
        }
        if (addAndPayLiteViewResponse != null && EPayMode.ADDANDPAY.equals(workFlowResponseBean.getAllowedPayMode())) {
            addMoneyPayMethods = getPayMethods(addAndPayLiteViewResponse.getPayMethodViews(), eChannelId,
                    userDetailsBiz, workFlowResponseBean.getAllowedPayMode(), workFlowResponseBean, cashierInfoRequest
                            .getBody().getOrderAmount(), true, isPrepaidCardFeatureEnabled);
            payMethodViews.setAddMoneyPayMethods(addMoneyPayMethods);
        }
        if (userDetailsBiz != null) {
            payMethodViews.setMerchantSavedInstruments(getSavedInstruments(
                    userDetailsBiz.getMerchantViewSavedCardsList(), eChannelId, merchantPayMethods,
                    cashierInfoRequest.getBody(), workFlowResponseBean, false, isPrepaidCardFeatureEnabled));
            payMethodViews.setAddMoneySavedInstruments(getSavedInstruments(
                    userDetailsBiz.getAddAndPayViewSavedCardsList(), eChannelId, addMoneyPayMethods,
                    cashierInfoRequest.getBody(), workFlowResponseBean, true, isPrepaidCardFeatureEnabled));
        }
        populateSarvatraVPAs(payMethodViews.getMerchantSavedInstruments(), cashierInfoRequest, workFlowResponseBean,
                paymentRequestBean);
        if (cashierInfoResponseBody.getEnabledFlows().contains(PaymentFlow.ADD_AND_PAY)) {
            populateSarvatraVPAs(payMethodViews.getAddMoneySavedInstruments(), cashierInfoRequest,
                    workFlowResponseBean, paymentRequestBean);
        }
        cashierInfoResponseBody.setPayMethodViews(payMethodViews);

        if (workFlowResponseBean.isPrepaidEnabledOnAnyInstrument()) {
            cashierInfoResponseBody.setPrepaidCardMaxAmount(ConfigurationUtil.getProperty(PREPAID_CARD_MAX_AMOUNT,
                    "100000"));
        }

        // TODO:to Check extend info
        cashierInfoResponseBody.setExtendInfoString(workFlowResponseBean.getExtendedInfo());
        cashierInfoResponseBody.setResultInfo(OfflinePaymentUtils.resultInfoForSuccess());
        cashierInfoResponseBody.setSignature("");
        CashierInfoResponse cashierInfoResponse = new CashierInfoResponse();
        cashierInfoResponse.setBody(cashierInfoResponseBody);
        cashierInfoResponse.setHead(new ResponseHeader(cashierInfoRequest.getHead()));
        LOGGER.debug("CashierInfoResponse {}", cashierInfoResponse);
        return cashierInfoResponse;
    }

    private String getOrderId(CashierInfoRequest cashierInfoRequest) {
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoRequest.getBody();
        if (cashierInfoRequestBody != null && StringUtils.isNotEmpty(cashierInfoRequestBody.getOrderId())) {
            LOGGER.info("OrderId is received in fetchinstrument request, returning the same {}",
                    cashierInfoRequestBody.getOrderId());
            return cashierInfoRequestBody.getOrderId();
        }

        String aggregatorMid = merchantDataUtil.getAggregatorMid(cashierInfoRequest.getHead().getMid());

        return OfflinePaymentUtils.generateOrderId(aggregatorMid);
    }

    private SavedInstruments getSavedInstruments(List<CardBeanBiz> cardBeanBizs, EChannelId eChannelId,
            List<PayMethod> payMethods, CashierInfoRequestBody cashierInfoRequestBody,
            WorkFlowResponseBean workFlowResponseBean, boolean addnPay, boolean isPrepaidFeatureEnabled) {
        List<SavedCard> savedCards = new ArrayList<>();
        // TODO:Empty for now
        List<SavedVPA> savedVPAs = new ArrayList<>();
        SavedInstruments savedInstruments = new SavedInstruments(savedCards, savedVPAs);
        if (cardBeanBizs == null)
            return savedInstruments;

        for (CardBeanBiz cardBeanBiz : cardBeanBizs) {
            CardDetails cardDetails = new CardDetails();
            SavedCard savedCard = new SavedCard(cardDetails);
            if (cardBeanBiz.getCardId() != null) {
                cardDetails.setCardId(String.valueOf(cardBeanBiz.getCardId()));
            } else {
                cardDetails.setCardId(cardBeanBiz.getCardIndexNo());
            }
            cardDetails.setCardType(cardBeanBiz.getCardType());
            // Strictly should not be sent
            // cardDetails.setCardNumber(cardBeanBiz.getCardNumber());
            // cardDetails.setExpiryDate(cardBeanBiz.getExpiryDate());
            // TODO:to check
            cardDetails.setCreated_on("");
            cardDetails.setUpdated_on("");

            cardDetails.setFirstSixDigit(String.valueOf(cardBeanBiz.getFirstSixDigit()));
            cardDetails.setLastFourDigit(NativePaymentUtil.getLastFourDigits(cardBeanBiz.getLastFourDigit()));
            cardDetails.setUserId(cardBeanBiz.getUserId());
            cardDetails.setStatus(String.valueOf(cardBeanBiz.getStatus()));
            savedCard.setIssuingBank(cardBeanBiz.getInstId());

            // TODO : Need to consult logic
            String payChannelOption = cardBeanBiz.getCardType() + "_" + cardBeanBiz.getCardScheme();
            savedCard.setPayChannelOption(payChannelOption);

            savedCard.setPayMethod(cardBeanBiz.getCardType());
            PayChannelBase payChannelBase = getPayChannelOptionFromPayMethods(payMethods, cardBeanBiz.getCardType(),
                    payChannelOption);
            if (payChannelBase == null && !(payChannelBase instanceof BankCard))
                continue;
            BankCard bankCard = (BankCard) payChannelBase;
            if (!validPayChannelBaseForSavedCard(cardBeanBiz, bankCard, cashierInfoRequestBody, addnPay,
                    workFlowResponseBean, isPrepaidFeatureEnabled)) {
                continue;
            }

            if (isPrepaidFeatureEnabled) {
                savedCard.setPrepaidCard(cardBeanBiz.isPrepaidCard());
                savedCard.setPrepaidCardSupported(null);
            }
            savedCard.setInstId(bankCard.getInstId());
            savedCard.setInstName(bankCard.getInstName());
            savedCard.setSupportedCountries(bankCard.getSupportedCountries());
            savedCard.setIsDisabled(bankCard.getIsDisabled());
            savedCard.setIconUrl(commonFacade.getLogoUrl(bankCard.getInstId(), eChannelId));
            savedCard.setHasLowSuccess(bankCard.getHasLowSuccess());
            boolean isDirectPayOption = getIsDirectPayOption(savedCard, payMethods);
            if (isDirectPayOption) {
                List<String> iDebitOptionsList = new ArrayList<String>();
                iDebitOptionsList.add(AuthMode.PIN.getType());
                iDebitOptionsList.add(AuthMode.OTP.getType());
                savedCard.setAuthModes(iDebitOptionsList);

            }
            savedCards.add(savedCard);
        }
        return savedInstruments;
    }

    boolean validPayChannelBaseForSavedCard(CardBeanBiz cardBeanBiz, BankCard bankCard,
            CashierInfoRequestBody cashierInfoRequestBody, boolean addnPay, WorkFlowResponseBean flowResponseBean,
            boolean isPrepaidFeatureEnabled) {

        boolean validSavedPrepaidCard = false;

        if (!cardBeanBiz.isPrepaidCard() || !isPrepaidFeatureEnabled)
            return true;

        if (addnPay)
            return false;
        if (BooleanUtils.isTrue(bankCard.getPrepaidCardSupported())
                && (cashierInfoRequestBody.getOrderAmount() == null || prepaidCardValidationUtil
                        .isPrepaidCardLimitValid(cashierInfoRequestBody.getOrderAmount().getValue(), false))) {
            validSavedPrepaidCard = true;
            flowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
        }
        return validSavedPrepaidCard;

    }

    private PayChannelBase getPayChannelOptionFromPayMethods(List<PayMethod> payMethods, String payMethod,
            String payChannelOption) {
        if (payMethods == null || payMethods.isEmpty() || StringUtils.isEmpty(payMethod)
                || StringUtils.isEmpty(payChannelOption))
            return null;
        for (PayMethod payMethod1 : payMethods) {
            if (payMethod.equals(payMethod1.getPayMethod())) {
                if (payMethod1.getPayChannelOptions() == null || payMethod1.getPayChannelOptions().isEmpty()) {
                    break;
                }
                for (PayChannelBase payChannelBase : payMethod1.getPayChannelOptions()) {
                    if (payChannelOption.equals(payChannelBase.getPayChannelOption())) {
                        return payChannelBase;
                    }
                }
            }
        }
        return null;
    }

    private List<PaymentFlow> getEnabledFlows(EPayMode ePayMode) {
        List<PaymentFlow> flows = getDefaultPaymentFlows();
        flows.add(PaymentFlow.paymentFlowByEPayMode(ePayMode));
        return flows;
    }

    // TODO:This is disabled as per discussion
    // now payment flow can have only hybrid or addandpay
    private List<PaymentFlow> getDefaultPaymentFlows() {
        List<PaymentFlow> paymentFlows = new ArrayList<>();
        // Disabling for now
        /*
         * paymentFlows.add(PaymentFlow.PG_ONLY);
         * paymentFlows.add(PaymentFlow.PPI);
         */
        return paymentFlows;
    }

    private List<PayMethod> getPayMethods(List<PayMethodViewsBiz> payMethodViewsBizs, EChannelId eChannelId,
            UserDetailsBiz userDetailsBiz, EPayMode paymentFlow, WorkFlowResponseBean workFlowResponseBean,
            Money orderAmount, boolean isAddMoneyPaymodes, boolean isPrepaidCardFeatureEnabled) {
        List<PayMethod> payMethods = new ArrayList<>();
        if (payMethodViewsBizs == null)
            return payMethods;

        SuccessRateCacheModel successRateCacheModel = successRateUtils.getSuccessRateCacheModel();

        Map<String, BankInfoData> bankInfoMap = getBankInfoDataMap(payMethodViewsBizs);

        for (PayMethodViewsBiz payMethodViewsBiz : payMethodViewsBizs) {
            PayMethod payMethod = new PayMethod();
            payMethod.setPayChannelOptions(getPayChannelOptions(payMethod,
                    payMethodViewsBiz.getPayChannelOptionViews(), payMethodViewsBiz.getPayMethod(), eChannelId,
                    successRateCacheModel, bankInfoMap, paymentFlow, workFlowResponseBean, orderAmount,
                    isAddMoneyPaymodes, isPrepaidCardFeatureEnabled));

            EPayMethod payMethodEnum = EPayMethod.getPayMethodByMethod(payMethodViewsBiz.getPayMethod());
            payMethod.setPayMethod(payMethodEnum.getMethod());
            payMethod.setDisplayName(payMethodEnum.getDisplayName());
            payMethod.setIsDisabled(new StatusInfo(Boolean.FALSE.toString(), ""));
            payMethod.setOnboarding(PayMethodOnboardingUtil.getOnboarding(payMethodEnum, userDetailsBiz));
            payMethods.add(payMethod);

        }
        return payMethods;
    }

    public void filterUpiPayOptions(List<PayMethod> payMethods) {
        boolean upiPush = false, upiPushExpress = false;

        if (payMethods == null || payMethods.isEmpty()) {
            return;
        }
        PayMethod upiPayMethod = null;
        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod())) {
                upiPayMethod = payMethod;
                break;
            }
        }
        if (upiPayMethod != null) {
            // Hack to remove UPI_PUSH_EXPRESS as a separate pay channel if
            // configured
            for (PayChannelBase payChannel : upiPayMethod.getPayChannelOptions()) {
                if (TheiaConstant.BasicPayOption.UPI_PUSH.equals(payChannel.getPayChannelOption())
                        && payChannel.getIsDisabled() != null
                        && Boolean.FALSE.toString().equals(payChannel.getIsDisabled().getStatus())) {
                    upiPush = true;
                }
                if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayChannelOption())
                        && payChannel.getIsDisabled() != null
                        && Boolean.FALSE.toString().equals(payChannel.getIsDisabled().getStatus())) {
                    upiPushExpress = true;
                }
            }
            if (!upiPush && upiPushExpress) {
                for (Iterator<PayChannelBase> it = upiPayMethod.getPayChannelOptions().iterator(); it.hasNext();) {
                    UPI payChannel = (UPI) it.next();
                    if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayChannelOption())) {
                        payChannel.setPayChannelOption("UPI_PUSH");
                        payChannel.setInstId("UPIPUSH");
                        payChannel.setInstName("Unified Payment Interface - PUSH");
                    }
                }
            }
        }
    }

    private Map<String, BankInfoData> getBankInfoDataMap(List<PayMethodViewsBiz> payMethodViewsBizs) {
        List<String> bankCodeList = new ArrayList<>();
        payMethodViewsBizs.stream().forEach(
                payMethodViewsBiz -> {
                    bankCodeList.addAll(payMethodViewsBiz.getPayChannelOptionViews().parallelStream()
                            .filter(Objects::nonNull)
                            .map(payChannelOptionViewBiz -> payChannelOptionViewBiz.getInstId())
                            .filter(Objects::nonNull).collect(Collectors.toList()));
                });
        if (CollectionUtils.isEmpty(bankCodeList)) {
            return Collections.emptyMap();
        }
        List<BankInfoData> bankInfoList = commonFacade.getBankInfoDataListFromBankCodes(bankCodeList);
        if (CollectionUtils.isEmpty(bankInfoList)) {
            return Collections.emptyMap();
        }
        Map<String, BankInfoData> map = new HashMap<>();
        for (BankInfoData bankInfo : bankInfoList) {
            map.put(bankInfo.getBankCode(), bankInfo);
        }
        return map;
    }

    private boolean getSuccessRateFlag(String payMethod, SuccessRateCacheModel successRateCacheModel,
            PayChannelOptionViewBiz payChannelOptionViewBiz) {
        if (null == successRateCacheModel) {
            return false;
        }
        return hasLowSuccessRate(payChannelOptionViewBiz, payMethod, successRateCacheModel);
    }

    private List<PayChannelBase> getPayChannelOptions(PayMethod payMethod,
            List<PayChannelOptionViewBiz> payChannelOptionViewBizs, String payMethodStr, EChannelId eChannelId,
            SuccessRateCacheModel successRateCacheModel, Map<String, BankInfoData> bankInfoMap, EPayMode paymentFlow,
            WorkFlowResponseBean workFlowResponseBean, Money orderAmount, boolean isAddMoneyPaymodes,
            boolean isPrepaidCardFeatureEnabled) {
        List<PayChannelBase> payChannelOptions = new ArrayList<>();
        if (payChannelOptionViewBizs == null)
            return payChannelOptions;
        boolean prepaidCardSupportedForPayMethod = false;
        boolean checkForAddAndPay = (!isAddMoneyPaymodes || !EPayMode.ADDANDPAY.equals(paymentFlow));
        for (PayChannelOptionViewBiz payChannelOptionViewBiz : payChannelOptionViewBizs) {
            PayChannelBase payChannelBase = null;
            boolean isPrepaidCardSupported = (payChannelOptionViewBiz.isPrepaidCardChannel() && checkForAddAndPay);
            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethodStr)) {
                CreditCard creditCard = new CreditCard();
                creditCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                creditCard.setInstId(payChannelOptionViewBiz.getInstId());
                creditCard.setInstName(payChannelOptionViewBiz.getInstName());
                if (isPrepaidCardFeatureEnabled) {
                    creditCard.setPrepaidCardSupported(isPrepaidCardSupported);
                }
                prepaidCardSupportedForPayMethod = false;
                payChannelBase = creditCard;
            }
            if (EPayMethod.DEBIT_CARD.getMethod().equals(payMethodStr)) {
                DebitCard debitCard = new DebitCard();
                debitCard.setSupportedCountries(toListOfCountryCodes(payChannelOptionViewBiz.getSupportCountries()));
                debitCard.setInstId(payChannelOptionViewBiz.getInstId());
                debitCard.setInstName(payChannelOptionViewBiz.getInstName());
                if (isPrepaidCardFeatureEnabled) {
                    debitCard.setPrepaidCardSupported(false);
                    if (isPrepaidCardSupported
                            && (orderAmount == null || orderAmount.getValue() == null || prepaidCardValidationUtil
                                    .isPrepaidCardLimitValid(orderAmount.getValue(), false))) {
                        debitCard.setPrepaidCardSupported(true);
                        prepaidCardSupportedForPayMethod = true;
                        workFlowResponseBean.setPrepaidEnabledOnAnyInstrument(true);
                    }
                }
                payChannelBase = debitCard;
            }
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethodStr)) {
                NetBanking netBanking = new NetBanking();
                netBanking.setInstId(payChannelOptionViewBiz.getInstId());
                netBanking.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = netBanking;
            }
            if (EPayMethod.UPI.getMethod().equals(payMethodStr)) {
                UPI upi = new UPI();
                upi.setInstId(payChannelOptionViewBiz.getInstId());
                upi.setInstName(payChannelOptionViewBiz.getInstName());
                payChannelBase = upi;
            }
            if (EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equals(payMethodStr)) {
                DigitalCredit digitalCredit = new DigitalCredit();
                digitalCredit.setBalanceInfo(getBalanceInfoFromExternalAccount(payChannelOptionViewBiz
                        .getExternalAccountInfos()));
                digitalCredit.setExtendInfo(getExtendInfoFromExternalAccount(payChannelOptionViewBiz
                        .getExternalAccountInfos()));
                payChannelBase = digitalCredit;
            }
            if (EPayMethod.BALANCE.getMethod().equals(payMethodStr)) {
                Wallet wallet = new Wallet();
                wallet.setBalanceInfo(getBalanceInfo(payChannelOptionViewBiz.getBalanceChannelInfos()));
                payChannelBase = wallet;
            }
            if (payChannelBase == null)
                continue;
            boolean successRateFlag = getSuccessRateFlag(payMethodStr, successRateCacheModel, payChannelOptionViewBiz);
            payChannelBase.setHasLowSuccess(new StatusInfo(String.valueOf(successRateFlag), OfflinePaymentUtils
                    .successRateMsg(successRateFlag)));
            payChannelBase.setIsDisabled(new StatusInfo(String.valueOf(!payChannelOptionViewBiz.isEnableStatus()),
                    payChannelOptionViewBiz.getDisableReason()));
            payChannelBase.setPayChannelOption(payChannelOptionViewBiz.getPayOption());
            payChannelBase.setPayMethod(payMethodStr);
            payChannelBase.setDirectServiceInsts(payChannelOptionViewBiz.getDirectServiceInsts());
            if (!EPayMethod.CREDIT_CARD.getMethod().equals(payMethodStr)
                    && !EPayMethod.DEBIT_CARD.getMethod().equals(payMethodStr)) {
                payChannelBase.setIconUrl(commonFacade.getLogoNameV1(payChannelOptionViewBiz.getInstId()));
            }
            payChannelOptions.add(payChannelBase);
        }
        if (isPrepaidCardFeatureEnabled) {
            setPrepaidCardForPayMethod(payMethod, payMethodStr, prepaidCardSupportedForPayMethod);
        }
        return payChannelOptions;
    }

    private void setPrepaidCardForPayMethod(PayMethod payMethod, String payMethodStr,
            boolean prepaidCardSupportedForPayMethod) {
        switch (payMethodStr) {
        case CREDIT_CARD:
        case EMI_DC:
        case EMI:
            payMethod.setPrepaidCardSupported(prepaidCardSupportedForPayMethod);
            break;
        case DEBIT_CARD:
            payMethod.setPrepaidCardSupported(prepaidCardSupportedForPayMethod);
            break;
        default:
            return;
        }
    }

    private boolean hasLowSuccessRate(PayChannelOptionViewBiz payChannelOptionViewBiz, String payMethod,
            SuccessRateCacheModel successRateCacheModel) {
        if (payChannelOptionViewBiz == null || StringUtils.isEmpty(payChannelOptionViewBiz.getInstId())
                || StringUtils.isEmpty(payMethod))
            return false;
        return commonFacade.hasLowSuccessRate(payChannelOptionViewBiz.getInstId(), payMethod, successRateCacheModel);
    }

    private BalanceInfo getBalanceInfo(List<BalanceChannelInfoBiz> balanceChannelInfoBizs) {
        // TODO:check
        if (balanceChannelInfoBizs == null || balanceChannelInfoBizs.isEmpty() || balanceChannelInfoBizs.get(0) == null)
            return null;
        BalanceInfo balanceInfo = new BalanceInfo(balanceChannelInfoBizs.get(0).getPayerAccountNo(), new Money(
                balanceChannelInfoBizs.get(0).getAccountBalance()), true);
        return balanceInfo;
    }

    private DigitalCreditBalanceInfo getBalanceInfoFromExternalAccount(
            List<ExternalAccountInfoBiz> externalAccountInfoBizs) {
        if (externalAccountInfoBizs == null || externalAccountInfoBizs.isEmpty())
            return null;

        ExternalAccountInfoBiz externalAccountInfoBiz = externalAccountInfoBizs.get(0);
        if (externalAccountInfoBiz == null || StringUtils.isEmpty(externalAccountInfoBiz.getAccountBalance())
                || StringUtils.isEmpty(externalAccountInfoBiz.getExternalAccountNo()))
            return null;
        DigitalCreditBalanceInfo balanceInfo = new DigitalCreditBalanceInfo(
                externalAccountInfoBiz.getExternalAccountNo(), new Money(externalAccountInfoBiz.getAccountBalance()),
                externalAccountInfoBiz.getExtendInfo(), true);
        return balanceInfo;
    }

    private Map<String, String> getExtendInfoFromExternalAccount(List<ExternalAccountInfoBiz> externalAccountInfoBizs) {
        Map<String, String> map = null;
        if (externalAccountInfoBizs == null || externalAccountInfoBizs.isEmpty())
            return null;
        final ExternalAccountInfoBiz externalAccountInfoBiz = externalAccountInfoBizs.get(0);
        if (externalAccountInfoBiz == null || StringUtils.isBlank(externalAccountInfoBiz.getExtendInfo()))
            return null;
        try {
            map = JsonMapper.mapJsonToObject(externalAccountInfoBiz.getExtendInfo(), Map.class);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Error while converting extendInfo to Map in PDC");
        }
        return map;
    }

    private List<CountryCode> toListOfCountryCodes(List<String> countries) {
        if (countries == null)
            return Collections.emptyList();
        List<CountryCode> countryCodes = new ArrayList<>();
        for (String country : countries) {
            countryCodes.add(CountryCode.valueOf(country));
        }
        return countryCodes;
    }

    private PayChannelBase getPayChannelObject(String payOption) {
        if (StringUtils.isEmpty(payOption))
            return null;
        if (EPayMethod.CREDIT_CARD.equals(payOption)) {
            return new CreditCard();
        }
        return null;
    }

    public void filterDisabledPayMethods(CashierInfoResponse cashierInfoResponse) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null)
            return;
        filterDisabledPayMethods(cashierInfoResponse.getBody().getPayMethodViews().getMerchantPayMethods());
        filterDisabledPayMethods(cashierInfoResponse.getBody().getPayMethodViews().getAddMoneyPayMethods());

    }

    public void filterDisabledSavedInstruments(CashierInfoResponse cashierInfoResponse) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null
                || cashierInfoResponse.getBody().getPayMethodViews() == null)
            return;
        filterDisabledSavedInstruments(cashierInfoResponse.getBody().getPayMethodViews().getMerchantSavedInstruments());
        filterDisabledSavedInstruments(cashierInfoResponse.getBody().getPayMethodViews().getAddMoneySavedInstruments());

    }

    public void populateSarvatraVPAs(SavedInstruments savedInstruments, CashierInfoRequest cashierInfoRequest,
            WorkFlowResponseBean responseBean, PaymentRequestBean requestBean) {
        UserProfileSarvatra sarvatraUserProfile = new UserProfileSarvatra();
        List<String> sarvatraVpa = new ArrayList<>();
        boolean isMultiAccForVpaSupported = cashierInfoRequest.getBody().isMultiAccForVpaSupported();
        String token = cashierInfoRequest.getHead().getToken();
        String mids = "ALL";
        if (TaskFlowUtils.isMidEligibleForTaskFlow(requestBean.getMid(), mids)) {
            if (null != responseBean.getSarvatraUserProfile()
                    && responseBean.getSarvatraUserProfile().getResponse() != null
                    && "SUCCESS".equalsIgnoreCase(responseBean.getSarvatraUserProfile().getStatus())) {
                if (isMultiAccForVpaSupported) {
                    vpaHelper.populateVPALinkedBankAccounts(responseBean.getSarvatraUserProfile());
                }
                sarvatraUserProfile = new UserProfileSarvatra(responseBean.getSarvatraUserProfile().getStatus(),
                        responseBean.getSarvatraUserProfile().getResponse());

                sarvatraVpa = responseBean.getSarvatraVpa();
            }
        } else {
            FetchUserPaytmVpaRequest fetchUserPaytmVpaRequest = new FetchUserPaytmVpaRequest(token);
            if (responseBean.getUserDetails() != null) {
                fetchUserPaytmVpaRequest.setUserId(responseBean.getUserDetails().getUserId());
                fetchUserPaytmVpaRequest.setQueryParams((requestBean != null ? requestBean.getQueryParams() : null));
            }
            GenericCoreResponseBean<UserProfileSarvatra> userProfileSarvatra = sarvatraVpaDetails
                    .fetchUserProfileVpa(fetchUserPaytmVpaRequest);
            if (null != userProfileSarvatra && userProfileSarvatra.getResponse() != null
                    && "SUCCESS".equalsIgnoreCase(userProfileSarvatra.getResponse().getStatus())) {
                // Populate bank-accounts in vpa-response
                if (isMultiAccForVpaSupported) {
                    vpaHelper.populateVPALinkedBankAccounts(userProfileSarvatra.getResponse());
                }

                sarvatraUserProfile = new UserProfileSarvatra(userProfileSarvatra.getResponse().getStatus(),
                        userProfileSarvatra.getResponse().getResponse());
                sarvatraVpa = workFlowHelper.getSarvatraVPAList(userProfileSarvatra.getResponse().getResponse());
            }
        }
        savedInstruments.setSarvatraVpa(sarvatraVpa);
        savedInstruments.setSarvatraUserProfile(sarvatraUserProfile);

    }

    public void trimResponse(CashierInfoResponse cashierInfoResponse, CashierInfoRequest cashierInfoRequest) {
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null)
            return;
        List<InstrumentType> instrumentTypes = cashierInfoRequest.getBody().getInstrumentTypes();
        List<InstrumentType> savedInstrumentsTypes = cashierInfoRequest.getBody().getSavedInstrumentsTypes();

        int netbankingOptionSize = instrumentTypes.contains(InstrumentType.NB_TOP5) ? 5 : -1;

        CashierInfoResponseBody cashierInfoResponseBody = cashierInfoResponse.getBody();
        List<PayMethod> merchantPayMethods = cashierInfoResponseBody.getPayMethodViews().getMerchantPayMethods();
        List<PayMethod> addMoneyPayMethods = cashierInfoResponseBody.getPayMethodViews().getAddMoneyPayMethods();
        SavedInstruments merchantSavedInstruments = cashierInfoResponseBody.getPayMethodViews()
                .getMerchantSavedInstruments();
        SavedInstruments addMoneySavedInstruments = cashierInfoResponseBody.getPayMethodViews()
                .getAddMoneySavedInstruments();

        trimInstrumentTypes(instrumentTypes, merchantPayMethods);
        trimInstrumentTypes(instrumentTypes, addMoneyPayMethods);
        trimSavedInstrumentTypes(savedInstrumentsTypes, merchantSavedInstruments);
        trimSavedInstrumentTypes(savedInstrumentsTypes, addMoneySavedInstruments);

        trimPayChannelOptions(merchantPayMethods, netbankingOptionSize);
        trimPayChannelOptions(addMoneyPayMethods, netbankingOptionSize);

        LOGGER.debug("Trimmed Response {}", cashierInfoResponse);
    }

    private void trimInstrumentTypes(List<InstrumentType> instrumentTypes, List<PayMethod> payMethods) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Set<String> instrumentTypeSet = instrumentTypeListToPayMethodStringSet(instrumentTypes);
        if (instrumentTypeSet == null || instrumentTypeSet.contains(InstrumentType.ALL.getPayMethod()))
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (!instrumentTypeSet.contains(payMethod.getPayMethod())) {
                payMethodIterator.remove();
            }
        }
    }

    private void trimSavedInstrumentTypes(List<InstrumentType> savedInstrumentTypes, SavedInstruments savedInstruments) {
        if (savedInstruments == null)
            return;
        Set<String> savedInstrumentTypeSet = instrumentTypeListToPayMethodStringSet(savedInstrumentTypes);
        if (savedInstrumentTypeSet == null || savedInstrumentTypeSet.contains(InstrumentType.ALL.getPayMethod()))
            return;
        List<SavedCard> savedCards = savedInstruments.getSavedCards();
        List<SavedVPA> savedVPAs = savedInstruments.getSavedVPAs();

        trimSavedCard(savedInstrumentTypeSet, savedCards);
        trimSavedVPA(savedInstrumentTypeSet, savedVPAs);
    }

    private void trimSavedCard(Set<String> savedInstrumentTypeSet, List<SavedCard> savedCards) {
        if (savedInstrumentTypeSet == null || savedCards == null || savedCards.isEmpty())
            return;
        Iterator<SavedCard> savedCardIterator = savedCards.iterator();
        while (savedCardIterator.hasNext()) {
            SavedCard savedCard = savedCardIterator.next();
            if (!savedInstrumentTypeSet.contains(savedCard.getPayMethod())) {
                savedCardIterator.remove();
            }
        }
    }

    private void trimSavedVPA(Set<String> savedInstrumentTypeSet, List<SavedVPA> savedVPAs) {
        if (savedInstrumentTypeSet == null || savedVPAs == null || savedVPAs.isEmpty())
            return;
        Iterator<SavedVPA> savedVPAIterator = savedVPAs.iterator();
        while (savedVPAIterator.hasNext()) {
            SavedVPA savedVPA = savedVPAIterator.next();
            if (!savedInstrumentTypeSet.contains(savedVPA.getPayMethod())) {
                savedVPAIterator.remove();
            }
        }
    }

    private void trimPayChannelOptions(List<PayMethod> payMethods, int netbankingOptionSize) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        for (PayMethod payMethod : payMethods) {
            if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                    || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                payMethod.setPayChannelOptions(Collections.emptyList());
            }
            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod()) && netbankingOptionSize > 0
                    && netbankingOptionSize <= payMethod.getPayChannelOptions().size()) {
                // TODO:Assuming it is sorted
                payMethod.setPayChannelOptions(payMethod.getPayChannelOptions().subList(0, netbankingOptionSize));
            }
        }

    }

    private Set<String> instrumentTypeListToPayMethodStringSet(List<InstrumentType> instrumentTypes) {
        Set<String> instrumentTypeSet = new HashSet<String>();
        if (instrumentTypes == null)
            return instrumentTypeSet;
        for (InstrumentType instrumentType : instrumentTypes) {
            instrumentTypeSet.add(instrumentType.getPayMethod());
        }
        return instrumentTypeSet;
    }

    private void filterDisabledSavedInstruments(SavedInstruments savedInstruments) {
        if (savedInstruments == null)
            return;
        if (savedInstruments.getSavedCards() != null && !savedInstruments.getSavedCards().isEmpty()) {
            Iterator<SavedCard> savedCardIterator = savedInstruments.getSavedCards().iterator();
            if (savedCardIterator.hasNext()) {
                SavedCard savedCard = savedCardIterator.next();
                if (savedCard != null && savedCard.getIsDisabled() != null
                        && Boolean.parseBoolean(savedCard.getIsDisabled().getStatus())) {
                    savedCardIterator.remove();
                }
            }

        }
        if (savedInstruments.getSavedVPAs() != null && !savedInstruments.getSavedCards().isEmpty()) {
            Iterator<SavedVPA> savedVPAIterator = savedInstruments.getSavedVPAs().iterator();
            if (savedVPAIterator.hasNext()) {
                SavedVPA savedVPA = savedVPAIterator.next();
                if (savedVPA != null && savedVPA.getIsDisabled() != null
                        && Boolean.parseBoolean(savedVPA.getIsDisabled().getStatus())) {
                    savedVPAIterator.remove();
                }
            }
        }

    }

    private void filterDisabledPayMethods(List<PayMethod> payMethods) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (payMethod.getPayChannelOptions() != null) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (payChannelBase.getIsDisabled() != null
                            && Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        payChannelBaseIterator.remove();
                    }
                }
            }
            if (payMethod.getPayChannelOptions() == null
                    || payMethod.getPayChannelOptions().isEmpty()
                    || (payMethod.getIsDisabled() != null && Boolean
                            .parseBoolean(payMethod.getIsDisabled().getStatus()))) {
                payMethodIterator.remove();
            }
        }
    }

    private boolean getIsDirectPayOption(SavedCard savedCard, List<PayMethod> payMethods) {
        PayMethod payMethod = payMethods.stream().filter(s -> s.getPayMethod().equals(savedCard.getPayMethod()))
                .findAny().orElse(null);
        if (payMethod != null) {

            BankCard bankCard = payMethod.getPayChannelOptions().stream().map(s -> (BankCard) s)
                    .filter(s -> s.getInstId().equals(savedCard.getInstId())).findAny().orElse(null);
            if (bankCard != null && !isEmpty(bankCard.getDirectServiceInsts())
                    && !isEmpty(bankCard.getSupportAtmPins())) {
                return theiaSessionDataService.isDirectChannelEnabled(savedCard.getIssuingBank(), savedCard
                        .getCardDetails().getCardType(), new HashSet<>(bankCard.getDirectServiceInsts()), true,
                        new HashSet<>(bankCard.getSupportAtmPins()));

            }
        }
        return false;
    }

    public void filterUpiPayOptionsInNative(
            List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods) {
        boolean upiPush = false, upiPushExpress = false;

        if (payMethods == null || payMethods.isEmpty()) {
            return;
        }
        com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod upiPayMethod = null;
        for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : payMethods) {
            if (EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod())) {
                upiPayMethod = payMethod;
                break;
            }
        }
        if (upiPayMethod != null) {
            // Hack to remove UPI_PUSH_EXPRESS as a separate pay channel if
            // configured
            for (com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase payChannel : upiPayMethod
                    .getPayChannelOptions()) {
                if (TheiaConstant.BasicPayOption.UPI_PUSH.equals(payChannel.getPayChannelOption())
                        && Boolean.FALSE.toString().equalsIgnoreCase(payChannel.getIsDisabled().getStatus())) {
                    upiPush = true;
                }
                if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayChannelOption())
                        && Boolean.FALSE.toString().equalsIgnoreCase(payChannel.getIsDisabled().getStatus())) {
                    upiPushExpress = true;
                }

            }

            if (!upiPush && upiPushExpress) {
                for (Iterator<com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase> it = upiPayMethod
                        .getPayChannelOptions().iterator(); it.hasNext();) {
                    com.paytm.pgplus.theia.nativ.model.payview.response.UPI payChannel = (com.paytm.pgplus.theia.nativ.model.payview.response.UPI) it
                            .next();
                    if (TheiaConstant.BasicPayOption.UPI_PUSH.equals(payChannel.getPayChannelOption())
                            && Boolean.TRUE.toString().equalsIgnoreCase(payChannel.getIsDisabled().getStatus())) {
                        payChannel.setIsDisabled(new com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo(
                                Boolean.FALSE.toString(), ""));
                    }

                    if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayChannelOption())) {
                        payChannel.setPayChannelOption("UPI_PUSH");
                        payChannel.setInstId("UPIPUSH");
                        payChannel.setInstName("Unified Payment Interface - PUSH");
                    }
                }
            }
            if (!upiPush && !upiPushExpress) {
                for (Iterator<com.paytm.pgplus.theia.nativ.model.payview.response.PayChannelBase> it = upiPayMethod
                        .getPayChannelOptions().iterator(); it.hasNext();) {
                    com.paytm.pgplus.theia.nativ.model.payview.response.UPI payChannel = (com.paytm.pgplus.theia.nativ.model.payview.response.UPI) it
                            .next();

                    if (TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS.equals(payChannel.getPayChannelOption())) {
                        payChannel.setPayChannelOption("UPI_PUSH");
                        payChannel.setInstId("UPIPUSH");
                        payChannel.setInstName("Unified Payment Interface - PUSH");
                    }
                }
            }
        }
    }

}
