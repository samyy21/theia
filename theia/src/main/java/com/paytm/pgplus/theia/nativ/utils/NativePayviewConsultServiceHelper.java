package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.NpciHealthUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.BankMasterDetails;
import com.paytm.pgplus.cache.model.MBIDLimitMappingDetails;
import com.paytm.pgplus.cache.model.MerchantBlockConfig;
import com.paytm.pgplus.cache.model.MerchantBlockFilters;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.merchantlimit.enums.LimitType;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitAccumulateVo;
import com.paytm.pgplus.facade.merchantlimit.models.response.LimitDetail;
import com.paytm.pgplus.facade.merchantlimit.models.response.MerchantLimitInfo;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.user.models.UpiBankAccountV4;
import com.paytm.pgplus.facade.user.models.VpaDetailV4;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatraV4;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoResponseData;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMbidLimitDataService;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantBlockFiltersService;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantBankInfoDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.ResponseCodeConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.helper.BizRequestResponseMapperHelper;
import com.paytm.pgplus.theia.enums.ELitePayViewDisabledReasonMsg;
import com.paytm.pgplus.theia.enums.PromoCodeType;
import com.paytm.pgplus.theia.merchant.models.BankInfoData;
import com.paytm.pgplus.theia.nativ.enums.EmiType;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.*;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.nativ.promo.IPromoHelper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.services.impl.CashierServiceHelper;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.helper.VPAHelper;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZEST;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.ZESTMONEY;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.MAX_DEFICIT_AMOUNT;

