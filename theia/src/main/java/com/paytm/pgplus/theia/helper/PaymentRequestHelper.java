package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowRequestCreationHelper;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.*;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.enums.TransactionStatus;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ICacheCardService;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.subscriptionClient.model.request.ModifySubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.subscriptionClient.utils.SubscriptionUtil;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtendedInfoPay;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.enums.TransactionMode;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.mapper.TheiaCashierMapper;
import com.paytm.pgplus.theia.merchant.models.PaymentInfo;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.paytm.pgplus.facade.constants.FacadeConstants.ExtendInfo.ROUTE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;

/**
 * @author amit.dubey
 * @since April 7, 2016
 *
 */
@Service
public final class PaymentRequestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestHelper.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    ITheiaSessionDataService sessionDataService;

    @Autowired
    @Qualifier("cacheCardService")
    ICacheCardService cacheCardService;

    @Autowired
    @Qualifier("savedCardService")
    ISavedCardService savedCardService;

    @Autowired
    ICashierCacheService cashierCacheServiceImpl;

    @Autowired
    TheiaCashierMapper theiaCashierMapper;

    @Autowired
    TheiaCashierUtil theiaCashierUtil;

    @Autowired
    PaymentRequestValidation paymentRequestValidation;

    @Autowired
    TheiaPromoUtil theiaPromoUtil;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private PaymentOTPService paymentOTPUtil;

    @Autowired
    private SubscriptionUtil subscriptionUtil;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    @Qualifier("commonFlowHelper")
    WorkFlowRequestCreationHelper workFlowRequestCreationHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    WorkFlowHelper workFlowHelper;

    @Autowired
    RiskExtendedInfoUtil riskExtendedInfoUtil;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private RouterUtil routerUtil;

    /**
     * @param httpServletRequest
     * @return
     * @throws CashierCheckedException
     * @throws PaytmValidationException
     */
    public CashierRequest prepareCashierRequest(final HttpServletRequest httpServletRequest)
            throws CashierCheckedException, PaytmValidationException {
        String cacheCard = httpServletRequest.getParameter("cacheCardToken");
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(httpServletRequest);
        LOGGER.debug("TransactionInfo : {}", txnInfo);

        boolean linkBasedPaymentRequest = false;
        if (TheiaConstant.RequestTypes.LINK_BASED_PAYMENT.equalsIgnoreCase(txnInfo.getRequestType())
                || TheiaConstant.RequestTypes.LINK_BASED_PAYMENT_INVOICE.equalsIgnoreCase(txnInfo.getRequestType())) {
            linkBasedPaymentRequest = true;
        }

        if (!StringUtils.isBlank(cacheCard)) {
            StringBuilder key = new StringBuilder(CashierConstant.RISK_RETRY_KEY).append(cacheCard);
            CashierRequest cashierRequest = (CashierRequest) theiaTransactionalRedisUtil.get(key.toString());
            cashierRequest.setIsProcessed(false);
            cashierRequest.setRequestId(RequestIdGenerator.generateRequestId());

            txnInfo.setRiskAllowed(false);
            LOGGER.info("Fee has been applied, successfully on credit_card/user_id");
            return cashierRequest;
        }
        txnInfo.setRiskAllowed(true);

        Double walletBalance = 0D;

        final TheiaPaymentRequest theiaPaymentRequest = new TheiaPaymentRequest(httpServletRequest);
        LOGGER.info("TheiaPaymentRequest : {} ", theiaPaymentRequest);

        final TransactionConfig txnConfig = sessionDataService.getTxnConfigFromSession(httpServletRequest);
        MerchantInfo merchantInfo = sessionDataService.getMerchantInfoFromSession(httpServletRequest);
        final LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(httpServletRequest);
        final WalletInfo walletInfo = sessionDataService.getWalletInfoFromSession(httpServletRequest);

        if (loginInfo == null || loginInfo.getUser() == null) {
            LOGGER.warn("No user associated with this transaction");
        }

        final EntityPaymentOptionsTO entityPaymentOptions = sessionDataService
                .getEntityPaymentOptions(httpServletRequest);
        final PaymentRequestBean requestData = new PaymentRequestBean(httpServletRequest);
        EnvInfoRequestBean envInfo = sessionDataService.getEnvInfoRequestBean(httpServletRequest);
        if (StringUtils.isBlank(envInfo.getTokenId())
                && ((envInfo.getExtendInfo() == null) || !envInfo.getExtendInfo().containsKey(
                        TheiaConstant.ExtraConstants.DEVICE_ID))) {
            envInfo = EnvInfoUtil.fetchEnvInfo(httpServletRequest);
        }
        final CardInfo cardInfo = sessionDataService.getCardInfoFromSession(httpServletRequest);
        final ExtendedInfoRequestBean extendedInfoRequestBean = sessionDataService
                .geExtendedInfoRequestBean(httpServletRequest);
        // Calling velocity service
        if (isNotExemptedPayMode(txnConfig, theiaPaymentRequest.getPaymentMode(), theiaPaymentRequest.getBankCode(),
                requestData)) {

            LOGGER.info("updating merchant velocity for default for mid = {} , txnAmount = {} ", merchantInfo.getMid(),
                    txnInfo.getTxnAmount());
            workFlowHelper.validateAndUpdateMerchantVelocity(merchantInfo.getMid(), txnInfo.getTxnAmount(),
                    extendedInfoRequestBean, false);
        }

        /* Need to store original value for fetching the cashier workflow */
        final String txnMode = theiaPaymentRequest.getTxnMode();
        resetTxnModeAndPaymentMode(theiaPaymentRequest, cardInfo);

        if (txnConfig.getSubsTypes() != null) {
            validateRequestForSubscription(theiaPaymentRequest, txnConfig, txnInfo, cardInfo);
            if (txnInfo.isZeroRupeesSubscription()
                    && (StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId()) || SubsTypes.PPI_ONLY
                            .equals(txnConfig.getSubsTypes()))) {
                processAndSetResponse(merchantInfo, txnConfig, loginInfo, extendedInfoRequestBean, txnInfo,
                        theiaPaymentRequest, cardInfo);
                return new CashierRequestBuilder(txnInfo.getTxnId(), true).build();
            }
        }

        paymentOTPUtil.validateIfPaymentOTP(requestData);

        final PromoCodeResponse applyPromoCodeResponse = theiaPromoUtil.applyPromocode(theiaPaymentRequest,
                merchantInfo, txnInfo, loginInfo);

        LOGGER.debug("PromoCodeResponse : {} ", applyPromoCodeResponse);

        if ((walletInfo != null) && (walletInfo.getWalletBalance() != null) && walletInfo.isWalletEnabled()) {
            walletBalance = walletInfo.getWalletBalance();
        }

        CashierWorkflow cashierWorkflow = theiaCashierMapper.getCashierWorkflow(txnInfo, theiaPaymentRequest,
                entityPaymentOptions);

        LOGGER.debug("CashierWork : {} ", cashierWorkflow);

        final PaymentInfo paymentInfo = theiaCashierUtil.computeRequestTypeAndAmount(theiaPaymentRequest,
                walletBalance, txnConfig, txnInfo);

        if (PaymentType.ONLY_WALLET == paymentInfo.getPaymentType()) {
            cashierWorkflow = CashierWorkflow.WALLET;
        }
        LOGGER.debug("PaymentInfo : {} ", paymentInfo);

        final Map<String, String> extendedInfo = ExtendedInfoUtil.selectExtendedInfo(theiaPaymentRequest, merchantInfo,
                txnInfo, paymentInfo, cashierWorkflow, txnConfig, loginInfo, applyPromoCodeResponse,
                extendedInfoRequestBean);

        Routes route = routerUtil.getRoute(extendedInfoRequestBean.getPaytmMerchantId(),
                extendedInfoRequestBean.getMerchantTransId(), "cashierPay");
        extendedInfo.put(ROUTE, Objects.nonNull(route) ? route.toString() : "");

        final Map<String, String> riskExtendedInfo = createRiskExtendedInfo(loginInfo, txnInfo, paymentInfo);

        if (merchantInfo.isMerchantRetryEnabled()) {
            setRetryDataInExtendeInfo(extendedInfo, txnInfo, theiaPaymentRequest);
        } else {
            LOGGER.debug("Retry is not enabled for the merchant id : {}", merchantInfo.getInternalMid());
        }

        final OAuthUserInfo userInfo = loginInfo != null ? loginInfo.getUser() : null;

        DigitalCreditRequest digitalCreditRequest = buildDigitalCreditRequest(userInfo, httpServletRequest, requestData);

        BinDetail binDetail = null;
        if (StringUtils.isNotBlank(theiaPaymentRequest.getCardNo())) {
            binDetail = cardUtils.fetchBinDetails(theiaPaymentRequest.getCardNo().substring(0, 6));
        }
        final ValidationRequest validationRequest = theiaCashierMapper.prepareValidationRequest(theiaPaymentRequest,
                merchantInfo, cashierWorkflow, binDetail);

        PaymentsBankRequest paymentsBankRequest = buildPaymentsBankRequest(userInfo, httpServletRequest, requestData,
                validationRequest);

        final CashierMerchant cashierMerchant = theiaCashierMapper.prepareCashierMerchant(theiaPaymentRequest,
                txnConfig, merchantInfo);

        final UPIPushRequest upiPushRequest = buildUPIPushRequest(httpServletRequest, theiaPaymentRequest, userInfo,
                txnInfo, entityPaymentOptions);

        // Setting Vpa in case user chooses a savedVpa for payment
        if (theiaPaymentRequest.getTxnMode() != null && theiaPaymentRequest.getTxnMode().equals("UPI")) {
            if (theiaPaymentRequest.getTxnMde() != null && theiaPaymentRequest.getTxnMde().equals("SC")) {
                setVpa(httpServletRequest, theiaPaymentRequest, userInfo);
            }
            riskExtendedInfo.put(TheiaConstant.ExtendedInfoPay.VPA, theiaPaymentRequest.getVpa());
        }

        final PaymentRequest paymentRequest = theiaCashierMapper.preparePaymentRequest(theiaPaymentRequest,
                paymentInfo, txnInfo, txnConfig, userInfo, extendedInfo, merchantInfo, entityPaymentOptions, envInfo,
                riskExtendedInfo, digitalCreditRequest, upiPushRequest, binDetail);

        CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder(txnInfo.getTxnId(),
                paymentRequest.getRequestId(), cashierWorkflow).setPaymentRequest(paymentRequest)
                .setCashierMerchant(cashierMerchant).setValidationRequest(validationRequest)
                .setDigitalCreditRequest(digitalCreditRequest).setPaymentsBankRequest(paymentsBankRequest)
                .setLinkBasedPaymentRequest(linkBasedPaymentRequest).setUpiPushRequest(upiPushRequest);

        merchantInfo = sessionDataService.getMerchantInfoFromSession(httpServletRequest);

        if (merchantInfo != null) {
            cashierRequestBuilder.setMerchantName(merchantInfo.getMerchantName()).setMerchantImage(
                    merchantInfo.getMerchantImage());
        }

        paymentRequestValidation.prepareCardRequest(theiaPaymentRequest, entityPaymentOptions, cashierWorkflow,
                cashierRequestBuilder, cardInfo, paymentRequest);

        cashierRequestBuilder.OnTheFlyKYCRequired(txnConfig.isOnTheFlyKYCRequired());

        final CashierRequest cashierRequest = cashierRequestBuilder.build();
        LOGGER.debug("CashierRequest : {}", cashierRequest);

        if (txnConfig.isOnTheFlyKYCRequired()) {

            if (loginInfo == null || loginInfo.getUser() == null) {
                LOGGER.error("KYC flow wont be validate as no user available");
                return cashierRequest;
            }

            String kycFlowKey = "KYC_FLOW_" + cashierRequest.getAcquirementId();
            String kycRetryCount = "KYC_RETRY_COUNT_" + cashierRequest.getAcquirementId();

            theiaTransactionalRedisUtil.set(kycFlowKey, cashierRequest,
                    Long.valueOf(ConfigurationUtil.getProperty("kyc.timeout", "600")));

            try {
                httpServletRequest.setAttribute(KYC_TXN_ID, cashierRequest.getAcquirementId());
                httpServletRequest.setAttribute(KYC_MID, txnInfo.getMid());
                httpServletRequest.setAttribute(KYC_ORDER_ID, txnInfo.getOrderId());
                httpServletRequest.setAttribute(KYC_USER_ID, loginInfo.getUser().getUserID());
                httpServletRequest.setAttribute(KYC_RETRY_COUNT, "0");

                theiaTransactionalRedisUtil.set(kycRetryCount, 0);

            } catch (Exception ex) {
                LOGGER.error("Error while creating the data", ex);
            }

        }

        return cashierRequest;
    }

    public static boolean isNotExemptedPayMode(TransactionConfig txnConfig, String payMode, String bankCode,
            PaymentRequestBean paymentRequestBean) {
        try {
            // not handling for hybrid merchants.as DIY merchants are not gonna
            // be of hybrid type.
            // its mainly for offus merchants
            if (txnConfig != null
                    && (txnConfig.isHybridAllowed() || (txnConfig.isAddMoneyFlag() && "1"
                            .equalsIgnoreCase(paymentRequestBean.isAddMoney())))) {
                LOGGER.info("isExemptedPayMode  isHybridAllowed = {},isAddAndPayAllowed = {} isAddMoney = {} ",
                        txnConfig.isHybridAllowed(), txnConfig.isAddAndPayAllowed(), paymentRequestBean.isAddMoney());
                return true;
            }
            LOGGER.info("isExemptedPayMode  payMode = {},bankCoe= {}", payMode, bankCode);
            return !(PaymentTypeIdEnum.UPI.value.equals(payMode) || (PaymentTypeIdEnum.NB.value.equals(payMode) && EPayMethod.PPBL
                    .getOldName().equals(bankCode)));
        } catch (Exception e) {
            LOGGER.warn("something went wrong while check is exempted pay mode");
        }
        return true;
    }

    private void validateRequestForSubscription(TheiaPaymentRequest theiaPaymentRequest, TransactionConfig txnConfig,
            TransactionInfo txnInfo, CardInfo cardInfo) throws PaytmValidationException {
        boolean isSavedCardRequest = StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId());
        boolean isValidRequest = false;
        BinDetail binDetail = null;

        switch (txnConfig.getSubsTypes()) {
        case DC_ONLY:
        case CC_ONLY:
            binDetail = getBinDetails(theiaPaymentRequest, cardInfo, isSavedCardRequest, false);
            isValidRequest = subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail,
                    getSubsPaymentMode(txnConfig.getSubsTypes()));
            break;

        case NORMAL:
            binDetail = getBinDetails(theiaPaymentRequest, cardInfo, isSavedCardRequest, true);
            isValidRequest = subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail,
                    getSubsPaymentMode(txnConfig.getSubsTypes()));
            break;
        case PPI_ONLY:
            isValidRequest = true;
            break;
        case PPBL_ONLY:
            isValidRequest = true;
            break;
        }
        if (!isValidRequest) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_SUBS_PAYMENT_DETAILS);
        }
    }

    private BinDetail getBinDetails(TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo,
            boolean isSavedCardRequest, boolean isAddAndPay) throws PaytmValidationException {

        String binNumber = getBinNumber(theiaPaymentRequest, cardInfo, isSavedCardRequest, isAddAndPay);

        return cardUtils.fetchBinDetails(binNumber);
    }

    private String getBinNumber(TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo, boolean isSavedCardRequest,
            boolean isAddAndPay) {
        String binNumber = null;
        if (isSavedCardRequest) {
            String savedCardId = theiaPaymentRequest.getSavedCardId();
            SavedCardInfo cardBeingUsed = isAddAndPay ? cardInfo.getAddAnPaySavedCardMap().get(savedCardId) : cardInfo
                    .getSavedCardMap().get(savedCardId);
            binNumber = String.valueOf(cardBeingUsed.getFirstSixDigit());
        } else {
            binNumber = theiaPaymentRequest.getCardNo().substring(0, 6);
        }
        return binNumber;
    }

    private void resetTxnModeAndPaymentMode(TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo)
            throws PaytmValidationException {
        String txnMode = theiaPaymentRequest.getTxnMode();
        if (PayMethod.CREDIT_CARD.getOldName().equals(txnMode) || PayMethod.DEBIT_CARD.getOldName().equals(txnMode)) {
            String txnMde = theiaPaymentRequest.getTxnMde();
            if (StringUtils.isNotBlank(txnMde)) {
                resetForSavedCard(theiaPaymentRequest, cardInfo, txnMde);
            } else {
                resetForPlainCard(theiaPaymentRequest);
            }
        }
    }

    private void resetForPlainCard(TheiaPaymentRequest theiaPaymentRequest) throws PaytmValidationException {
        String cardNumberEntered = theiaPaymentRequest.getCardNo();
        if (StringUtils.isBlank(cardNumberEntered) || cardNumberEntered.length() < ExtraConstants.MIN_CARD_LENGTH
                || cardNumberEntered.length() > ExtraConstants.MAX_CARD_LENGTH) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
        }

        String binNumber = cardNumberEntered.substring(0, 6);

        try {

            BinDetail binDetail = cardUtils.fetchBinDetails(binNumber);

            if (null == binDetail) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD,
                        "Unable to fetch bin details for bin number ::" + binNumber);
            }

            if (StringUtils.isNotBlank(binDetail.getCardType())) {
                PayMethod payMethod = PayMethod.valueOf(binDetail.getCardType());
                if (payMethod != null) {
                    LOGGER.info("Resetting txnMode and paymentMode to : {}", payMethod.getOldName());
                    theiaPaymentRequest.setTxnMode(payMethod.getOldName());
                    theiaPaymentRequest.setPaymentMode(payMethod.getOldName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Bin : ", e);
        }
    }

    private void resetForSavedCard(TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo, String txnMde)
            throws PaytmValidationException {
        boolean isAddAndPay = ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag());
        if ("SC".equals(txnMde) && StringUtils.isNotBlank(theiaPaymentRequest.getSavedCardId())) {
            String savedCardId = theiaPaymentRequest.getSavedCardId();

            SavedCardInfo savedCardInfo;

            if (isAddAndPay) {
                savedCardInfo = cardInfo.getAddAnPaySavedCardMap() != null ? cardInfo.getAddAnPaySavedCardMap().get(
                        savedCardId) : null;
            } else {
                savedCardInfo = cardInfo.getSavedCardMap() != null ? cardInfo.getSavedCardMap().get(savedCardId) : null;
            }

            if (savedCardInfo != null && StringUtils.isNotBlank(savedCardInfo.getCardType())) {
                switch (savedCardInfo.getCardType()) {
                case "DEBIT_CARD":
                    txnMde = TransactionMode.DC.getMode();
                    break;
                case "IMPS":
                    txnMde = TransactionMode.IMPS.getMode();
                    break;
                case "CREDIT_CARD":
                    txnMde = TransactionMode.CC.getMode();
                    break;
                case "UPI":
                    txnMde = TransactionMode.UPI.getMode();
                    break;
                default:
                    break;
                }
            }

        }

        if ("SC".equals(txnMde)) {
            LOGGER.info("Received txnMde=SC with empty Saved Card ID.");
            throw new PaytmValidationException(PaytmValidationExceptionType.NO_SAVED_CARD_SELECTED);
        }

        LOGGER.info("Resetting txnMode and paymentMode to : {}", txnMde);
        theiaPaymentRequest.setTxnMode(txnMde);
        theiaPaymentRequest.setPaymentMode(txnMde);
    }

    private void processAndSetResponse(MerchantInfo merchInfo, TransactionConfig txnConfig, LoginInfo loginInfo,
            ExtendedInfoRequestBean extendedInfoRequestBean, TransactionInfo txnInfo,
            TheiaPaymentRequest theiaPaymentRequest, CardInfo cardInfo) throws PaytmValidationException {

        validateSavedCardId(theiaPaymentRequest, txnInfo, txnConfig, cardInfo);
        final ModifySubscriptionRequest modifyRequestBean = new ModifySubscriptionRequest(merchInfo.getMid(),
                txnInfo.getRequestType(), txnInfo.getOrderId(), txnInfo.getCustID(), "0",
                txnInfo.getSubscriptionServiceID(), theiaPaymentRequest.getSavedCardId(), loginInfo.getUser()
                        .getPayerUserID(), loginInfo.getUser().getPayerAccountNumber(), txnInfo.getTxnId(), loginInfo
                        .getUser().getEmailId(), loginInfo.getUser().getMobileNumber());

        SubscriptionResponse response = subscriptionService.activateSubscription(modifyRequestBean);

        if (TransactionStatus.TXN_SUCCESS.equals(response.getStatus())) {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setMid(merchInfo.getMid());
            transactionResponse.setTxnId(txnInfo.getTxnId());
            transactionResponse.setOrderId(txnInfo.getOrderId());
            transactionResponse.setResponseCode(TheiaConstant.ResponseConstants.ResponseCodes.SUCCESS_RESPONSE_CODE);
            transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
            transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnInfo.getTxnAmount()));
            transactionResponse.setCallbackUrl(extendedInfoRequestBean.getCallBackURL());
            transactionResponse.setSubsId(txnInfo.getSubscriptionServiceID());

            if (extendedInfoRequestBean != null) {
                if (StringUtils.isNotBlank(extendedInfoRequestBean.getClientId()))
                    transactionResponse.setClientId(extendedInfoRequestBean.getClientId());
                transactionResponse.setExtraParamsMap(extendedInfoRequestBean.getExtraParamsMap());
            } else {
                transactionResponse.setExtraParamsMap(Collections.emptyMap());
            }

            String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
            sessionDataService.setRedirectPageInSession(theiaPaymentRequest.getRequest(), responsePage);
        } else {
            throw new TheiaServiceException("Could not activate Subscription because : " + response.getRespMsg());
        }

    }

    private void validateSavedCardId(TheiaPaymentRequest theiaPaymentRequest, TransactionInfo txnInfo,
            TransactionConfig txnConfig, CardInfo cardInfo) throws PaytmValidationException {
        String savedCardId = theiaPaymentRequest.getSavedCardId();
        switch (txnConfig.getSubsTypes()) {
        case CC_ONLY:
            SavedCardInfo savedCardInfo = cardInfo.getSavedCardMap().get(savedCardId);
            if (savedCardId == null) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_SAVED_CARD);
            }

            BinDetail binDetail = cardUtils.fetchBinDetails(String.valueOf(savedCardInfo.getFirstSixDigit()));

            SubsPaymentMode subsPaymentMode = getSubsPaymentMode(txnConfig.getSubsTypes());

            if (!subscriptionUtil.isBinBoundToSubscriptionFlow(binDetail, subsPaymentMode)) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_BIN_DETAILS);
            }
            break;
        case DC_ONLY:
        case NORMAL:
        case PPI_ONLY:
            break;
        }
    }

    private SubsPaymentMode getSubsPaymentMode(SubsTypes subsTypes) throws PaytmValidationException {
        switch (subsTypes) {
        case CC_ONLY:
            return SubsPaymentMode.CC;
        case DC_ONLY:
            return SubsPaymentMode.DC;
        case NORMAL:
            return SubsPaymentMode.NORMAL;
        case PPI_ONLY:
            return SubsPaymentMode.PPI;
        }
        throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE);
    }

    /** Fetch retryCount from Cache if exist & increment accordingly. */
    private void setRetryDataInExtendeInfo(final Map<String, String> extendedInfo, final TransactionInfo txnInfo,
            final TheiaPaymentRequest theiaPaymentRequest) {

        // Setting EMI Plan id in extended info
        extendedInfo.put(TheiaConstant.ChannelInfoKeys.EMI_PLANID, theiaPaymentRequest.getEmiPlanID());

        // Setting retry count
        Integer retryCount = getCurrentRetryCount(txnInfo.getTxnId());
        extendedInfo.put(ExtendedInfoPay.RETRY_COUNT, String.valueOf(retryCount));
    }

    public void incrementRetryCount(final String txnId) {
        // Setting retry count
        Integer retryCount = getCurrentRetryCount(txnId);
        cashierCacheServiceImpl.cachePaymentRetryCount(txnId, retryCount);
    }

    private Integer getCurrentRetryCount(final String txnId) {
        Integer retryCount = cashierCacheServiceImpl.getPaymentRetryCountFromCache(txnId);

        if (null != retryCount) {
            retryCount = retryCount + 1;
            cacheCardService.deleteCardDetailsFromCache(txnId);
        } else {
            retryCount = 0;
        }
        return retryCount;
    }

    private DigitalCreditRequest buildDigitalCreditRequest(OAuthUserInfo userInfo,
            HttpServletRequest httpServletRequest, PaymentRequestBean requestData) {
        DigitalCreditRequest digitalCreditRequest = null;
        if (userInfo != null) {
            final DigitalCreditInfo digitalCreditInfo = sessionDataService.getDigitalCreditInfoFromSession(
                    httpServletRequest, false);
            final TransactionInfo transInfo = sessionDataService.getTxnInfoFromSession(httpServletRequest, false);
            if (digitalCreditInfo != null) {
                digitalCreditRequest = new DigitalCreditRequest(digitalCreditInfo.getAccountBalance(),
                        digitalCreditInfo.getExternalAccountNo(), digitalCreditInfo.getLenderId(),
                        requestData.getPassCode(), digitalCreditInfo.getPaymentRetryCount());
                digitalCreditRequest.setUserMobile(userInfo.getMobileNumber());
                digitalCreditRequest.setClientId(configurationDataService
                        .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID));
                digitalCreditRequest.setClientSecret(configurationDataService
                        .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY));
                digitalCreditRequest.setPaytmToken(userInfo.getPaytmToken());
                digitalCreditRequest.setPasscodeRequired(digitalCreditInfo.isPasscodeRequired());
                if (transInfo != null && StringUtils.isNotBlank(transInfo.getSsoToken())) {
                    digitalCreditRequest.setSsoToken(transInfo.getSsoToken());
                }
            }
        }
        return digitalCreditRequest;
    }

    private PaymentsBankRequest buildPaymentsBankRequest(OAuthUserInfo userInfo, HttpServletRequest httpServletRequest,
            PaymentRequestBean requestData, ValidationRequest validationRequest) {
        PaymentsBankRequest paymentsBankRequest = null;
        if (userInfo != null) {
            final SavingsAccountInfo savingsAccountInfo = sessionDataService.getSavingsAccountInfoFromSession(
                    httpServletRequest, false);
            if (savingsAccountInfo != null) {
                if (validationRequest != null && TransactionMode.NB.getMode().equals(validationRequest.getTxnMode())
                        && StringUtils.isNotBlank(validationRequest.getSelectedBank())
                        && TransactionMode.PPBL.getMode().equals(validationRequest.getSelectedBank())) {
                    paymentsBankRequest = new PaymentsBankRequest(savingsAccountInfo.getAccountNumber(),
                            savingsAccountInfo.getEffectiveBalance(), savingsAccountInfo.getPaymentRetryCount());
                    paymentsBankRequest.setPassCode(requestData.getPassCode());
                    paymentsBankRequest.setUserMobile(userInfo.getMobileNumber());
                    paymentsBankRequest.setClientId(configurationDataService
                            .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID));
                    paymentsBankRequest.setClientSecret(configurationDataService
                            .getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY));
                    paymentsBankRequest.setBankCode(savingsAccountInfo.getPaymentsBankCode());
                    paymentsBankRequest.setPaytmToken(userInfo.getPaytmToken());
                    paymentsBankRequest.setAccountRefId(savingsAccountInfo.getAccountRefId());
                }
            }
        }
        return paymentsBankRequest;
    }

    private void setVpa(HttpServletRequest httpServletRequest, TheiaPaymentRequest theiaPaymentRequest,
            OAuthUserInfo userInfo) throws PaytmValidationException {
        String savedCardId = theiaPaymentRequest.getSavedCardId();

        String userId = null;
        if (userInfo != null) {
            userId = userInfo.getUserID();
        }

        boolean cardFound = false;
        String vpa = "";
        CardInfo cardInfo = sessionDataService.getCardInfoFromSession(httpServletRequest);

        if (cardInfo != null && MapUtils.isNotEmpty(cardInfo.getSavedCardMap())) {

            SavedCardInfo savedCardInfo = cardInfo.getSavedCardMap().get(savedCardId);

            if (savedCardInfo != null) {
                cardFound = true;
                vpa = savedCardInfo.getCardNumber();
            }
        }

        if (!cardFound) {
            LOGGER.info("Card not found in session so fetching the details from SavedCardService");
            SavedCardVO savedCardVO = cashierUtilService.getSavedCardDetails(Long.parseLong(savedCardId), userId);
            if (savedCardVO == null) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
            }
            vpa = savedCardVO.getCardNumber();
        }

        theiaPaymentRequest.setVpa(vpa);
    }

    private UPIPushRequest buildUPIPushRequest(HttpServletRequest httpServletRequest,
            TheiaPaymentRequest theiaPaymentRequest, OAuthUserInfo userInfo, TransactionInfo txnInfo,
            EntityPaymentOptionsTO entityPaymentOptions) {
        UPIPushRequest upiPushRequest = new UPIPushRequest();
        SarvatraVPAMapInfo sarvatraSessionData = sessionDataService.getSarvatraVPAInfoFromSession(httpServletRequest,
                false);
        if (TheiaConstant.BasicPayOption.UPI.equals(theiaPaymentRequest.getTxnMode())
                && StringUtils.isNotBlank(theiaPaymentRequest.getMpin())
                && StringUtils.isNotBlank(theiaPaymentRequest.getDeviceId()) && null != sarvatraSessionData) {

            if (StringUtils.isNotBlank(theiaPaymentRequest.getVpa())
                    && null != sarvatraSessionData.getSarvatraVpaMapInfo().get(theiaPaymentRequest.getVpa())) {
                upiPushRequest.setUpiPushTxn(true);
                // Hack to support EXPRESS flow
                if ((ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptions
                        .isAddUpiPushExpressEnabled())
                        || (!ExtraConstants.ADD_MONEY_FLAG_VALUE.equals(theiaPaymentRequest.getAddMoneyFlag()) && entityPaymentOptions
                                .isUpiPushExpressEnabled())) {
                    upiPushRequest.setUpiPushExpressSupported(true);
                }
                upiPushRequest.setDeviceId(theiaPaymentRequest.getDeviceId());
                upiPushRequest.setMobile(userInfo.getMobileNumber());
                upiPushRequest.setMpin(theiaPaymentRequest.getMpin());
                upiPushRequest.setSeqNo(theiaPaymentRequest.getSequenceNumber());
                upiPushRequest.setOrderId(txnInfo.getOrderId());
                upiPushRequest.setAppId(theiaPaymentRequest.getAppId());
                List<SarvatraVpaDetails> sarvatraVpaDetails = sarvatraSessionData.getUserProfileSarvatra()
                        .getResponse().getVpaDetails();
                for (SarvatraVpaDetails vpaDetailIterartor : sarvatraVpaDetails) {
                    if (theiaPaymentRequest.getVpa().equals(vpaDetailIterartor.getName())) {
                        upiPushRequest.setSarvatraVpaDetails(vpaDetailIterartor);
                    }
                }
            }
            new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_UPIPUSH);
        }
        return upiPushRequest;
    }

    public boolean prepareKycRetryData(HttpServletRequest httpServletRequest) {
        String kycErrMsg = ConfigurationUtil.getProperty(KYC_ERROR_MESSAGE,
                "We could not validate your ID. Please try again with a different document ID");
        LOGGER.info("Checking for the KYC retry");

        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(httpServletRequest);
        final TransactionConfig txnConfig = sessionDataService.getTxnConfigFromSession(httpServletRequest);
        final LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(httpServletRequest);

        String kycRetryCount = "KYC_RETRY_COUNT_" + txnInfo.getTxnId();

        int count = (int) theiaTransactionalRedisUtil.get(kycRetryCount);

        if (count >= 3) {
            LOGGER.info("Retry limit reached for KYC on boarding flow");
            return false;
        }

        count++;

        if (txnConfig.isOnTheFlyKYCRequired()) {
            try {
                httpServletRequest.setAttribute(KYC_TXN_ID, txnInfo.getTxnId());
                httpServletRequest.setAttribute(KYC_MID, txnInfo.getMid());
                httpServletRequest.setAttribute(KYC_ORDER_ID, txnInfo.getOrderId());
                httpServletRequest.setAttribute(KYC_USER_ID, loginInfo.getUser().getUserID());
                httpServletRequest.setAttribute(KYC_RETRY_COUNT, count);
                httpServletRequest.setAttribute(KYC_ERROR_MESSAGE, kycErrMsg);

                theiaTransactionalRedisUtil.set(kycRetryCount, count);
            } catch (Exception ex) {
                LOGGER.error("Error while creating the data", ex);
            }
        }

        return true;
    }

    private Map<String, String> createRiskExtendedInfo(LoginInfo loginInfo, TransactionInfo txnInfo,
            PaymentInfo paymentInfo) {
        Map<String, String> riskExtendedInfo = riskExtendedInfoUtil.selectRiskExtendedInfo(loginInfo);

        if (loginInfo != null && loginInfo.getUser() != null) {
            if (workFlowRequestCreationHelper.isAddMoneyToWallet(txnInfo.getMid(), txnInfo.getRequestType())) {
                workFlowRequestCreationHelper.updateRiskExtendInfoForAddMoney(riskExtendedInfo, loginInfo.getUser()
                        .getUserTypes(), txnInfo.getOrderId(), paymentInfo.getServiceAmount().toString(), loginInfo
                        .getUser().getUserID(), txnInfo.getTrustFactor());
            }
        }

        // Set merchant-alipay-user-id in risk-extend-info
        if (txnInfo != null) {
            riskExtendedInfoUtil.setMerchantUserIdInRiskExtendInfo(txnInfo.getMid(), riskExtendedInfo);
        }
        return riskExtendedInfo;
    }
}