@Component("nativePayviewConsultServiceHelper")
public class NativePayviewConsultServiceHelper extends
        BasePayviewConsultServiceHelper<NativeCashierInfoRequest, NativeCashierInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayviewConsultServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativePayviewConsultServiceHelper.class);

    @Autowired
    @Qualifier("commonFacade")
    private ICommonFacade commonFacade;

    @Autowired
    @Qualifier("nativeCustomBeanMapper")
    private ICustomBeanMapper<NativeCashierInfoResponse> customBeanMapper;

    @Autowired
    @Qualifier("promoHelperImpl")
    private IPromoHelper nativePromoHelper;

    @Autowired
    @Qualifier("merchantBankInfoDataService")
    private IMerchantBankInfoDataService merchantBankInfoDataService;

    @Autowired
    @Qualifier("bankInfoDataServiceImpl")
    IBankInfoDataService bankInfoDataService;

    @Autowired
    private CashierServiceHelper cashierServiceHelper;

    @Autowired
    @Qualifier("mbidLimitDataServiceImpl")
    IMbidLimitDataService mbidLimitDataService;

    @Autowired
    @Qualifier("vpaHelper")
    VPAHelper vpaHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    protected WorkFlowHelper workFlowHelper;

    @Autowired
    private NpciHealthUtil npciHealthUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private IMerchantBlockFiltersService merchantBlockFiltersService;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("bizRequestResponseMapperHelper")
    private BizRequestResponseMapperHelper bizRequestResponseMapperHelper;

    @Override
    public NativeCashierInfoResponse transformResponse(WorkFlowResponseBean serviceResponse,
            CashierInfoRequest serviceRequest, NativeCashierInfoRequest request) {
        boolean disableWallet = isDisableWallet(request.getBody().getEnablePaymentMode(), request.getBody()
                .getDisablePaymentMode());
        NativeCashierInfoResponse response = customBeanMapper.getCashierInfoResponse(serviceResponse, serviceRequest,
                disableWallet);

        updateChannelIfBankPresentInEnablePayModes(request);
        updatePaymentFlow(response, request);
        filterDisablePayOptionResponse(response, request);
        trimResponse(response, request, serviceRequest, serviceResponse);
        updateConsultFee(serviceResponse, response);
        updateSarvatraVpa(serviceResponse, response.getBody(), request);
        filterSarvatraVpa(response.getBody(), serviceRequest);
        updateSavedCardsForAddNPay(serviceResponse, response);
        updateActiveSubscriptions(serviceResponse, response);
        updateEnabledBanksAndChannelInResponse(request, response);
        updateEmiPaymentModeBasedOnRequestedEmiType(request, response);
        updatePreAuthDetails(response, serviceResponse);
        filterAddNPayUpiPushChannel(response, request);

        if (ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_ADDNPAY_ENABLED, false)) {
            filterCCOnUPIDetail(response.getBody().getAddMoneyPayOption());
        }
        if (ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_SUBS_ENABLED, false)
                && ERequestType.isSubscriptionRequest(serviceRequest.getBody().getRequestType())) {
            filterCCOnUPIDetail(response.getBody().getMerchantPayOption());
        }
        if (ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_NATIVE_ADDMONEY_ENABLED, false)
                && (ERequestType.ADD_MONEY.equals(serviceRequest.getBody().getRequestType()) || checkOnNativeAddMoneyRequest(
                        response, request))) {
            filterCCOnUPIDetail(response.getBody().getMerchantPayOption());
        }

        setCCOnUPIPreferenceEnabledKey(serviceResponse, serviceRequest, response, request);

        if (ff4JUtils.isFeatureEnabled(ENABLE_ZEST_LOGIC, false)) {
            populateZestDataforPG2(response, serviceResponse, serviceRequest);
        }

        if (ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), FEATURE_ENABLE_PAYMODE_FILTER_ON_BOSS, false)) {
            filterPayModeOnBossLevel(serviceResponse, response);
        }

        if (ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), FEATURE_ENABLE_UPI_COLLECT_ON_ADDNPAY, false)) {
            try {
                filterUpiCollectChannel(response, request);
            } catch (Exception e) {
                LOGGER.error("Exception in filterUpiCollect function {}", e);
            }
        }

        if (StringUtils.isNotBlank(request.getBody().getSupportedPayModesForAddNPay())
                && ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(),
                        SUPPORT_ADD_MONEY_PAY_OPTION_IN_OFFLINE_FLOW, false)) {

            if (response != null && response.getBody() != null && response.getBody().getAddMoneyPayOption() != null) {
                List<PayChannelBase> savedInstrument = response.getBody().getAddMoneyPayOption().getSavedInstruments();
                EXT_LOGGER.info("SavedInstruments in AddMoney Pay-Option are : {}", savedInstrument);
                if (CollectionUtils.isNotEmpty(savedInstrument)) {
                    boolean IsSavedInstrumentAvailable = savedInstrument
                            .stream()
                            .anyMatch(
                                    s -> (s instanceof SavedCard)
                                            && ((SavedCard) s).getCardDetails() != null
                                            && (CREDIT_CARD.equals(((SavedCard) s).getCardDetails().getCardType()) || DEBIT_CARD
                                                    .equals(((SavedCard) s).getCardDetails().getCardType())));
                    response.getBody().setSavedCardsAvailable(IsSavedInstrumentAvailable);
                }
            }
            String[] paymodes = request.getBody().getSupportedPayModesForAddNPay().split(",");
            Boolean isPCFMerchant = (merchantPreferenceService.isPostConvenienceFeesEnabled(request.getBody().getMid())
                    || merchantPreferenceService.isDynamicFeeMerchant(request.getBody().getMid()) || merchantPreferenceService
                    .isSlabBasedMDREnabled(request.getBody().getMid()));
            if (!isPCFMerchant && !isMLVMerchant(request) && !isMLoyalMerchant(response)
                    && !isPostPaidEnabled(response, serviceResponse)) {
                trimAddMoneyPayOptionOffline(paymodes, response);
            } else {
                EXT_LOGGER
                        .info("AddMoney Pay-Option won't be returned as Merchant is not eligible for offline AddNPay.");
                response.getBody().setAddMoneyPayOption(null);
            }
        }

        if (serviceRequest.getBody().getSubscriptionTransactionRequestBody() != null
                && StringUtils.isNotBlank(serviceRequest.getBody().getSubscriptionTransactionRequestBody()
                        .getAccountNumber()))
            response.getBody().setCashierAccountNumber(
                    MaskingUtil.getMaskedBankAccountNumber(serviceRequest.getBody()
                            .getSubscriptionTransactionRequestBody().getAccountNumber(), 0, 4));

        return response;
    }

    private void setCCOnUPIPreferenceEnabledKey(WorkFlowResponseBean serviceResponse,
            CashierInfoRequest serviceRequest, NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        response.getBody().setCcOnUPIAllowed(
                checkCCOnUPIPreferenceEnabled(serviceResponse, serviceRequest, response, request));
    }

    private boolean checkCCOnUPIPreferenceEnabled(WorkFlowResponseBean serviceResponse,
            CashierInfoRequest serviceRequest, NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        if ((EPayMode.ADDANDPAY.equals(serviceResponse.getAllowedPayMode()) || (serviceRequest.getBody()
                .isFetchAddMoneyOptions() && merchantPreferenceService.convertTxnToAddNPayEnabled(request.getBody()
                .getMid(), false)))
                && ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_ADDNPAY_ENABLED, false)) {
            return false;
        }
        if ((ERequestType.ADD_MONEY.equals(serviceRequest.getBody().getRequestType()) || checkOnNativeAddMoneyRequest(
                response, request))
                && ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_NATIVE_ADDMONEY_ENABLED, false)) {
            return false;
        }
        if (ERequestType.isSubscriptionRequest(serviceRequest.getBody().getRequestType())
                && ff4JUtils.isFeatureEnabledOnMid(request.getBody().getMid(), UPI_CC_SUBS_ENABLED, false)) {
            return false;
        }
        if (bizRequestResponseMapperHelper.validateAndCheckCCOnUpi(request.getBody().getMid())) {
            return true;
        }
        return false;
    }

    private boolean checkOnNativeAddMoneyRequest(NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        try {
            if (request != null && request.getHead() != null && StringUtils.isNotBlank(request.getHead().getTxnToken())) {
                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(request
                        .getHead().getTxnToken());
                if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                    InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                    if (orderDetail != null && orderDetail.isNativeAddMoney()) {
                        return true;
                    } else {
                        return false;
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while checkOnNativeAddMoneyRequest");
        }

        return false;
    }

    private void filterCCOnUPIDetail(PayOption payOption) {
        try {
            if (null == payOption || null == payOption.getUpiProfileV4()
                    || null == payOption.getUpiProfileV4().getRespDetails()
                    || null == payOption.getUpiProfileV4().getRespDetails().getProfileDetail()
                    || null == payOption.getUpiProfileV4().getRespDetails().getProfileDetail().getBankAccounts()) {
                return;
            }

            List<UpiBankAccountV4> bankAccounts = payOption.getUpiProfileV4().getRespDetails().getProfileDetail()
                    .getBankAccounts();

            if (CollectionUtils.isNotEmpty(bankAccounts)) {
                Iterator<UpiBankAccountV4> accountIterator = bankAccounts.iterator();
                while (accountIterator.hasNext()) {
                    UpiBankAccountV4 bankAccount = accountIterator.next();
                    String account = StringUtils.stripStart(bankAccount.getAccountType(), "0");
                    if (CREDIT.equalsIgnoreCase(account)) {
                        accountIterator.remove();
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("Exception while filtering CC ON UPI");
        }
    }

    private void filterAddNPayUpiPushChannel(NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        if (request != null && request.getHead() != null && StringUtils.isNotBlank(request.getHead().getTxnToken())) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(request.getHead()
                    .getTxnToken());
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                List<PayMethod> addMoneyPayMethods = response.getBody().getAddMoneyPayOption().getPayMethods();
                boolean conditionToRemoveUpiPushExpressChannel = orderDetail != null
                        && !(orderDetail.isAppInvoke() && request.getBody().isAddNPayOnUPIPushSupported());
                if (conditionToRemoveUpiPushExpressChannel && addMoneyPayMethods != null) {
                    addMoneyPayMethods
                            .stream()
                            .filter(payMethod -> EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod()))
                            .forEach(
                                    payMethod -> {
                                        payMethod.getPayChannelOptions()
                                                .removeIf(
                                                        payChannelBase -> payChannelBase instanceof Bank
                                                                && UPI_PUSH_EXPRESS.equals(((Bank) payChannelBase)
                                                                        .getInstId()));
                                    });
                }
            }
        }
    }

    private void updatePreAuthDetails(NativeCashierInfoResponse response, WorkFlowResponseBean serviceResponse) {
        if (serviceResponse.getWorkFlowRequestBean().isPreAuth()) {
            if (null != response.getBody().getPreAuthDetails()
                    && (null != response.getBody().getPreAuthDetails().getPreAuthType())) {
                response.getBody().getPreAuthDetails().setPreAuth(true);
            } else {
                response.getBody().setPreAuthDetails(new PreAuthDetails(true));
            }
        }
    }

    private void updateEmiPaymentModeBasedOnRequestedEmiType(NativeCashierInfoRequest request,
            NativeCashierInfoResponse response) {
        List<PaymentMode> enablePayModes = request.getBody().getEnablePaymentMode();

        if (CollectionUtils.isNotEmpty(enablePayModes)) {

            PaymentMode emiInRequest = request.getBody().getEnablePaymentMode().stream()
                    .filter(payMethod -> EPayMethod.EMI.getMethod().equals(payMethod.getMode())).findAny().orElse(null);

            boolean isEmiOnlyPayModeRequested = (emiInRequest != null && request.getBody().getEnablePaymentMode()
                    .size() == 1);

            if (emiInRequest != null) {

                PayMethod emiInResponse = response.getBody().getMerchantPayOption().getPayMethods().stream()
                        .filter(payMethod -> EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())).findAny()
                        .orElse(null);

                String requestedEmiType = emiInRequest.getEmiType();

                if (StringUtils.isNotBlank(requestedEmiType) && emiInResponse != null) {

                    Iterator<PayChannelBase> iterator = emiInResponse.getPayChannelOptions().listIterator();

                    while (iterator.hasNext()) {
                        EmiChannel emiChannel = (EmiChannel) iterator.next();
                        if (!StringUtils.equals(requestedEmiType, emiChannel.getEmiType().getType())) {
                            LOGGER.info("removing emi channel, requestedEmiType:{}, emi channel:{}", requestedEmiType,
                                    emiChannel.getEmiType().getType());
                            iterator.remove();
                        }
                    }

                    updateSavedCardsBasedOnRequestedEmiType(response.getBody().getMerchantPayOption()
                            .getSavedInstruments(), requestedEmiType, isEmiOnlyPayModeRequested);
                }

            }
        }
    }

    private void updateSavedCardsBasedOnRequestedEmiType(List<PayChannelBase> savedCards, String requestedEmiType,
            boolean isEmiOnlyPayModeRequested) {
        if (CollectionUtils.isEmpty(savedCards)) {
            return;
        }

        Iterator<PayChannelBase> iterator = savedCards.listIterator();
        while (iterator.hasNext()) {
            SavedCard savedCard = (SavedCard) iterator.next();
            if (!StringUtils.equals(requestedEmiType, savedCard.getCardDetails().getCardType())) {
                if (isEmiOnlyPayModeRequested) {
                    LOGGER.info(
                            "requestedEmiType:{}, cardType:{}, removing cardId:{} as it does not match requested cardType",
                            requestedEmiType, savedCard.getCardDetails().getCardType(), savedCard.getCardDetails()
                                    .getCardId());
                    iterator.remove();
                } else {
                    LOGGER.info("making IsEmiAvailable false, requestedEmiType:{}, cardType:{}", requestedEmiType,
                            savedCard.getCardDetails().getCardType());
                    savedCard.setIsEmiAvailable(false);
                }
            }
        }
    }

    /**
     * Set active subscriptions of user in response.
     *
     * @param serviceResponse
     * @param response
     */
    private void updateActiveSubscriptions(WorkFlowResponseBean serviceResponse, NativeCashierInfoResponse response) {
        if (serviceResponse.getActiveSubscriptions() != null) {
            response.getBody().getMerchantPayOption().setActiveSubscriptions(serviceResponse.getActiveSubscriptions());
        }
    }

    public void populateZestData(NativeCashierInfoResponse response) {

        EmiChannel zestMoneyData = new EmiChannel();

        if (response != null && response.getBody() != null && response.getBody().getMerchantPayOption() != null
                && !CollectionUtils.isEmpty(response.getBody().getMerchantPayOption().getPayMethods())) {

            PayMethod payMode = response.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(payMethod -> EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())).findAny()
                    .orElse(null);

            if (payMode != null && !payMode.getPayChannelOptions().isEmpty()) {
                for (PayChannelBase payChannelBase : payMode.getPayChannelOptions()) {
                    Bank bank = (Bank) payChannelBase;
                    if (ZEST.equals(bank.getInstId())) {
                        zestMoneyData.setInstId(ZEST);
                        zestMoneyData.setInstName(ZESTMONEY);
                        zestMoneyData.setIconUrl(bank.getIconUrl());
                        zestMoneyData.setEmiType(EmiType.NBFC);
                        zestMoneyData.setHybridDisabled(bank.isHybridDisabled());
                        zestMoneyData.setMinAmount(new Money(ConfigurationUtil.getProperty(ZEST_MONEY_MINAMOUNT_TEXT)));
                        zestMoneyData.setMaxAmount(new Money(ConfigurationUtil.getProperty(ZEST_MONEY_MAXAMOUNT_TEXT)));
                        zestMoneyData.setIsDisabled(new StatusInfo(Boolean.FALSE.toString(), ""));
                        LOGGER.info("Adding Zest to EMI list");
                        PayChannelBase emiChannel = zestMoneyData;

                        for (PayMethod payMethodIterator : response.getBody().getMerchantPayOption().getPayMethods()) {

                            if (payMethodIterator.getPayMethod().equals(EPayMethod.EMI.toString())) {

                                payMethodIterator.getPayChannelOptions().add(emiChannel);
                                break;
                            }

                        }
                        break;
                    }
                }
            }
        }

    }

    /*
     * This method updates savedCards, removed isEmiAvailable flag from them
     */
    private void updateSavedCardsForAddNPay(WorkFlowResponseBean workFlowResponseBean,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        if (nativeCashierInfoResponse != null && nativeCashierInfoResponse.getBody() != null) {
            if (EPayMode.ADDANDPAY.getValue().equals(nativeCashierInfoResponse.getBody().getPaymentFlow().getValue())) {
                List<PayChannelBase> savedCards = nativeCashierInfoResponse.getBody().getAddMoneyPayOption()
                        .getSavedInstruments();

                if (savedCards != null) {
                    for (PayChannelBase savedCard : savedCards) {
                        if (savedCard instanceof SavedCard && ((SavedCard) savedCard).getIsEmiAvailable()) {
                            ((SavedCard) savedCard).setIsEmiAvailable(false);
                            ((SavedCard) savedCard).setMinAmount(null);
                            ((SavedCard) savedCard).setMaxAmount(null);
                        }
                    }
                }
            }
        }
    }

    private void updateEnabledBanksAndChannelInResponse(NativeCashierInfoRequest request,
            NativeCashierInfoResponse response) {

        List<PaymentMode> enabledPaymentModes = request.getBody().getEnablePaymentMode();
        if (CollectionUtils.isEmpty(enabledPaymentModes)) {
            return;
        }

        List<String> enabledCreditCardBanks = null;
        List<String> enabledDebitCardBanks = null;

        List<String> enabledEmiBanks = null;
        List<String> enableEmiChannels = null;

        List<String> enabledNetBankingBanks = null;

        for (PaymentMode pm : enabledPaymentModes) {
            if (EPayMethod.CREDIT_CARD.getMethod().equals(pm.getMode())) {
                enabledCreditCardBanks = pm.getBanks();
            } else if (EPayMethod.DEBIT_CARD.getMethod().equals(pm.getMode())) {
                enabledDebitCardBanks = pm.getBanks();
            } else if (EPayMethod.EMI.getMethod().equals(pm.getMode())) {
                enabledEmiBanks = pm.getBanks();
                enableEmiChannels = pm.getChannels();
            } else if (EPayMethod.NET_BANKING.getMethod().equals(pm.getMode())) {
                enabledNetBankingBanks = pm.getBanks();

            }
        }

        List<PayMethod> merchantPayMethods = response.getBody().getMerchantPayOption().getPayMethods();
        if (CollectionUtils.isNotEmpty(merchantPayMethods)) {
            for (PayMethod paymentMethod : merchantPayMethods) {
                if (EPayMethod.CREDIT_CARD.getMethod().equals(paymentMethod.getPayMethod())) {
                    paymentMethod.setEnabledBanks(enabledCreditCardBanks);
                } else if (EPayMethod.DEBIT_CARD.getMethod().equals(paymentMethod.getPayMethod())) {
                    paymentMethod.setEnabledBanks(enabledDebitCardBanks);
                } else if (EPayMethod.EMI.getMethod().equals(paymentMethod.getPayMethod())) {
                    paymentMethod.setEnabledBanks(enabledEmiBanks);
                    paymentMethod.setEnabledPayChannels(enableEmiChannels);
                } else if (EPayMethod.NET_BANKING.getMethod().equals(paymentMethod.getPayMethod())) {
                    paymentMethod.setEnabledBanks(enabledNetBankingBanks);
                }
            }
        }
    }

    private void updateChannelIfBankPresentInEnablePayModes(NativeCashierInfoRequest request) {
        /*
         * PGP-14015 We are doing this because this was requirement by the promo
         * team, banks which was coming in channels would come in "banks" now
         */
        List<PaymentMode> enablePayModes = request.getBody().getEnablePaymentMode();
        if (CollectionUtils.isNotEmpty(enablePayModes)) {
            for (PaymentMode paymentMode : enablePayModes) {
                if (EPayMethod.NET_BANKING.getMethod().equals(paymentMode.getMode())) {
                    if (CollectionUtils.isNotEmpty(paymentMode.getBanks())) {
                        paymentMode.setChannels(paymentMode.getBanks());
                    }
                }
                if (EPayMethod.EMI.getMethod().equals(paymentMode.getMode())) {
                    if (CollectionUtils.isNotEmpty(paymentMode.getChannels())) {
                        CardScheme cardScheme = CardScheme.getCardSchemebyName(paymentMode.getChannels().get(0));
                        if (cardScheme == null) {
                            paymentMode.setBanks(paymentMode.getChannels());
                            paymentMode.setChannels(null);
                        }
                    }
                }
            }
        }
    }

    private void updateSarvatraVpa(WorkFlowResponseBean workFlowResponseBean,
            NativeCashierInfoResponseBody nativeCashierInfoResponseBody, NativeCashierInfoRequest request) {

        if (null == nativeCashierInfoResponseBody) {
            return;
        }

        if (!isVersionAllowed(request) && workFlowResponseBean.getSarvatraUserProfile() == null) {
            return;
        }

        if (isVersionAllowed(request) && workFlowResponseBean.getUpiProfileV4() != null) {
            setBankLogoUrl(workFlowResponseBean.getUpiProfileV4());
        }

        List<PayMethod> merchantPayOption = null;
        if (checkPayOptionListNotEmpty(nativeCashierInfoResponseBody.getMerchantPayOption())) {
            merchantPayOption = nativeCashierInfoResponseBody.getMerchantPayOption().getPayMethods();
        }

        List<PayMethod> addMoneyPayOption = null;
        if (checkPayOptionListNotEmpty(nativeCashierInfoResponseBody.getAddMoneyPayOption())) {
            addMoneyPayOption = nativeCashierInfoResponseBody.getAddMoneyPayOption().getPayMethods();
        }

        if ((!isUPIPresentInPayOption(merchantPayOption) && !isUPIPresentInPayOption(addMoneyPayOption))) {
            return;
        }

        if (!isVersionAllowed(request) && workFlowResponseBean.getSarvatraUserProfile() != null) {
            // populate multi-bank-accounts in vpa-detail
            vpaHelper.populateVPALinkedBankAccounts(workFlowResponseBean.getSarvatraUserProfile());
        }

        boolean isEnhancedCashierPageRenderRequest = false;
        if (workFlowResponseBean.getExtendedInfo() != null
                && StringUtils.equals("true", workFlowResponseBean.getExtendedInfo().get("isEnhancedNative"))) {
            isEnhancedCashierPageRenderRequest = true;
        }
        boolean isSavedVpaCheckOverride = workFlowHelper.isSavedVpaCheckOverride(request.getBody().getAppVersion());

        if ((isUPIPresentInPayOption(merchantPayOption))) {
            // set user-profile-sarvatra for merchant
            if (isVersionAllowed(request) || workFlowResponseBean.isMerchantUpiPushExpressEnabled()
                    || isEnhancedCashierPageRenderRequest || isSavedVpaCheckOverride) {
                if (!isVersionAllowed(request)) {
                    nativeCashierInfoResponseBody.getMerchantPayOption().setUserProfileSarvatra(
                            workFlowResponseBean.getSarvatraUserProfile());
                } else if (workFlowResponseBean.getUpiProfileV4() != null) {
                    nativeCashierInfoResponseBody.getMerchantPayOption().setUpiProfileV4(
                            workFlowResponseBean.getUpiProfileV4());

                }
            }
        }
        if (isUPIPresentInPayOption(addMoneyPayOption)) {
            // set user-profile-sarvatra for add-money-merchant
            if (EPayMode.ADDANDPAY.equals(workFlowResponseBean.getAllowedPayMode())
                    && ((isVersionAllowed(request)) || workFlowResponseBean.isAddUpiPushExpressEnabled()
                            || isEnhancedCashierPageRenderRequest || isSavedVpaCheckOverride)
                    || isEnhancedFlow(workFlowResponseBean)) {
                if (!isVersionAllowed(request)) {
                    nativeCashierInfoResponseBody.getAddMoneyPayOption().setUserProfileSarvatra(
                            SerializationUtils.clone(workFlowResponseBean.getSarvatraUserProfile()));
                } else if (workFlowResponseBean.getUpiProfileV4() != null) {
                    nativeCashierInfoResponseBody.getAddMoneyPayOption().setUpiProfileV4(
                            SerializationUtils.clone(workFlowResponseBean.getUpiProfileV4()));

                }
            }
        }
    }

    private void setBankLogoUrl(UserProfileSarvatraV4 upiProfileV4) {
        if (upiProfileV4 == null || upiProfileV4.getRespDetails() == null
                || upiProfileV4.getRespDetails().getProfileDetail() == null
                || upiProfileV4.getRespDetails().getProfileDetail().getBankAccounts() == null) {
            return;
        }
        List<UpiBankAccountV4> upiBankAccountsV4 = upiProfileV4.getRespDetails().getProfileDetail().getBankAccounts();

        for (UpiBankAccountV4 upiBankAccountV4 : upiBankAccountsV4) {

            if (null != upiBankAccountV4) {
                if (CREDIT.equalsIgnoreCase(upiBankAccountV4.getAccountType())) {
                    upiBankAccountV4.setBankLogoUrl(upiBankAccountV4.getLogoUrl());
                } else {
                    upiBankAccountV4.setBankLogoUrl(commonFacade.getBankLogo(upiBankAccountV4.getPgBankCode()));
                }
            }

        }
    }

    private boolean isVersionAllowed(NativeCashierInfoRequest request) {
        if (request != null && request.getHead() != null) {
            String allowedVersions[] = com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS)
                    .split(",");
            return Arrays.stream(allowedVersions).anyMatch(
                    n -> StringUtils.equalsIgnoreCase(request.getHead().getVersion(), n));
        } else
            return false;
    }

    private boolean isEnhancedFlow(WorkFlowResponseBean workFlowResponseBean) {
        if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null) {
            return workFlowResponseBean.getWorkFlowRequestBean().isEnhancedCashierPageRequest();
        } else
            return false;
    }

    private boolean checkPayOptionListNotEmpty(PayOption payOption) {
        return null != payOption && null != payOption.getPayMethods() && !payOption.getPayMethods().isEmpty();
    }

    private boolean isUPIPresentInPayOption(List<PayMethod> payOption) {
        return null != payOption
                && !payOption.isEmpty()
                && payOption.stream().filter(o -> o.getPayMethod().equals(EPayMethod.UPI.getMethod())).findFirst()
                        .isPresent();
    }

    private void filterDisablePayOptionResponse(NativeCashierInfoResponse cashierInfoResponse,
            NativeCashierInfoRequest request) {
        filterDisabledPayMethods(cashierInfoResponse, request);
        filterDisabledSavedInstruments(cashierInfoResponse, request);
    }

    private void trimResponse(NativeCashierInfoResponse cashierInfoResponse, NativeCashierInfoRequest request,
            CashierInfoRequest cashierInfoRequest, WorkFlowResponseBean serviceResponse) {
        LOGGER.debug("Trimming Response ...");
        if (cashierInfoResponse == null || cashierInfoResponse.getBody() == null)
            return;
        String emiOption = cashierInfoRequest.getBody().getEmiOption();

        NativeCashierInfoResponseBody responseBody = cashierInfoResponse.getBody();

        List<PayMethod> merchantPayMethods = responseBody.getMerchantPayOption().getPayMethods();
        List<PayChannelBase> merchantSavedInstruments = responseBody.getMerchantPayOption().getSavedInstruments();

        List<PayMethod> addMoneyPayMethods = null;
        List<PayChannelBase> addMoneySavedInstruments = null;
        if (responseBody.getAddMoneyPayOption() != null) {
            addMoneyPayMethods = responseBody.getAddMoneyPayOption().getPayMethods();
            addMoneySavedInstruments = responseBody.getAddMoneyPayOption().getSavedInstruments();
        }

        // Check if emiOption starts with 0costEmi
        trimForZeroCostEmi(request, emiOption, cashierInfoResponse);
        trimDisablePayChannels(merchantPayMethods, request.getBody().getDisablePaymentMode());
        trimEnablePayChannels(merchantPayMethods, request.getBody().getEnablePaymentMode());

        // Check for Restricted | Discount promo pay methods and filter if
        // restricted promo.
        if (!StringUtils.isEmpty(cashierInfoRequest.getBody().getPromoCode())) {
            PromoCodeResponse promoCodeResponse = nativePromoHelper.validatePromoCode(cashierInfoRequest.getBody()
                    .getPromoCode(), cashierInfoRequest.getHead().getMid());
            NativePromoCodeData nativePromoCodeData = new NativePromoCodeData();
            nativePromoCodeData.setPromoCode(cashierInfoRequest.getBody().getPromoCode());
            // Check if promo valid and whether its a restricted promo and then
            // only filter paymethods
            final String promoCodeTypeName = (null == promoCodeResponse)
                    || (null == promoCodeResponse.getPromoCodeDetail()) ? null : promoCodeResponse.getPromoCodeDetail()
                    .getPromocodeTypeName();
            nativePromoCodeData.setPromoCodeTypeName(promoCodeTypeName);
            if (promoCodeResponse != null && promoCodeResponse.getPromoResponseCode() != null
                    && promoCodeResponse.getPromoCodeDetail() != null) {
                nativePromoCodeData.setPromoCodeValid(ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse
                        .getPromoResponseCode()));
                nativePromoCodeData.setPromoMsg(promoCodeResponse.getPromoCodeDetail().getPromoMsg());
                nativePromoCodeData.setPromoCodeMsg(promoCodeResponse.getResultMsg());
            }
            cashierInfoResponse.getBody().setPromoCodeData(nativePromoCodeData);

            // For restricted promo only: In case Promocode is present and
            // Hybrid payment flow called: Remove
            // Hybrid and make NONE
            if (null != promoCodeTypeName && EPayMode.HYBRID.equals(cashierInfoResponse.getBody().getPaymentFlow())) {
                cashierInfoResponse.getBody().setPaymentFlow(EPayMode.NONE);
            }
            filterPromoPayMethods(promoCodeResponse, promoCodeTypeName, merchantPayMethods, addMoneyPayMethods);
        }

        updateEmiAvailableOnSavedInstruments(merchantSavedInstruments, merchantPayMethods, request.getBody()
                .getEnablePaymentMode());
        updateEmiAvailableOnSavedInstruments(addMoneySavedInstruments, addMoneyPayMethods, null);

        trimSavedInstrumentTypesByAllowedPayMethod(merchantSavedInstruments, merchantPayMethods);
        trimSavedInstrumentTypesByAllowedPayMethod(addMoneySavedInstruments, addMoneyPayMethods);

        trimSavedCardsIfNotInIssuingBankList(merchantSavedInstruments, request);

        trimForAdvanceDeposit(request, cashierInfoResponse, serviceResponse);

        LOGGER.debug("Trimming Response Done");
        LOGGER.debug("Trimmed Response {}", cashierInfoResponse);
    }

    private void trimSavedCardsIfNotInIssuingBankList(List<PayChannelBase> merchantSavedInstruments,
            NativeCashierInfoRequest request) {

        List<PaymentMode> enabledPayModes = request.getBody().getEnablePaymentMode();
        if (CollectionUtils.isEmpty(merchantSavedInstruments) || CollectionUtils.isEmpty(enabledPayModes)) {
            return;
        }

        for (PaymentMode p : enabledPayModes) {
            Iterator<PayChannelBase> i = merchantSavedInstruments.iterator();
            while (i.hasNext()) {
                PayChannelBase pm = i.next();
                if (pm.getPayMethod().equals(p.getMode())) {
                    if (CollectionUtils.isNotEmpty(p.getBanks())
                            && !p.getBanks().contains(((SavedCard) pm).getIssuingBank())) {
                        i.remove();
                    }
                }
            }
        }
    }

    private void trimForZeroCostEmi(NativeCashierInfoRequest request, String emiOption,
            NativeCashierInfoResponse cashierInfoResponse) {

        if (StringUtils.startsWithIgnoreCase(emiOption, TheiaConstant.ExtraConstants.ZERO_COST_EMI)) {
            cashierInfoResponse.getBody().setZeroCostEmi(true);
            PayMethod payMethod = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(s -> EPayMethod.EMI.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
            String emiType = StringUtils.substringAfter(emiOption, ":");
            String[] emiBankDetails = fetchEmiBankDetailsFromEmiOptions(emiOption, emiType);
            EmiType type = StringUtils.contains(emiType, TheiaConstant.ExtraConstants.DC) ? EmiType.DEBIT_CARD
                    : EmiType.CREDIT_CARD;
            final String bank = emiBankDetails[0];
            final String planId = emiBankDetails[1];
            // fetching bank and plan info from mapping service
            BankMasterDetails bankMasterDetails = getBankMasterDetails(bank);
            MBIDLimitMappingDetails mbidLimitMappingDetails = getPlanDetails(planId);

            if (!filterEMI(bankMasterDetails.getBankCode(), mbidLimitMappingDetails, payMethod, type)) {
                LOGGER.info("For zeroCostEmi Invalid Payment details entered");
                throw RequestValidationException.getException();
            }

            List<PaymentMode> disablePaymentModes = request.getBody().getDisablePaymentMode();
            if (disablePaymentModes == null) {
                disablePaymentModes = new ArrayList<>();
            }

            disablePaymentModes.add(new PaymentMode(EPayMethod.BALANCE.toString()));
            disablePaymentModes.add(new PaymentMode(EPayMethod.PPBL.toString()));
            request.getBody().setDisablePaymentMode(disablePaymentModes);
            List<PaymentMode> enablePaymentModes = new ArrayList<>();
            PaymentMode paymentMode = new PaymentMode();
            List<String> channels = new ArrayList<>();
            channels.add(bankMasterDetails.getBankCode());
            paymentMode.setMode(EPayMethod.EMI.toString());
            paymentMode.setChannels(channels);
            enablePaymentModes.add(paymentMode);
            request.getBody().setEnablePaymentMode(enablePaymentModes);
        }
    }

    private void trimDisablePayChannels(List<PayMethod> payMethods, List<PaymentMode> paymentModes) {
        if (null != paymentModes && !paymentModes.isEmpty()) {
            Iterator<PayMethod> payMethodIterator = payMethods.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                PaymentMode paymentMode = getPaymentMode(paymentModes, payMethod);
                if (null != paymentMode && (null == paymentMode.getChannels() || paymentMode.getChannels().isEmpty())) {
                    payMethodIterator.remove();
                } else if (null != paymentMode) {
                    Iterator<PayChannelBase> payChannelIterator = payMethod.getPayChannelOptions().iterator();
                    while (payChannelIterator.hasNext()) {
                        PayChannelBase payChannelBase = payChannelIterator.next();
                        if (payChannelBase instanceof Bank) {
                            if (paymentMode.getChannels().contains(((Bank) payChannelBase).getInstId())) {
                                payChannelIterator.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private PaymentMode getPaymentMode(List<PaymentMode> paymentModes, PayMethod payMethod) {
        for (PaymentMode paymentMode : paymentModes) {
            if (payMethod.getPayMethod().equals(paymentMode.getMode())) {
                return paymentMode;
            }
        }
        return null;
    }

    private PayMethod getPaymentModeFromList(List<PayMethod> payMethods, String paymentMode) {
        for (PayMethod paymentModeFromList : payMethods) {
            if (StringUtils.equals(paymentMode, paymentModeFromList.getPayMethod())) {
                return paymentModeFromList;
            }
        }
        return null;
    }

    @Override
    public void setMerchantLimit(WorkFlowResponseBean workFlowResponseBean,
            NativeCashierInfoResponse nativeCashierInfoResponse) {
        MerchantLimitDetail merchantLimitDetail = new MerchantLimitDetail();
        List<MerchantRemainingLimit> merchantRemainingLimitList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(workFlowResponseBean.getMerchantLimitInfos())
                && CollectionUtils.isNotEmpty(workFlowResponseBean.getLimitAccumulateVos())) {

            for (MerchantLimitInfo merchantLimitInfo : workFlowResponseBean.getMerchantLimitInfos()) {
                MerchantRemainingLimit merchantRemainingLimit = new MerchantRemainingLimit();
                LimitType limitType = merchantLimitInfo.getLimitType();
                com.paytm.pgplus.facade.common.model.Money limitAmount = merchantLimitInfo.getAmountThreshold();

                if (limitAmount == null || StringUtils.equals("-1", limitAmount.getAmount())) {
                    merchantRemainingLimit.setAmount(null);
                } else {
                    Double remainingAmount = Double.parseDouble(limitAmount.getAmount());
                    LimitAccumulateVo limitAccumulateVo = workFlowResponseBean.getLimitAccumulateVos().stream()
                            .filter(a -> a.getLimitType().name().equals(limitType.name())).collect(Collectors.toList())
                            .get(0);
                    if (limitAccumulateVo != null && limitAccumulateVo.getAmount() != null
                            && !limitAccumulateVo.getAmount().getAmount().equals("-1")) {
                        remainingAmount = remainingAmount
                                - Double.parseDouble(limitAccumulateVo.getAmount().getAmount());
                    }

                    String amountInRupees = formatAmount(remainingAmount);
                    merchantRemainingLimit.setAmount(amountInRupees);
                }
                merchantRemainingLimit.setLimitType(limitType);
                merchantRemainingLimitList.add(merchantRemainingLimit);
            }
        } else if (CollectionUtils.isNotEmpty(workFlowResponseBean.getLimitDetails())) {
            for (LimitDetail limitDetail : workFlowResponseBean.getLimitDetails()) {
                if (limitDetail != null && limitDetail.getFrequency() != null) {
                    MerchantRemainingLimit merchantRemainingLimit = new MerchantRemainingLimit();
                    merchantRemainingLimit.setLimitType(limitDetail.getFrequency());

                    if (limitDetail.getMaxLimit() == null || StringUtils.equals("-1", limitDetail.getMaxLimit()))
                        merchantRemainingLimit.setAmount(null);
                    else {
                        try {
                            Double maxLimit = Double.parseDouble(limitDetail.getMaxLimit());
                            Double accumulatedLimit = Double.parseDouble(limitDetail.getAccumulatedValue());
                            Double remainingAmount = maxLimit - accumulatedLimit;
                            String amountInRupees = formatAmount(remainingAmount);
                            merchantRemainingLimit.setAmount(amountInRupees);
                        } catch (Exception e) {
                            LOGGER.error("Error while parsing amount", e);
                        }
                    }
                    merchantRemainingLimitList.add(merchantRemainingLimit);
                }
            }
            populateRemainingLimit(merchantLimitDetail, merchantRemainingLimitList);
        } else if (CollectionUtils.isNotEmpty(workFlowResponseBean.getMerchnatLiteViewResponse()
                .getMerchantRemainingLimits())) {
            for (com.paytm.pgplus.facade.payment.models.response.MerchantRemainingLimit merchantRemainingLimit : workFlowResponseBean
                    .getMerchnatLiteViewResponse().getMerchantRemainingLimits()) {
                if (merchantRemainingLimit != null && merchantRemainingLimit.getLimitType() != null) {
                    LimitType limitType = merchantRemainingLimit.getLimitType();
                    if (limitType == LimitType.REMAINING_LIMIT) {
                        LOGGER.info("Setting Overall Limit in separate field : {}", merchantRemainingLimit.getAmount());
                        merchantLimitDetail.setRemainingLimit(merchantRemainingLimit.getAmount());
                    } else {
                        MerchantRemainingLimit merchantRemainingLimit1 = new MerchantRemainingLimit();
                        merchantRemainingLimit1.setLimitType(merchantRemainingLimit.getLimitType());
                        merchantRemainingLimit1.setAmount(merchantRemainingLimit.getAmount());
                        if (ff4JUtils.isFeatureEnabled(THEIA_ENABLE_LIMIT_CONVERSION, false)) {
                            if (StringUtils.isNotBlank(merchantRemainingLimit.getAmount())) {
                                try {
                                    Double remainingAmount = Double.parseDouble(merchantRemainingLimit.getAmount());
                                    String amountInRupees = formatAmount(remainingAmount);
                                    merchantRemainingLimit1.setAmount(amountInRupees);
                                } catch (Exception e) {
                                    LOGGER.error("Error in parsing limit amount");
                                }
                            }
                        }
                        merchantRemainingLimitList.add(merchantRemainingLimit1);
                    }
                }
            }
        }
        merchantLimitDetail.setMerchantLimitType(workFlowResponseBean.getMerchantLimitType());
        merchantLimitDetail.setMerchantPaymodesLimits(workFlowResponseBean.getmerchantPaymodesLimits());
        merchantLimitDetail.setMerchantRemainingLimits(merchantRemainingLimitList);
        nativeCashierInfoResponse.getBody().setMerchantLimitDetail(merchantLimitDetail);

        setMerchantLimitExcludedPaymodesAndMessage(nativeCashierInfoResponse);
    }

    private String formatAmount(Double remainingAmount) {
        String merchantLimitAmount = remainingAmount <= 0 ? "0" : String.valueOf(remainingAmount);
        String amountInRupees = AmountUtils.getTransactionAmountInRupee(merchantLimitAmount);
        if (StringUtils.isNotBlank(amountInRupees) && '.' == amountInRupees.charAt(0)) {
            amountInRupees = "0" + amountInRupees;
        }
        return amountInRupees;
    }

    private void populateRemainingLimit(MerchantLimitDetail limitDetail, List<MerchantRemainingLimit> remainingLimits) {
        Double maxAmount = Double.MAX_VALUE;
        Double currentAmount = 0d;
        String remainingAmount = null;
        if (CollectionUtils.isNotEmpty(remainingLimits)) {
            for (MerchantRemainingLimit limit : remainingLimits) {
                if (StringUtils.isNotBlank(limit.getAmount())) {
                    currentAmount = Double.parseDouble(limit.getAmount());
                    if (currentAmount < maxAmount) {
                        maxAmount = currentAmount;
                        remainingAmount = limit.getAmount();
                    }
                }
            }
            limitDetail.setRemainingLimit(remainingAmount);
            LOGGER.info("Computing Remaining Limit for overall merchant limit");
        }
    }

    private void setMerchantLimitExcludedPaymodesAndMessage(NativeCashierInfoResponse nativeCashierInfoResponse) {

        String merchantLimitMsg = ConfigurationUtil.getProperty("merchant.limit.msg", "");
        if (StringUtils.isNotBlank(merchantLimitMsg)) {
            nativeCashierInfoResponse.getBody().getMerchantLimitDetail().setMessage(merchantLimitMsg);
        }
        List<EPayMethod> excludedPayModes = new ArrayList<>();
        String excludedPayMode = ConfigurationUtil.getProperty("merchant.limit.excluded.paymodes", "");
        if (StringUtils.isBlank(excludedPayMode)) {
            return;
        }
        String[] excludedPaymodeList = excludedPayMode.split(Pattern.quote(","));
        for (String paymode : excludedPaymodeList) {
            excludedPayModes.add(EPayMethod.getPayMethodByMethod(paymode.trim()));
        }
        nativeCashierInfoResponse.getBody().getMerchantLimitDetail().setExcludedPaymodes(excludedPayModes);
    }

    private void trimEnablePayChannels(List<PayMethod> merchantPayMethods, List<PaymentMode> toBeFilteredPayMethods) {
        if (null != toBeFilteredPayMethods && !toBeFilteredPayMethods.isEmpty()) {
            Iterator<PayMethod> payMethodIterator = merchantPayMethods.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                PaymentMode paymentMode = getPaymentMode(toBeFilteredPayMethods, payMethod);
                if (null == paymentMode) {
                    payMethodIterator.remove();
                } else {
                    if (paymentMode.getChannels() != null
                            && !(paymentMode.getChannels().isEmpty())
                            || (EPayMethod.EMI.getMethod().equals(paymentMode.getMode())
                                    && paymentMode.getBanks() != null && paymentMode.getBanks().size() >= 1)) {
                        Iterator<PayChannelBase> payChannelIterator = payMethod.getPayChannelOptions().iterator();
                        while (payChannelIterator.hasNext()) {
                            PayChannelBase payChannelBase = payChannelIterator.next();
                            if (payChannelBase instanceof Bank) {
                                if (payMethod.getPayMethod().equals(EPayMethod.EMI.getMethod())) {
                                    if (paymentMode.getBanks() != null
                                            && !paymentMode.getBanks().contains(((Bank) payChannelBase).getInstId())) {
                                        payChannelIterator.remove();
                                    }
                                } else if (!paymentMode.getChannels().contains(((Bank) payChannelBase).getInstId())) {
                                    payChannelIterator.remove();
                                }
                            }
                        }
                    }
                    if (payMethod.getPayChannelOptions().size() == 0
                            && !EPayMethod.UPI_LITE.getMethod().equals(payMethod.getPayMethod())) {
                        payMethodIterator.remove();
                    }
                }
            }
        }
    }

    private void filterDisabledPayMethods(NativeCashierInfoResponse cashierInfoResponse,
            NativeCashierInfoRequest request) {
        if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()) {
            if (cashierInfoResponse.getBody().getMerchantPayOption() != null) {
                filterDisabledNetBankingPayOptions(
                        cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods(), request);
                updateChannelNotAvailableMsg(cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods(),
                        request);
            }
            if (cashierInfoResponse.getBody().getAddMoneyPayOption() != null) {
                filterDisabledPayMethods(cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods(), request);
            }
        }
    }

    private void filterDisabledSavedInstruments(NativeCashierInfoResponse cashierInfoResponse,
            NativeCashierInfoRequest request) {
        if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()) {
            if (null != cashierInfoResponse.getBody().getMerchantPayOption()) {
                filterDisabledSavedInstruments(cashierInfoResponse.getBody().getMerchantPayOption()
                        .getSavedInstruments(), request);
            }
            if (null != cashierInfoResponse.getBody().getAddMoneyPayOption()) {
                filterDisabledSavedInstruments(cashierInfoResponse.getBody().getAddMoneyPayOption()
                        .getSavedInstruments(), request);
            }
        }

    }

    private void filterDisabledSavedInstruments(List<PayChannelBase> savedInstruments, NativeCashierInfoRequest request) {
        if (savedInstruments != null) {
            Iterator<PayChannelBase> savedInstrumentIterator = savedInstruments.iterator();
            boolean returnDisabledInstruments = request.getBody().isReturnDisabledChannels();
            while (savedInstrumentIterator.hasNext()) {
                PayChannelBase savedInstrument = savedInstrumentIterator.next();
                if (savedInstrument != null && savedInstrument.getIsDisabled() != null
                        && Boolean.parseBoolean(savedInstrument.getIsDisabled().getStatus())) {
                    if (returnDisabledInstruments
                            && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                    savedInstrument.getIsDisabled().getMsg())) {
                        ELitePayViewDisabledReasonMsg.updateDisplayMessage(savedInstrument.getIsDisabled());
                    } else {
                        savedInstrumentIterator.remove();
                    }
                }
            }
        }
    }

    private void filterDisabledPayMethods(List<PayMethod> payMethods, NativeCashierInfoRequest request) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        boolean returnDisabledChannels = request.getBody().isReturnDisabledChannels();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            if (payMethod.getPayChannelOptions() != null) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (payChannelBase.getIsDisabled() != null
                            && Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        if (returnDisabledChannels
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannelBase.getIsDisabled().getMsg())) {
                            ELitePayViewDisabledReasonMsg.updateDisplayMessage(payChannelBase.getIsDisabled());
                        } else {
                            payChannelBaseIterator.remove();
                        }
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

    private void trimSavedInstrumentTypesByAllowedPayMethod(List<PayChannelBase> savedInstuments,
            List<PayMethod> merchantPayMethods) {
        if (null != savedInstuments) {
            Set<String> payMethodSet = payMethodSet(merchantPayMethods);

            Map<String, PayMethod> payMethodMap = getPayMethodMap(merchantPayMethods);

            if (null != payMethodSet && !payMethodSet.isEmpty()) {
                Iterator<PayChannelBase> savedInstumentIterator = savedInstuments.iterator();
                while (savedInstumentIterator.hasNext()) {
                    PayChannelBase savedInstument = savedInstumentIterator.next();

                    // Do not remove savedCard if EMI is available on the card
                    if (!((SavedCard) savedInstument).getIssuingBank().equals("BAJAJFN")
                            && savedInstument instanceof SavedCard && ((SavedCard) savedInstument).getIsEmiAvailable()
                            && payMethodSet.contains(EPayMethod.EMI.getMethod())) {
                        continue;
                    }

                    if (((SavedCard) savedInstument).getIssuingBank().equals("BAJAJFN")
                            && savedInstument instanceof SavedCard
                            && !(((SavedCard) savedInstument).getIsEmiAvailable())) {
                        savedInstumentIterator.remove();
                        continue;
                    }

                    if (!payMethodSet.contains(savedInstument.getPayMethod())) {
                        savedInstumentIterator.remove();
                    } else {
                        PayMethod pm = payMethodMap.get(savedInstument.getPayMethod());
                        if (pm != null) {
                            Set<String> channels = getChannelMap(pm.getPayChannelOptions());
                            if (!channels.contains(((SavedCard) savedInstument).getInstId())) {
                                savedInstumentIterator.remove();
                            }
                        }
                    }
                }
            } else {
                savedInstuments.clear();
            }
        }
    }

    private Set<String> payMethodSet(List<PayMethod> payMethods) {
        if (null != payMethods) {
            Set<String> payMethodSet = new HashSet<String>();
            Iterator<PayMethod> iterator = payMethods.iterator();
            while (iterator.hasNext()) {
                PayMethod payMethod = iterator.next();
                if ("false".equals(payMethod.getIsDisabled().getStatus())) {
                    payMethodSet.add(payMethod.getPayMethod());
                }
            }
            return payMethodSet;
        }
        return null;
    }

    private Map<String, PayMethod> getPayMethodMap(List<PayMethod> payMethods) {
        if (null != payMethods) {
            Map<String, PayMethod> map = new HashMap<>();
            Iterator<PayMethod> iterator = payMethods.iterator();
            while (iterator.hasNext()) {
                PayMethod payMethod = iterator.next();
                if ("false".equals(payMethod.getIsDisabled().getStatus())) {
                    map.put(payMethod.getPayMethod(), payMethod);
                }
            }
            return map;
        }
        return null;
    }

    private Set<String> getChannelMap(List<PayChannelBase> channels) {
        if (channels != null) {
            Set<String> set = new HashSet<>();
            Iterator<PayChannelBase> iterator = channels.iterator();
            while (iterator.hasNext()) {
                BankCard pcb = (BankCard) iterator.next();
                set.add(pcb.getInstId());
            }
            return set;
        }
        return null;
    }

    @Override
    public void trimCcDcPayChannels(NativeCashierInfoResponse response) {
        NativeCashierInfoResponseBody responseBody = response.getBody();
        List<PayMethod> merchantPayMethods = responseBody.getMerchantPayOption().getPayMethods();
        List<PayMethod> addMoneyPayMethods = null;
        if (responseBody.getAddMoneyPayOption() != null) {
            addMoneyPayMethods = responseBody.getAddMoneyPayOption().getPayMethods();
        }
        trimCcDcPayChannels(merchantPayMethods);
        trimCcDcPayChannels(addMoneyPayMethods);
    }

    private void trimCcDcPayChannels(List<PayMethod> paymentModes) {
        if (null != paymentModes && !paymentModes.isEmpty()) {
            for (PayMethod payMethod : paymentModes) {
                if (EPayMethod.CREDIT_CARD.getMethod().equals(payMethod.getPayMethod())
                        || EPayMethod.DEBIT_CARD.getMethod().equals(payMethod.getPayMethod())) {
                    payMethod.setPayChannelOptions(Collections.emptyList());
                }
            }
        }
    }

    @Override
    public void trimByTopNBChannels(NativeCashierInfoResponse response) {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> merchantPayMethods = response.getBody()
                .getMerchantPayOption().getPayMethods();
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> addMoneyPayMethods = null;
        if (response.getBody().getAddMoneyPayOption() != null) {
            addMoneyPayMethods = response.getBody().getAddMoneyPayOption().getPayMethods();
        }
        trimNB(merchantPayMethods);
        trimNB(addMoneyPayMethods);
    }

    private void trimNB(List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> merchantPayMethods) {
        if (null != merchantPayMethods && !merchantPayMethods.isEmpty()) {
            String nbpropertyVal = ConfigurationUtil.getProperty(TOP_NB_CHANNEL_SIZE);
            int topnbchannelsize = TOP_NB_CHANNEL_SIZE_DEFAULT;
            if (!StringUtils.isBlank(nbpropertyVal)) {
                try {
                    topnbchannelsize = Integer.parseInt(nbpropertyVal);
                } catch (NumberFormatException e) {
                    LOGGER.error("Error in parsing top NB channel size value", e);
                }
            }
            for (com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod payMethod : merchantPayMethods) {
                if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod()) && topnbchannelsize > 0
                        && topnbchannelsize <= payMethod.getPayChannelOptions().size()) {
                    if (ff4JUtils.isFeatureEnabled(ENABLE_PPBL_NB_CHANNEL_ORDERING, false)) {
                        Optional<PayChannelBase> ppblPayOption = payMethod.getPayChannelOptions().stream()
                                .filter(payChannel -> NB_CHANNEL_PPBL.equals(((Bank) payChannel).getInstId()))
                                .findFirst();
                        if (ppblPayOption.isPresent()) {
                            List<PayChannelBase> nbChannels = new ArrayList<>();
                            nbChannels.add(ppblPayOption.get());
                            nbChannels.addAll(payMethod.getPayChannelOptions().stream()
                                    .filter(payChannel -> !NB_CHANNEL_PPBL.equals(((Bank) payChannel).getInstId()))
                                    .limit(topnbchannelsize - 1).collect(Collectors.toList()));
                            payMethod.setPayChannelOptions(nbChannels);
                        } else {
                            payMethod.setPayChannelOptions(new ArrayList<PayChannelBase>(payMethod
                                    .getPayChannelOptions().subList(0, topnbchannelsize)));
                        }
                    } else {
                        payMethod.setPayChannelOptions(new ArrayList<PayChannelBase>(payMethod.getPayChannelOptions()
                                .subList(0, topnbchannelsize)));
                    }
                }
            }
        }
    }

    @Override
    public void trimEmiChannelInfo(NativeCashierInfoResponse response) {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> addMoneyPayMethods = null;
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> merchantPayMethods = null;
        // Added null check for fetching addmoney paymethods even though its
        // incorrect existing logic
        if (response.getBody().getAddMoneyPayOption() != null) {
            merchantPayMethods = response.getBody().getAddMoneyPayOption().getPayMethods();
        }
        if (response.getBody().getAddMoneyPayOption() != null) {
            addMoneyPayMethods = response.getBody().getMerchantPayOption().getPayMethods();
        }
        trimEmiChannel(merchantPayMethods);
        trimEmiChannel(addMoneyPayMethods);
    }

    private void trimEmiChannel(List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> payMethods) {
        if (payMethods != null) {
            PayMethod payMethod = payMethods.stream().filter(s -> EPayMethod.EMI.getMethod().equals(s.getPayMethod()))
                    .findAny().orElse(null);
            // for (PayMethod payMethod : payMethods) {
            if (payMethod != null && payMethod.getPayChannelOptions() != null
                    && !payMethod.getPayChannelOptions().isEmpty()) {
                boolean allDisable = true;
                boolean showDisabled = false;
                for (PayChannelBase payChannelBase : payMethod.getPayChannelOptions()) {
                    if (payChannelBase instanceof EmiChannel) {
                        // Do not set emiChannelInfo to null if paymethod is
                        // only emi and contains only one bank and plan
                        if (!(payMethods.size() == 1 && payMethod.getPayChannelOptions().size() == 1 && ((EmiChannel) payChannelBase)
                                .getEmiChannelInfos().size() == 1))
                            ((EmiChannel) payChannelBase).setEmiChannelInfos(null);
                    }
                    if (!Boolean.valueOf(payChannelBase.getIsDisabled().getStatus())) {
                        allDisable = false;
                    }
                    if (Boolean.TRUE.equals(payChannelBase.getIsDisabled().getShowDisabled())) {
                        showDisabled = true;
                    }
                }
                if (allDisable) {
                    payMethod.getIsDisabled().setStatus(String.valueOf(true));
                    if (showDisabled) {
                        payMethod.getIsDisabled().setShowDisabled(Boolean.TRUE);
                    }
                }
            } else if (payMethod != null) {
                payMethod.getIsDisabled().setStatus(String.valueOf(true));
            }
        }
    }

    @Override
    public void filterPayChannelinUPINative(NativeCashierInfoResponse response) {
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> merchantPayMethods = response.getBody()
                .getMerchantPayOption().getPayMethods();
        List<com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod> addMoneyPayMethods = response.getBody()
                .getAddMoneyPayOption().getPayMethods();
        cashierServiceHelper.filterUpiPayOptionsInNative(merchantPayMethods);
        cashierServiceHelper.filterUpiPayOptionsInNative(addMoneyPayMethods);
    }

    // Removing Zest from NB if available
    @Override
    public void filterZestFromNBInNative(NativeCashierInfoResponse response) {

        if (response != null && response.getBody() != null && response.getBody().getMerchantPayOption() != null
                && !CollectionUtils.isEmpty(response.getBody().getMerchantPayOption().getPayMethods())) {

            PayMethod payMode = response.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(payMethod -> EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())).findAny()
                    .orElse(null);

            if (payMode != null && payMode.getPayChannelOptions() != null && !payMode.getPayChannelOptions().isEmpty()) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMode.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    Bank bank = (Bank) payChannelBase;
                    if (ZEST.equals(bank.getInstId())) {
                        payChannelBaseIterator.remove();
                        break;
                    }

                }
            }
        }
    }

    private void updateChannelNotAvailableMsg(List<PayMethod> payMethods, NativeCashierInfoRequest request) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        if (request.getBody().isReturnDisabledChannels()) {
            Iterator<PayMethod> payMethodIterator = payMethods.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                if (payMethod.getPayChannelOptions() != null) {
                    Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                    while (payChannelBaseIterator.hasNext()) {
                        PayChannelBase payChannelBase = payChannelBaseIterator.next();
                        if (payChannelBase.getIsDisabled() != null
                                && Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannelBase.getIsDisabled().getMsg())) {
                            ELitePayViewDisabledReasonMsg.updateDisplayMessage(payChannelBase.getIsDisabled());
                        }
                    }
                }
            }
        }
    }

    private void filterDisabledNetBankingPayOptions(List<PayMethod> payMethods, NativeCashierInfoRequest request) {
        if (payMethods == null || payMethods.isEmpty())
            return;
        Iterator<PayMethod> payMethodIterator = payMethods.iterator();
        boolean returnDisabledChannels = request.getBody().isReturnDisabledChannels();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();

            if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())
                    && payMethod.getPayChannelOptions() != null) {
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (payChannelBase.getIsDisabled() != null
                            && Boolean.parseBoolean(payChannelBase.getIsDisabled().getStatus())) {
                        if (returnDisabledChannels
                                && ELitePayViewDisabledReasonMsg.CHANNEL_NOT_AVAILABLE.getDisabledReason().equals(
                                        payChannelBase.getIsDisabled().getMsg())) {
                            ELitePayViewDisabledReasonMsg.updateDisplayMessage(payChannelBase.getIsDisabled());
                        } else {
                            payChannelBaseIterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void trimSavedInstrumentEMIByDisablePayMethods(List<PayChannelBase> savedInstuments,
            List<PaymentMode> DisablePaymentModes) {
        if (DisablePaymentModes != null && !DisablePaymentModes.isEmpty()) {
            PaymentMode paymentMode = DisablePaymentModes.stream().filter(o -> o.getMode().equals(BizConstant.EMI))
                    .findAny().orElse(null);
            if (paymentMode != null) {
                for (PayChannelBase savedInstument : savedInstuments) {
                    if (savedInstument instanceof SavedCard) {
                        SavedCard savedCard = (SavedCard) savedInstument;
                        if (CollectionUtils.isEmpty(paymentMode.getChannels())
                                || paymentMode.getChannels().contains(savedCard.getIssuingBank())) {
                            savedCard.setIsEmiAvailable(false);
                            savedCard.setMaxAmount(null);
                            savedCard.setMinAmount(null);
                        }
                    }
                }
            }
        }
    }

    /*
     * populate ADVANCE_DEPOSIT_ACCOUNT only if userType and litepayviewconsult
     * response both support advance deposit
     */
    private void trimForAdvanceDeposit(NativeCashierInfoRequest request, NativeCashierInfoResponse cashierInfoResponse,
            WorkFlowResponseBean serviceResponse) {

        boolean isAdvanceDepositUser = Boolean.FALSE;
        // Getting userdetails from cache to get its usertype
        if (null != serviceResponse.getUserDetails()) {
            StringBuilder userDetailsCacheKey = new StringBuilder(CommonConstants.USER_INFO_KEY);
            userDetailsCacheKey.append(serviceResponse.getUserDetails().getUserId());
            UserDetailsBiz userDetail = serviceResponse.getUserDetails();

            List<String> userTypes = null;
            if (userDetail != null) {
                userTypes = userDetail.getUserTypes();
            }
            if (userTypes != null && !userTypes.isEmpty()) {
                for (String type : userTypes) {
                    if ("ADVANCE_DEPOSIT_USER".equals(type)) {
                        isAdvanceDepositUser = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        List<PayMethod> payMethodViews = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods();
        /**
         * isAdvanceDepositAvailable checks for advance deposit account without
         * checking the paymode at Oauth on the basis of Corporate CustID.
         */
        if (serviceResponse.isAdvanceDepositAvailable()) {
            isAdvanceDepositUser = Boolean.TRUE;
        }

        if (!isAdvanceDepositUser) {
            Iterator<PayMethod> payMethodIterator = payMethodViews.iterator();
            while (payMethodIterator.hasNext()) {
                PayMethod payMethod = payMethodIterator.next();
                if (com.paytm.pgplus.facade.enums.PayMethod.ADVANCE_DEPOSIT_ACCOUNT.getOldName().equals(
                        payMethod.getPayMethod())) {
                    payMethodIterator.remove();
                }
            }
        }
    }

    private void filterPromoPayMethods(PromoCodeResponse promoCodeResponse, String promoCodeTypeName,
            List<PayMethod> merchantPayMethods, List<PayMethod> addMoneyPayMethods) {

        try {
            if (promoCodeResponse == null || null == promoCodeTypeName
                    || null == promoCodeResponse.getPromoCodeDetail()) {
                // Something went wrong on promo service or promo not found.
                // Ignore for now.
                return;
            } else if (!PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName)) {
                // Nothing to filter for normal promo
                return;
            } else if (PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName)
                    && !ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse.getPromoResponseCode())) {
                // This is a restricted promo and it is not valid. hence remove
                // all payment modes.
                merchantPayMethods.clear();
                if (addMoneyPayMethods != null) {
                    addMoneyPayMethods.clear();
                }
            } else if (PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName)
                    && ResponseCodeConstant.PROMO_SUCCESS.equals(promoCodeResponse.getPromoResponseCode())) {
                // This is used for restricted|Discount promos only.
                // Filter the pay methods based on promo response pay methods
                filterPromoPayOptionsInNative(merchantPayMethods, addMoneyPayMethods,
                        promoCodeResponse.getPromoCodeDetail(), promoCodeTypeName);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while promo pay options filtering in Native.");
        }
    }

    private void filterPromoPayOptionsInNative(List<PayMethod> merchantPayMethods, List<PayMethod> addMoneyPayMethods,
            PromoCodeData promoCodeData, String promoCodeTypeName) {
        // Use this method only for Discount Type promo codes. And not for
        // CASHBACKS.
        if (!PromoCodeType.DISCOUNT.getValue().equals(promoCodeTypeName))
            return;

        // clear both the lists if promo payment modes data is null or empty
        if (null == promoCodeData.getPaymentModes() || promoCodeData.getPaymentModes().isEmpty()) {
            if (merchantPayMethods != null && !merchantPayMethods.isEmpty()) {
                merchantPayMethods.clear();
            }
            if (addMoneyPayMethods != null && !addMoneyPayMethods.isEmpty()) {
                addMoneyPayMethods.clear();
            }
            return;
        }

        Set<String> promoCodePaymentModes = promoCodeData.getPaymentModes();
        Set<Long> promoCodeNBBanks = promoCodeData.getNbBanks();

        // clear addMoneyPayMethods list if PPI not present in promo payment
        // modes data
        if (!promoCodePaymentModes.contains(EPayMethod.BALANCE.getOldName())
                && !promoCodePaymentModes.contains(EPayMethod.BALANCE.name())) {
            if (addMoneyPayMethods != null && !addMoneyPayMethods.isEmpty()) {
                addMoneyPayMethods.clear();
            }
        }

        // filter merchantPayMethods based on promo data
        if (merchantPayMethods == null || merchantPayMethods.isEmpty()) {
            return;
        }
        Iterator<PayMethod> payMethodIterator = merchantPayMethods.iterator();
        while (payMethodIterator.hasNext()) {
            PayMethod payMethod = payMethodIterator.next();
            // Remove the payment mode from list if it is not present in promo
            // data
            if (!promoCodePaymentModes.contains(EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()).getOldName())
                    && !promoCodePaymentModes
                            .contains(EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()).name())) {
                payMethodIterator.remove();
            } else if (EPayMethod.NET_BANKING.getMethod().equals(payMethod.getPayMethod())) {
                // filter netbanking as per promo nbbanks list
                Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                while (payChannelBaseIterator.hasNext()) {
                    PayChannelBase payChannelBase = payChannelBaseIterator.next();
                    if (payChannelBase instanceof Bank) {
                        BankInfoData bankInfoData = merchantBankInfoDataService.getBankInfo(((Bank) payChannelBase)
                                .getInstId());
                        if (null != bankInfoData && !promoCodeNBBanks.contains(bankInfoData.getBankId())) {
                            payChannelBaseIterator.remove();
                        }
                    }
                }

            }
        }
    }

    private void updatePaymentFlow(NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        List<PaymentMode> disablePaymentMode = request.getBody().getDisablePaymentMode();
        if (disablePaymentMode != null && !disablePaymentMode.isEmpty()) {
            boolean isDisableBalance = disablePaymentMode.stream().anyMatch(
                    paymentMode -> StringUtils.equalsIgnoreCase(paymentMode.getMode(), EPayMethod.BALANCE.getMethod()));
            if (isDisableBalance) {
                response.getBody().setPaymentFlow(EPayMode.NONE);
            }
        }

        List<PaymentMode> enablePaymentModes = request.getBody().getEnablePaymentMode();
        if (enablePaymentModes != null && !enablePaymentModes.isEmpty()) {
            boolean isBalancePresent = enablePaymentModes.stream().anyMatch(
                    paymentMode -> StringUtils.equalsIgnoreCase(paymentMode.getMode(), EPayMethod.BALANCE.getMethod()));

            if (!isBalancePresent) {
                LOGGER.info("making paymentFlow=NONE as Wallet not present");
                response.getBody().setPaymentFlow(EPayMode.NONE);
                response.getBody().setAddMoneyPayOption(new PayOption());
                response.getBody().setAddMoneyMerchantDetails(null);
            }
        }
    }

    private void updateEmiAvailableOnSavedInstruments(List<PayChannelBase> savedInstruments,
            List<PayMethod> payMethods, List<PaymentMode> enablePaymethods) {

        if (CollectionUtils.isEmpty(savedInstruments)) {
            return;
        }

        List<PayChannelBase> emiChannels = Collections.EMPTY_LIST;
        if (CollectionUtils.isNotEmpty(payMethods)) {
            PayMethod emiPayMethod = payMethods.stream()
                    .filter(payMethod -> payMethod.getPayMethod().equalsIgnoreCase(EPayMethod.EMI.getMethod()))
                    .findAny().orElse(null);
            if (emiPayMethod != null) {
                emiChannels = emiPayMethod.getPayChannelOptions();
            }
        }
        /*
         * if emi does not have support for particular channel(not present in
         * enable channels) ,then setting the same.
         */
        for (PayChannelBase savedInstument : savedInstruments) {
            if (savedInstument instanceof SavedCard) {
                SavedCard savedCard = (SavedCard) savedInstument;
                if (CollectionUtils.isEmpty(emiChannels)
                        || emiChannels.stream().noneMatch(
                                emiChannel -> StringUtils.equalsIgnoreCase(((Bank) emiChannel).getInstId(),
                                        savedCard.getIssuingBank())
                                        && ((EmiChannel) emiChannel).getEmiType().getType()
                                                .equals(savedCard.getCardDetails().getCardType()))
                        || !isSchemeSupported(enablePaymethods, savedCard.getInstId())) {
                    savedCard.setIsEmiAvailable(false);
                    savedCard.setMaxAmount(null);
                    savedCard.setMinAmount(null);
                }
            }
        }
    }

    private boolean filterEMI(final String bankCode, final MBIDLimitMappingDetails mbidLimitMappingDetails,
            PayMethod payMethod, EmiType emiType) {

        if (payMethod == null) {
            return false;
        }

        List<PayChannelBase> payChannelOptions = payMethod.getPayChannelOptions();

        PayChannelBase payChannelBase = fetchPayChannelOption(payChannelOptions, bankCode, emiType);
        if (payChannelBase == null) {
            payMethod.setPayChannelOptions(null);
            return false;
        }

        payMethod.setPayChannelOptions(Collections.singletonList(payChannelBase));
        EMIChannelInfo emiChannelInfo = fetchEMIChannelInfo(mbidLimitMappingDetails, payChannelBase);
        EmiChannel emiChannel = (EmiChannel) payChannelBase;

        if (emiChannelInfo == null) {
            emiChannel.setEmiChannelInfos(null);
            return false;
        }

        emiChannel.setEmiChannelInfos(Collections.singletonList(emiChannelInfo));
        return true;
    }

    private EMIChannelInfo fetchEMIChannelInfo(MBIDLimitMappingDetails mbidLimitMappingDetails,
            PayChannelBase payChannelBase) {
        Double interest = mbidLimitMappingDetails.getInterest();
        Long month = mbidLimitMappingDetails.getMonth();
        EmiChannel emiChannel = (EmiChannel) payChannelBase;
        return emiChannel
                .getEmiChannelInfos()
                .stream()
                .filter(s -> s.getOfMonths().equals(String.valueOf(month))
                        && s.getInterestRate().equals(String.valueOf(interest))).findAny().orElse(null);
    }

    private PayChannelBase fetchPayChannelOption(List<PayChannelBase> payChannelOptions, String bankCode,
            EmiType emiType) {
        return payChannelOptions.stream().map(s -> (EmiChannel) s)
                .filter(s -> bankCode.equals(s.getInstId()) && emiType.equals(s.getEmiType())).findAny().orElse(null);
    }

    private String[] fetchEmiBankDetailsFromEmiOptions(String emiOption, String emiType) {
        if (StringUtils.contains(emiType, TheiaConstant.ExtraConstants.DC)) {
            return StringUtils.split(emiOption, ":")[2].split("_");
        } else {
            return StringUtils.substringAfter(emiOption, ":").split("_");
        }
    }

    private BankMasterDetails getBankMasterDetails(String bank) {
        BankMasterDetails bankMasterDetails;
        try {
            bankMasterDetails = bankInfoDataService.getBankListInfoDataFromBankIds(Collections.singletonList(bank))
                    .getBankMasterDetailsList().get(0);
            EXT_LOGGER.customInfo("Mapping response - BankMasterDetails :: {} for Bank :: {}", bankMasterDetails, bank);
            if (bankMasterDetails == null) {
                LOGGER.info("For zeroCostEmi Invalid Payment details entered");
                throw RequestValidationException.getException();
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching bank details for  {} : {} ", bank, e);
            throw RequestValidationException.getException(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        }
        return bankMasterDetails;
    }

    private MBIDLimitMappingDetails getPlanDetails(String planId) {
        MBIDLimitMappingDetails mbidLimitMappingDetails;
        try {
            mbidLimitMappingDetails = mbidLimitDataService.getMbidLimitInfoData(planId);
            EXT_LOGGER.customInfo("Mapping response - MBIDLimitMappingDetails :: {} for PlanId :: {}",
                    mbidLimitMappingDetails, planId);
            if (mbidLimitMappingDetails == null) {
                LOGGER.info("For zeroCostEmi Invalid Payment details entered");
                throw RequestValidationException.getException();
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching plan details for  {} : {}", planId, e);
            throw RequestValidationException.getException(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        }
        return mbidLimitMappingDetails;
    }

    private void updateConsultFee(WorkFlowResponseBean serviceResponse, NativeCashierInfoResponse response) {
        ConsultDetails consultDetails;
        if (serviceResponse.getConsultFeeResponse() != null && response.getBody().getMerchantPayOption() != null) {
            for (PayMethod payMethod : response.getBody().getMerchantPayOption().getPayMethods()) {
                // (serviceResponse.getConsultFeeResponse().getConsultDetails().get())
                consultDetails = serviceResponse.getConsultFeeResponse().getConsultDetails()
                        .get(EPayMethod.getPayMethodByMethod(payMethod.getPayMethod()));
                if (consultDetails != null) {
                    payMethod.setFeeAmount(consultDetails.getFeeAmount());
                    payMethod.setTaxAmount(consultDetails.getTaxAmount());
                    payMethod.setTotalTransactionAmount(consultDetails.getTotalTransactionAmount());
                }
            }

            PayMethod netbanking = response.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(s -> EPayMethod.NET_BANKING.getMethod().equals(s.getPayMethod())).findAny().orElse(null);

            PayMethod ppbl = response.getBody().getMerchantPayOption().getPayMethods().stream()
                    .filter(s -> EPayMethod.PPBL.getOldName().equals(s.getPayMethod())).findAny().orElse(null);

            if (netbanking != null && ppbl != null) {
                ppbl.setFeeAmount(netbanking.getFeeAmount());
                ppbl.setTaxAmount(netbanking.getTaxAmount());
                ppbl.setTotalTransactionAmount(netbanking.getTotalTransactionAmount());

            }

            ConsultDetails consultDetailsNetbanking = serviceResponse.getConsultFeeResponse().getConsultDetails()
                    .get(EPayMethod.NET_BANKING);
            if (consultDetailsNetbanking != null && ppbl != null) {
                ConsultDetails consultDetailsPpbl = consultDetailsNetbanking;
                consultDetailsPpbl.setPayMethod(EPayMethod.PPBL);
                serviceResponse.getConsultFeeResponse().getConsultDetails().put(EPayMethod.PPBL, consultDetailsPpbl);
            }

        }
    }

    @Override
    public void trimConsultFeeResponse(NativeCashierInfoResponse response) {
        if (response.getBody().getConsultFeeResponse() != null) {
            response.getBody().setConsultFeeResponse(null);
        }

    }

    private boolean isSchemeSupported(List<PaymentMode> enablePayment, String instId) {

        if (CollectionUtils.isEmpty(enablePayment)) {
            return true;
        }
        for (PaymentMode paymentMode : enablePayment) {
            if (EPayMethod.EMI.getMethod().equals(paymentMode.getMode())) {
                if (CollectionUtils.isEmpty(paymentMode.getChannels()) || (paymentMode.getChannels().contains(instId))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void filterSarvatraVpa(NativeCashierInfoResponseBody body, CashierInfoRequest serviceRequest) {

        if (null == serviceRequest || null == serviceRequest.getBody()
                || CollectionUtils.isEmpty(serviceRequest.getBody().getBankAccountNumbers())) {
            // for backward compatibility when merchant do not send bank account
            // numbers
            return;
        }
        if (null == body.getMerchantPayOption().getUserProfileSarvatra()
                || null == body.getMerchantPayOption().getUserProfileSarvatra().getResponse()
                || CollectionUtils.isEmpty(body.getMerchantPayOption().getUserProfileSarvatra().getResponse()
                        .getVpaDetails())) {
            return;
        }

        List<String> bankAccountNumbers = getBankAccountNumbers(serviceRequest.getBody().getBankAccountNumbers());

        List<SarvatraVpaDetails> vpaDetails = body.getMerchantPayOption().getUserProfileSarvatra().getResponse()
                .getVpaDetails();
        LOGGER.info("Saved VPA count before filtering {} ", vpaDetails.size());
        Iterator<SarvatraVpaDetails> vpaIterator = vpaDetails.iterator();
        while (vpaIterator.hasNext()) {
            SarvatraVpaDetails vpaDetail = vpaIterator.next();
            if (null != vpaDetail.getDefaultDebit()) {
                String account = StringUtils.stripStart(vpaDetail.getDefaultDebit().getAccount(), "0");
                if (!bankAccountNumbers.contains(account)) {
                    vpaIterator.remove();
                }
            }
        }

        LOGGER.info("Saved VPA count after filtering {} ", vpaDetails.size());

        if (CollectionUtils.isEmpty(vpaDetails)) {
            body.getMerchantPayOption().setUserProfileSarvatra(null);
            return;
        }

        List<PaytmBanksVpaDefaultDebitCredit> bankAccounts = body.getMerchantPayOption().getUserProfileSarvatra()
                .getResponse().getBankAccounts();
        if (CollectionUtils.isNotEmpty(bankAccounts)) {
            Iterator<PaytmBanksVpaDefaultDebitCredit> accountIterator = bankAccounts.iterator();
            while (accountIterator.hasNext()) {
                PaytmBanksVpaDefaultDebitCredit bankAccount = accountIterator.next();
                String account = StringUtils.stripStart(bankAccount.getAccount(), "0");
                if (!bankAccountNumbers.contains(account)) {
                    accountIterator.remove();
                }
            }

        }

    }

    private List<String> getBankAccountNumbers(List<String> accountNumberList) {
        List<String> bankAccounts = new ArrayList<>();
        for (String account : accountNumberList) {
            bankAccounts.add(StringUtils.stripStart(account, "0"));
        }
        return bankAccounts;
    }

    @Override
    public void trimAdditionalInfoForSavedAssets(NativeCashierInfoResponse response) {
        if (response.getBody().getMerchantPayOption() != null) {
            trimAdditionalInfoForSavedAssetsUtil(response.getBody().getMerchantPayOption().getSavedInstruments());
        }
        if (response.getBody().getAddMoneyPayOption() != null) {
            trimAdditionalInfoForSavedAssetsUtil(response.getBody().getAddMoneyPayOption().getSavedInstruments());
        }

    }

    private void trimAdditionalInfoForSavedAssetsUtil(List<PayChannelBase> savedInstruments) {
        if (CollectionUtils.isNotEmpty(savedInstruments)) {
            for (PayChannelBase savedInstrument : savedInstruments) {
                SavedCard savedcard = (SavedCard) savedInstrument;
                savedcard.getCardDetails().setCurrencyCodeIso(null);
                savedcard.getCardDetails().setCurrency(null);
                savedcard.getCardDetails().setCurrencyCode(null);
                savedcard.getCardDetails().setCategory(null);
                savedcard.getCardDetails().setCountryCode(null);
                savedcard.getCardDetails().setInstName(null);
                savedcard.getCardDetails().setZeroSuccessRate(null);
                savedcard.getCardDetails().setCurrencyPrecision(null);
                savedcard.getCardDetails().setSymbol(null);
                savedcard.getCardDetails().setCountry(null);
                savedcard.getCardDetails().setCountryCodeIso(null);
                savedcard.getCardDetails().setCardScheme(null);
            }
        }

    }

    private boolean isDisableWallet(List<PaymentMode> enablePaymentModes, List<PaymentMode> disablePaymentModes) {
        boolean disableWallet = false;
        if (CollectionUtils.isNotEmpty(disablePaymentModes)) {
            for (PaymentMode paymode : disablePaymentModes) {
                if (EPayMethod.BALANCE.getMethod().equals(paymode.getMode())) {
                    disableWallet = true;
                    break;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(enablePaymentModes)) {
            disableWallet = true;
            for (PaymentMode paymode : enablePaymentModes) {
                if (EPayMethod.BALANCE.getMethod().equals(paymode.getMode())) {
                    disableWallet = false;
                    break;
                }
            }
        }
        return disableWallet;
    }

    private void filterUpiCollectChannel(NativeCashierInfoResponse response, NativeCashierInfoRequest request)
            throws Exception {

        response.getBody().setDisableCustomVPAInUPICollect(true);
        if (request != null && request.getHead() != null && StringUtils.isNotBlank(request.getHead().getTxnToken())) {
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(request.getHead()
                    .getTxnToken());
            if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                if (orderDetail != null) {
                    PayOption payOption = orderDetail.isNativeAddMoney() ? response.getBody().getMerchantPayOption()
                            : response.getBody().getAddMoneyPayOption();
                    if (payOption != null) {
                        filteringUPICollectChannel(response, request, payOption, orderDetail);
                    }
                } else {
                    LOGGER.info("orderDetails object is null");
                }

            }
        }
    }

    void filteringUPICollectChannel(NativeCashierInfoResponse response, NativeCashierInfoRequest request,
            PayOption payOption, InitiateTransactionRequestBody orderDetail) {
        String walletBalanceStr = "";
        String txnAmountStr = "";
        Double txnAmount = 0.0;
        Double walletBalance = 0.0;
        String differenceAmount;
        Double diffAmountInDouble = 0.0;
        boolean removeUPICollect = true;
        List<VpaDetailV4> vpaDetailsList = null;
        List<SarvatraVpaDetails> vpaDetailsList2 = null;
        if (CollectionUtils.isNotEmpty(payOption.getPayMethods())) {
            List<PayMethod> payMethods = payOption.getPayMethods();
            vpaDetailsList = fetchVPADetailsList(payOption);
            vpaDetailsList2 = fetchVPADetailsList2(payOption);
            if (merchantPreferenceService.enableUPICollectFromADDNPAY(orderDetail.getMid())) {

                if (orderDetail.getPaytmSsoToken() != null && !orderDetail.isAppInvoke()) {
                    walletBalanceStr = fetchWalletBalance(response, request);
                    txnAmountStr = fetchTxnAmount(orderDetail);
                    Double maxDeficitAmount = Double.valueOf((com.paytm.pgplus.common.config.ConfigurationUtil
                            .getProperty(MAX_DEFICIT_AMOUNT, "2000")));

                    if (StringUtils.isNotBlank(walletBalanceStr) && StringUtils.isNotBlank(txnAmountStr)) {
                        walletBalance = Double.valueOf(walletBalanceStr);
                        txnAmount = Double.valueOf(txnAmountStr);
                        differenceAmount = differenceAmount(txnAmount, walletBalance);
                        diffAmountInDouble = Double.valueOf(differenceAmount);

                        if (((vpaDetailsList != null && vpaDetailsList.size() > 0) || (vpaDetailsList2 != null && vpaDetailsList2
                                .size() > 0)) && diffAmountInDouble <= maxDeficitAmount) {
                            removeUPICollect = false;
                            LOGGER.info("Enable UPI collect for 3P on add money and add n pay for txn upto Rs 2000");
                        } else {
                            LOGGER.info("vpaDetailsList is not found");
                        }
                    } else {
                        LOGGER.info("Not able to fetch TxnAmount");
                    }
                }
            }
            if (removeUPICollect) {

                payMethods
                        .stream()
                        .filter(payMethod -> EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod()))
                        .forEach(
                                payMethod -> {
                                    payMethod.getPayChannelOptions().removeIf(
                                            payChannelBase -> payChannelBase instanceof Bank
                                                    && UPI.equals(((Bank) payChannelBase).getInstId()));
                                });

                List<PayMethod> payMethodList = payMethods
                        .stream()
                        .filter(payMethod -> EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod())
                                && payMethod.getPayChannelOptions() != null
                                && payMethod
                                        .getPayChannelOptions()
                                        .stream()
                                        .anyMatch(
                                                payChannelBase -> ((payChannelBase instanceof Bank && UPI
                                                        .equals(((Bank) payChannelBase).getInstId())) || UPI_PUSH_EXPRESS
                                                        .equals(((Bank) payChannelBase).getInstId()))))
                        .collect(Collectors.toList());

                if (ff4JUtils.isFeatureEnabledOnMid(orderDetail.getMid(), ADDNPAY_UPI_ACC_REF_ID_ALLOWED, false))
                    payMethodList = payMethods
                            .stream()
                            .filter(payMethod -> EPayMethod.UPI.getMethod().equals(payMethod.getPayMethod())
                                    && payMethod.getPayChannelOptions() != null
                                    && payMethod
                                            .getPayChannelOptions()
                                            .stream()
                                            .anyMatch(
                                                    payChannelBase -> ((payChannelBase instanceof Bank && UPI_PUSH
                                                            .equals(((Bank) payChannelBase).getInstId())) || UPI_PUSH_EXPRESS
                                                            .equals(((Bank) payChannelBase).getInstId()))))
                            .collect(Collectors.toList());

                if (payMethodList.size() == 0) {
                    if (vpaDetailsList != null) {
                        removeVpaList(payOption);
                    } else if (vpaDetailsList2 != null) {
                        removeVpaList(payOption);
                    }
                }
            }
        }
    }

    private String differenceAmount(Double txnAmount, Double walletBalance) {
        Double diff = txnAmount - walletBalance;
        return diff.toString();
    }

    private String fetchWalletBalance(NativeCashierInfoResponse response, NativeCashierInfoRequest request) {
        String walletBalanceStr = "0";
        PayMethod payMethod = response.getBody().getMerchantPayOption().getPayMethods().stream()
                .filter(s -> EPayMethod.BALANCE.getMethod().equals(s.getPayMethod())).findAny().orElse(null);
        if (payMethod != null && payMethod.getPayChannelOptions() != null
                && !payMethod.getPayChannelOptions().isEmpty()) {
            AccountInfo balanceInfo = ((BalanceChannel) payMethod.getPayChannelOptions().get(0)).getBalanceInfo();
            if (balanceInfo != null && balanceInfo.getAccountBalance() != null
                    && !StringUtils.isEmpty(balanceInfo.getAccountBalance().getValue())) {
                walletBalanceStr = balanceInfo.getAccountBalance().getValue();
            }
        }
        return walletBalanceStr;
    }

    private String fetchTxnAmount(InitiateTransactionRequestBody orderDetail) {
        String txnAmount = "";
        if (orderDetail.getTxnAmount() != null && orderDetail.getTxnAmount().getValue() != null) {
            txnAmount = orderDetail.getTxnAmount().getValue();
        }
        return txnAmount;
    }

    public List<VpaDetailV4> fetchVPADetailsList(PayOption payOption) {
        List<VpaDetailV4> vpaDetailList = null;
        if (payOption.getUpiProfileV4() != null && payOption.getUpiProfileV4().getRespDetails() != null
                && payOption.getUpiProfileV4().getRespDetails().getProfileDetail() != null
                && payOption.getUpiProfileV4().getRespDetails().getProfileDetail().getVpaDetails() != null) {
            vpaDetailList = payOption.getUpiProfileV4().getRespDetails().getProfileDetail().getVpaDetails();
        }
        return vpaDetailList;
    }

    public List<SarvatraVpaDetails> fetchVPADetailsList2(PayOption payOption) {
        List<SarvatraVpaDetails> vpaDetailList = null;

        if (payOption.getUserProfileSarvatra() != null && payOption.getUserProfileSarvatra().getResponse() != null
                && payOption.getUserProfileSarvatra().getResponse().getVpaDetails() != null) {
            vpaDetailList = payOption.getUserProfileSarvatra().getResponse().getVpaDetails();
        }
        return vpaDetailList;
    }

    private void removeVpaList(PayOption payOption) {

        if (payOption.getUpiProfileV4() != null) {
            payOption.setUpiProfileV4(null);
        } else if (payOption.getUserProfileSarvatra() != null) {
            payOption.setUserProfileSarvatra(null);
        }

    }

    public void filterPayModeOnBossLevel(WorkFlowResponseBean workFlowResponseBean, NativeCashierInfoResponse response) {
        String OsType = fetchOsType(workFlowResponseBean);
        try {
            MerchantBlockFilters merchantBlockFilters = merchantBlockFiltersService
                    .getMerchantBlockFilters(workFlowResponseBean.getWorkFlowRequestBean().getPaytmMID());
            EXT_LOGGER.customInfo("Response received for MerchantBlockFilters is :{} ", merchantBlockFilters);
            if (merchantBlockFilters != null) {
                Map<String, List<MerchantBlockConfig>> merchantBlockFiltersMap = merchantBlockFilters
                        .getMerchantBlockFilters();
                if (MapUtils.isNotEmpty(merchantBlockFiltersMap) && response.getBody() != null
                        && response.getBody().getMerchantPayOption() != null
                        && response.getBody().getAddMoneyPayOption() != null) {
                    PayOption payOption = response.getBody().getMerchantPayOption();
                    List<PayMethod> payMethodList = payOption.getPayMethods();
                    List<MerchantBlockConfig> merchantBlockConfigs = merchantBlockFiltersMap.get("UPI");
                    if (CollectionUtils.isNotEmpty(merchantBlockConfigs) && CollectionUtils.isNotEmpty(payMethodList)) {
                        PayMethod payMethod = payMethodList.stream()
                                .filter(s -> s.getPayMethod().equals(EPayMethod.UPI.getMethod())).findAny()
                                .orElse(null);
                        if (payMethod != null) {
                            advanceFilterForUpi(OsType, response, merchantBlockConfigs);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching merchant block filters");
        }
    }

    public void advanceFilterForUpi(String OS, NativeCashierInfoResponse response,
            List<MerchantBlockConfig> merchantBlockConfigList) {
        PayOption merchantPayOption = response.getBody().getMerchantPayOption();
        PayOption addMoneyPayOption = response.getBody().getAddMoneyPayOption();
        List<PayMethod> merchantPayMethods = merchantPayOption.getPayMethods();
        List<PayMethod> addMoneyPayMethods = addMoneyPayOption.getPayMethods();
        for (MerchantBlockConfig merchantBlockConfig : merchantBlockConfigList) {
            if (merchantBlockConfig.getOperatingSystem() == null
                    || merchantBlockConfig.getOperatingSystem().equalsIgnoreCase(OS)) {
                String gateway = merchantBlockConfig.getGateway();
                if (StringUtils.isBlank(gateway)) {
                    if (CollectionUtils.isNotEmpty(merchantPayMethods)) {
                        merchantPayMethods.removeIf(payMethod -> payMethod.getPayMethod().equals(
                                EPayMethod.UPI.getMethod()));
                    }
                    if (CollectionUtils.isNotEmpty(addMoneyPayMethods)) {
                        addMoneyPayMethods.removeIf(payMethod -> payMethod.getPayMethod().equals(
                                EPayMethod.UPI.getMethod()));
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(merchantPayMethods)) {
                        merchantPayMethods
                                .stream()
                                .filter(payMethods -> payMethods.getPayMethod().equals(EPayMethod.UPI.getMethod()))
                                .forEach(
                                        payMethod -> payMethod.getPayChannelOptions().removeIf(
                                                payChannelBase -> payChannelBase.getPayChannelOption().equals(gateway)));
                    }
                    if (CollectionUtils.isNotEmpty(addMoneyPayMethods)) {
                        addMoneyPayMethods
                                .stream()
                                .filter(payMethods -> payMethods.getPayMethod().equals(EPayMethod.UPI.getMethod()))
                                .forEach(
                                        payMethod -> payMethod.getPayChannelOptions().removeIf(
                                                payChannelBase -> payChannelBase.getPayChannelOption().equals(gateway)));
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(merchantPayMethods)) {
            merchantPayMethods.removeIf(payMethod -> payMethod.getPayMethod().equals(EPayMethod.UPI.getMethod())
                    && CollectionUtils.isEmpty(payMethod.getPayChannelOptions()));
        }
        if (CollectionUtils.isNotEmpty(addMoneyPayMethods)) {
            addMoneyPayMethods.removeIf(payMethod -> payMethod.getPayMethod().equals(EPayMethod.UPI.getMethod())
                    && CollectionUtils.isEmpty(payMethod.getPayChannelOptions()));
        }
    }

    public String fetchOsType(WorkFlowResponseBean workFlowResponseBean) {
        String OSType = "unknown";
        try {
            if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null
                    && workFlowResponseBean.getWorkFlowRequestBean().getEnvInfoReqBean() != null) {
                String browserUserAgentInfo = workFlowResponseBean.getWorkFlowRequestBean().getEnvInfoReqBean()
                        .getBrowserUserAgent();
                UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
                ReadableUserAgent userAgent = parser.parse(browserUserAgentInfo);
                OperatingSystemFamily operatingSystemFamily = OperatingSystemFamily.evaluate(userAgent
                        .getOperatingSystem().getFamilyName());
                OSType = operatingSystemFamily.getName();
                EXT_LOGGER.customInfo("Os Type : {} Operating System : {}  ", OSType, operatingSystemFamily);
            }
        } catch (Exception e) {
            return OSType;
        }
        return OSType;
    }

    private boolean isMLVMerchant(NativeCashierInfoRequest request) {
        if (request.getBody() != null && request.getBody().isMlvSupported()) {
            QRCodeInfoResponseData qrCodeDetailsResponse = request.getBody().getqRCodeInfo();
            if (qrCodeDetailsResponse != null && StringUtils.isNotBlank(qrCodeDetailsResponse.getKybId())
                    && StringUtils.isNotBlank(qrCodeDetailsResponse.getShopId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMLoyalMerchant(NativeCashierInfoResponse response) {
        if (response.getBody() != null && response.getBody().getMerchantPayOption() != null) {
            List<PayMethod> payMethodsList = response.getBody().getMerchantPayOption().getPayMethods();
            if (CollectionUtils.isNotEmpty(payMethodsList)) {
                return (payMethodsList.stream().anyMatch(p -> EPayMethod.LOYALTY_POINT.getMethod().equalsIgnoreCase(
                        p.getPayMethod())));
            }
        }
        return false;
    }

    private boolean isPostPaidEnabled(NativeCashierInfoResponse response, WorkFlowResponseBean responseBean) {
        if (response.getBody() != null && response.getBody().getMerchantPayOption() != null) {
            List<PayMethod> payMethodsList = response.getBody().getMerchantPayOption().getPayMethods();
            if (CollectionUtils.isNotEmpty(payMethodsList)) {
                return (payMethodsList.stream().anyMatch(
                        p -> EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod().equalsIgnoreCase(p.getPayMethod()))
                        && responseBean.getUserDetails() != null && responseBean.getUserDetails().isPaytmCCEnabled());
            }
        }
        return false;
    }

    private boolean checkIfUpiBankAccountLinked(NativeCashierInfoResponse response) {
        if (response.getBody() != null && response.getBody().getAddMoneyPayOption() != null) {
            UserProfileSarvatraV4 userProfileSarvatraV4 = response.getBody().getAddMoneyPayOption().getUpiProfileV4();
            if (userProfileSarvatraV4 != null && userProfileSarvatraV4.getRespDetails() != null
                    && userProfileSarvatraV4.getRespDetails().getProfileDetail() != null) {
                List<UpiBankAccountV4> upiProfileDetailV4List = userProfileSarvatraV4.getRespDetails()
                        .getProfileDetail().getBankAccounts();
                if (CollectionUtils.isNotEmpty(upiProfileDetailV4List) && upiProfileDetailV4List.get(0) != null
                        && upiProfileDetailV4List.get(0).getBank() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void trimAddMoneyPayOptionOffline(String[] paymodes, NativeCashierInfoResponse response) {
        if (response.getBody() != null && response.getBody().getAddMoneyPayOption() != null) {
            List<PayMethod> payMethods = response.getBody().getAddMoneyPayOption().getPayMethods();
            if (CollectionUtils.isEmpty(payMethods)) {
                return;
            }
            List<PayMethod> payMethodList = new ArrayList<>();
            Set<String> payModesList = new HashSet<>(Arrays.asList(paymodes));
            for (PayMethod payMethod : payMethods) {
                if (payModesList.contains(payMethod.getPayMethod())) {
                    if (EPayMethod.UPI.getMethod().equalsIgnoreCase(payMethod.getPayMethod())) {
                        if (checkIfUpiBankAccountLinked(response)) {
                            payMethodList.add(payMethod);
                        }
                    } else {
                        payMethodList.add(payMethod);
                    }
                }
            }
            if (payMethodList.size() != 0) {
                response.getBody().setAddMoneyPayOptionsAvailable(true);
            }
            response.getBody().getAddMoneyPayOption().setSavedInstruments(null);
            response.getBody().getAddMoneyPayOption().setPayMethods(payMethodList);
        }
    }

    private void populateZestDataforPG2(NativeCashierInfoResponse response, WorkFlowResponseBean responseBean,
            CashierInfoRequest request) {
        populateZestData(response.getBody().getMerchantPayOption(), responseBean, request, false);
        if (response.getBody().getAddMoneyPayOption() != null) {
            populateZestData(response.getBody().getAddMoneyPayOption(), responseBean, request, true);
        }
    }

    private void populateZestData(PayOption payOption, WorkFlowResponseBean flowResponseBean,
            CashierInfoRequest request, boolean isAddMoneyPayOption) {
        if (payOption != null && CollectionUtils.isNotEmpty(payOption.getPayMethods())) {
            PayMethod nbPayMethod = payOption.getPayMethods().stream()
                    .filter(x -> EPayMethod.NET_BANKING.getMethod().equalsIgnoreCase(x.getPayMethod())).findAny()
                    .orElse(null);
            if (nbPayMethod != null && nbPayMethod.getPayMethod() != null
                    && CollectionUtils.isNotEmpty(nbPayMethod.getPayChannelOptions())) {
                NetBanking netBanking = new NetBanking();
                PayChannelBase channelBase = null;
                for (PayMethod payMethod : payOption.getPayMethods()) {
                    if (EPayMethod.EMI.getMethod().equals(payMethod.getPayMethod())) {
                        Iterator<PayChannelBase> payChannelBaseIterator = payMethod.getPayChannelOptions().iterator();
                        while (payChannelBaseIterator.hasNext()) {
                            EmiChannel emiChannel = (EmiChannel) payChannelBaseIterator.next();
                            if (ZEST.equals(emiChannel.getInstId())) {
                                if ((request.getBody().getOrderAmount() == null)
                                        || (Double.parseDouble(request.getBody().getOrderAmount().getValue()) >= Double
                                                .parseDouble(emiChannel.getMinAmount().getValue()) && Double
                                                .parseDouble(request.getBody().getOrderAmount().getValue()) <= Double
                                                .parseDouble(emiChannel.getMaxAmount().getValue()))) {
                                    LOGGER.info("Extracting Zest Configurations from EMI : {}", channelBase);
                                    if (isAddMoneyPayOption) {
                                        flowResponseBean.setZestOnAddMoneyPG2(true);
                                    } else {
                                        flowResponseBean.setZestOnPG2(true);
                                    }
                                    emiChannel.setInstId(ZEST);
                                    netBanking.setInstId(emiChannel.getInstId());
                                    netBanking.setInstName(emiChannel.getInstName());
                                    netBanking.setInstDispCode(emiChannel.getInstDispCode());
                                    netBanking.setPayMethod(NET_BANKING);
                                    netBanking.setPayChannelOption(NET_BANKING_ZEST);
                                    channelBase = netBanking;
                                    channelBase.setHybridDisabled(emiChannel.isHybridDisabled());
                                    channelBase.setIsDisabled(emiChannel.getIsDisabled());
                                    emiChannel.setEmiType(EmiType.NBFC);
                                    LOGGER.info("Setting Zest Configurations from EMI to NetBanking: {}", channelBase);
                                    nbPayMethod.getPayChannelOptions().add(channelBase);
                                    break;
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                }
            } else if (nbPayMethod == null) {
                // To remove Zest if NB is not applicable for merchant.
                PayMethod emiPayMethod = payOption.getPayMethods().stream()
                        .filter(x -> EPayMethod.EMI.getMethod().equalsIgnoreCase(x.getPayMethod())).findAny()
                        .orElse(null);
                if (emiPayMethod != null && CollectionUtils.isNotEmpty(emiPayMethod.getPayChannelOptions())) {
                    Iterator<PayChannelBase> payChannelBaseIterator = emiPayMethod.getPayChannelOptions().iterator();
                    while (payChannelBaseIterator.hasNext()) {
                        EmiChannel emiChannel = (EmiChannel) payChannelBaseIterator.next();
                        if (ZEST.equals(emiChannel.getInstId())) {
                            LOGGER.info("Removing Zest from EMI as NetBanking is not available");
                            payChannelBaseIterator.remove();
                            break;
                        }
                    }
                }
            }
        }
    }
}