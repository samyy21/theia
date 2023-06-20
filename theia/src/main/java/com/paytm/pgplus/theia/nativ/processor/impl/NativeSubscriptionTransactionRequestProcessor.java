package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.cache.model.IfscCodeDetails;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IIfscDetailsService;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.models.MandateAccountDetails;
import com.paytm.pgplus.models.PaymentMode;
import com.paytm.pgplus.payloadvault.subscription.response.BankMandateInfo;
import com.paytm.pgplus.payloadvault.subscription.response.SubscriptionUpiInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.response.*;
import com.paytm.pgplus.subscriptionClient.model.request.FreshSubscriptionRequest;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
import com.paytm.pgplus.theia.nativ.model.token.TrxInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.subscription.impl.NativeSubscriptionHelperImpl;
import com.paytm.pgplus.theia.nativ.utils.*;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SubscriptionServiceException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_SUCCESS_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.THEIA_ENABLE_CC_DC_PAYMODES_SUBSCRIPTION;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Created by himanshu3.garg on 31/1/19.
 */

@Service("nativeSubscriptionTransactionRequestProcessor")
public class NativeSubscriptionTransactionRequestProcessor
        extends
        AbstractRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse, SubscriptionTransactionRequest, TrxInfoResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSubscriptionTransactionRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger
            .create(NativeSubscriptionTransactionRequestProcessor.class);

    @Autowired
    @Qualifier("nativePaymentService")
    private INativePaymentService nativePaymentService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    @Qualifier("subscriptionNativeValidationService")
    private ISusbcriptionNativeValidationService susbcriptionNativeValidationService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeInitiateUtil")
    private NativeInitiateUtil nativeInitiateUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private IIfscDetailsService ifscDetailsService;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private WorkFlowHelper workFlowHelper;

    private static final int DEFAULT_EXPIRY_TIME_IN_MIN = 30;

    /**
     * This method is use to validate/enrich the request before processing the
     * request. It can be use as a hook before processing any request.
     *
     * @param request
     */
    @Override
    protected SubscriptionTransactionRequest preProcess(SubscriptionTransactionRequest request) {
        if (request.isSkipSubsContractValidation()) {
            nativeValidationService.validate(request);
            return request;
        }

        if (StringUtils.isEmpty(request.getBody().getRequestType())
                || ERequestType.SUBSCRIBE.getType().equalsIgnoreCase(request.getBody().getRequestType())) {
            request.getBody().setRequestType(ERequestType.NATIVE_SUBSCRIPTION.getType());
        }

        EventUtils.pushTheiaEvents(
                EventNameEnum.ORDER_INITIATED,
                new ImmutablePair<>("REQUEST_TYPE", String
                        .valueOf(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION)));
        enrich(request);
        validate(request);

        if (aoaUtils.isAOAMerchant(request.getBody().getMid())
                && ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMid(),
                        TheiaConstant.FF4J.THEIA_AOASUBSCRIPTIONS_MIDLISTFORAOATOPGCONVERSION, false)) {
            String paymode = ConfigurationUtil
                    .getTheiaProperty(TheiaConstant.ExtraConstants.SUBSCRIPTION_PAYMODE_FOR_AOA_TO_PG_CONVERSION);
            if (Arrays.asList(paymode.split(",")).contains(request.getBody().getSubscriptionPaymentMode())) {
                String aoaMid = request.getBody().getMid();
                request.getBody().setMid(aoaUtils.getPgMidForAoaMid(request.getBody().getMid()));
                LOGGER.info("Changed AOA mid {} ,to pg mid {} ", aoaMid, request.getBody().getMid());
                request.getBody().setAoaSubsOnPgMid(true);
            }
        }
        disableCcDcPaymodesIfEligible(request);

        return request;
    }

    private void disableCcDcIfPaymodeIsNull(SubscriptionTransactionRequest request,
            List<PaymentMode> disablePayModesList) {
        if (Integer.valueOf(NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD) < Integer
                .valueOf(request.getBody().getSubscriptionGraceDays())) {
            disablePayModesList.add(new PaymentMode(EPayMethod.DEBIT_CARD.getMethod()));
        }
        if (Integer.valueOf(NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD) < Integer
                .valueOf(request.getBody().getSubscriptionGraceDays())) {
            disablePayModesList.add(new PaymentMode(EPayMethod.CREDIT_CARD.getMethod()));
        }
    }

    private void disableCcDcPaymodesIfEligible(SubscriptionTransactionRequest request) {
        PaymentMode debitCardMode = new PaymentMode();
        PaymentMode creditCardMode = new PaymentMode();
        debitCardMode.setMode(EPayMethod.DEBIT_CARD.getMethod());
        creditCardMode.setMode(EPayMethod.CREDIT_CARD.getMethod());
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.Native.MID, request.getBody().getMid());
        List<PaymentMode> disablePayModesList = null;

        if (request.getBody().getDisablePaymentMode() != null && !request.getBody().getDisablePaymentMode().isEmpty()) {
            disablePayModesList = request.getBody().getDisablePaymentMode();
        } else {
            disablePayModesList = new ArrayList<>();
        }
        if (!iPgpFf4jClient.checkWithdefault(THEIA_ENABLE_CC_DC_PAYMODES_SUBSCRIPTION, context, false)
                || disableCcDcForSubsCriteria(request)) {

            disablePayModesList.add(debitCardMode);
            disablePayModesList.add(creditCardMode);
        } else {
            if (SubsPaymentMode.UNKNOWN.name().equals(request.getBody().getSubscriptionPaymentMode())) {
                disableCcDcIfPaymodeIsNull(request, disablePayModesList);
            } else {
                if (Integer.valueOf(NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_DEBIT_CARD) < Integer
                        .valueOf(request.getBody().getSubscriptionGraceDays())) {
                    disablePayModesList.add(debitCardMode);
                }
                if (Integer.valueOf(NativeSubscriptionHelperImpl.SUBSCRIPTION_MAXIMUM_GRACE_DAYS_CREDIT_CARD) < Integer
                        .valueOf(request.getBody().getSubscriptionGraceDays())) {
                    disablePayModesList.add(creditCardMode);
                }
            }
        }
        request.getBody().setDisablePaymentMode(disablePayModesList);
    }

    /**
     * This method is used to process the request which includes request
     * validation, processing, response generator.
     *
     * @param request
     * @param subscriptionTransactionRequest
     * @return
     * @throws Exception
     */
    @Override
    protected TrxInfoResponse onProcess(SubscriptionTransactionRequest request,
            SubscriptionTransactionRequest subscriptionTransactionRequest) throws Exception {
        boolean cacheSubsDetail = false;
        SubscriptionResponse subscriptionResponse = null;
        TrxInfoResponse trxInfoResponse = new TrxInfoResponse();
        setChannelCodeFromIFSC(subscriptionTransactionRequest.getBody().getMandateAccountDetails());

        if ((request.getBody().getSubscriptionResponse() != null)) {
            com.paytm.pgplus.response.SubscriptionResponse subscriptionResponseCommon = request.getBody()
                    .getSubscriptionResponse();
            subscriptionResponse = getSubscriptionResponse(subscriptionResponseCommon);
            prepareExtendInfoForNotify(request, subscriptionResponse);
        } else {
            subscriptionResponse = this.createSubscription(request);
        }

        if (!SUBSCRIPTION_SUCCESS_CODE.equalsIgnoreCase(subscriptionResponse.getRespCode())) {
            LOGGER.error("Unable to create subscription");
            throw new SubscriptionServiceException(new ResultInfo(subscriptionResponse.getStatus().getName(),
                    subscriptionResponse.getRespCode(), subscriptionResponse.getRespMsg()));
        } else {
            trxInfoResponse.setSubscriptionId(subscriptionResponse.getSubscriptionId());
            cacheSubsDetail = true;
        }

        InitiateTokenBody initiateTokenBody = nativePaymentService.initiateTransaction(request);
        if (initiateTokenBody.isIdempotent()) {
            if (!nativeSessionUtil.isIdempotentRequest(request.getBody(), initiateTokenBody.getTxnId())) {
                // Fail for Modified request if not idempotent
                throw RequestValidationException.getException(ResultCode.REPEAT_REQUEST_INCONSISTENT);
            }
        }

        if (cacheSubsDetail) {
            int expiryTimeInSeconds = Integer.parseInt(ConfigurationUtil.getProperty(
                    "subscriptionDetailCacheExpiryTimeInMinutes", String.valueOf(DEFAULT_EXPIRY_TIME_IN_MIN))) * 60;
            StringBuilder key = new StringBuilder(request.getBody().getRequestType()).append(initiateTokenBody
                    .getTxnId());
            boolean allowUnverfiedAccount = false;
            if (StringUtils.isNotBlank(request.getBody().getAllowUnverifiedAccount())) {
                allowUnverfiedAccount = Boolean.valueOf(request.getBody().getAllowUnverifiedAccount());
            }
            String accountNumber = request.getBody().getAccountNumber();
            String requestType = request.getBody().getRequestType();
            String subscriptionMaxAmount = request.getBody().getSubscriptionMaxAmount();
            String renewalAmount = request.getBody().getRenewalAmount();
            String subscriptionAmountType = request.getBody().getSubscriptionAmountType();

            theiaTransactionalRedisUtil.set(key.toString(), subscriptionResponse, expiryTimeInSeconds);
            theiaTransactionalRedisUtil.set(
                    workFlowHelper.getSubscriptionKey(subscriptionResponse.getSubscriptionId()), key.toString() + "||"
                            + requestType + "||" + allowUnverfiedAccount + "||" + accountNumber + "||"
                            + subscriptionMaxAmount + "||" + renewalAmount + "||" + subscriptionAmountType,
                    expiryTimeInSeconds);

            if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(request.getBody().getRequestType())
                    && merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(request.getBody().getMid())
                    && request.isSuperGwHit() && request.getBody().isAuthorized()) {
                nativeSessionUtil.markSubscriptionAuthorized(initiateTokenBody.getTxnId());
            }
        }
        Map<String, Object> context = new HashMap<>();
        context.put(TheiaConstant.RequestParams.Native.MID, request.getBody().getMid());
        HttpServletRequest serverRequest = EnvInfoUtil.httpServletRequest();
        if (StringUtils.equals(TheiaConstant.RequestParams.SUBSCRIPTION_INITIATE_TRANSACTION_URL,
                serverRequest.getRequestURI())
                && !request.isSkipOrderCreateInSubs()
                && !iPgpFf4jClient.checkWithdefault(
                        TheiaConstant.RequestParams.BLACKLIST_CREATE_ORDER_IN_NATIVE_SUBSCRIPTION_INITIATE, context,
                        true)
                && iPgpFf4jClient.checkWithdefault(
                        TheiaConstant.RequestParams.CREATE_ORDER_IN_NATIVE_SUBSCRIPTION_CREATION, context, false)) {
            try {
                processCreateOrder(subscriptionResponse, request, initiateTokenBody, trxInfoResponse);
            } catch (Exception ex) {
                LOGGER.error("Exception occured while creating order in Native initiate subscription request", ex);
                StringBuilder key = new StringBuilder(request.getBody().getRequestType()).append(initiateTokenBody
                        .getTxnId());
                theiaTransactionalRedisUtil.del(key.toString());
                throw ex;
            }
        }
        trxInfoResponse.setTxntoken(initiateTokenBody.getTxnId());
        trxInfoResponse.setIdempotent(initiateTokenBody.isIdempotent());
        return trxInfoResponse;
    }

    private void setChannelCodeFromIFSC(MandateAccountDetails mandateAccountDetails) throws TheiaDataMappingException {
        if (mandateAccountDetails != null && StringUtils.isBlank(mandateAccountDetails.getBankCode())
                && StringUtils.isNotBlank(mandateAccountDetails.getIfsc())) {
            String ifscCode = mandateAccountDetails.getIfsc();
            try {
                LOGGER.info("Calling mapping to get bank code for IFSC code: " + ifscCode);
                IfscCodeDetails ifscDetails = ifscDetailsService.getIfscCodeDetails(ifscCode);
                EXT_LOGGER.customInfo("Mapping response - IfscCodeDetails :: {}", ifscDetails);
                if (ifscDetails != null && StringUtils.isNotBlank(ifscDetails.getBankCode())) {
                    mandateAccountDetails.setBankCode(ifscDetails.getBankCode());
                } else {
                    LOGGER.warn("Bank code not found from mapping for IFSC code " + ifscCode);
                }
            } catch (MappingServiceClientException e) {
                LOGGER.error("Mapping Error while getting bank details for IFSC code {}, error: {}", ifscCode, e);
            }
        }
    }

    private SubscriptionResponse getSubscriptionResponse(
            com.paytm.pgplus.response.SubscriptionResponse subscriptionResponse) {
        SubscriptionResponse subscriptionResponseClient = new SubscriptionResponse();

        subscriptionResponseClient.setMid(subscriptionResponse.getMid());
        subscriptionResponseClient.setTxnId(subscriptionResponse.getTxnId());
        subscriptionResponseClient.setOrderId(subscriptionResponse.getOrderId());
        subscriptionResponseClient.setTxnAmount(subscriptionResponse.getTxnAmount());
        subscriptionResponseClient.setTxnDate(subscriptionResponse.getTxnDate());
        subscriptionResponseClient.setRespCode(subscriptionResponse.getRespCode());
        subscriptionResponseClient.setRespMsg(subscriptionResponse.getRespMsg());
        subscriptionResponseClient.setStatus(subscriptionResponse.getStatus());
        subscriptionResponseClient.setSubscriptionId(subscriptionResponse.getSubscriptionId());
        subscriptionResponseClient.setPaymentMode(subscriptionResponse.getPaymentMode());
        subscriptionResponseClient.setPayerUserID(subscriptionResponse.getPayerUserID());
        subscriptionResponseClient.setPayerAccountNumber(subscriptionResponse.getPayerAccountNumber());
        subscriptionResponseClient.setSavedCardID(subscriptionResponse.getSavedCardID());
        subscriptionResponseClient.setSubscriptionExpiryDate(subscriptionResponse.getSubscriptionExpiryDate());
        subscriptionResponseClient.setWebsite(subscriptionResponse.getWebsite());
        subscriptionResponseClient.setIndustryType(subscriptionResponse.getIndustryType());
        subscriptionResponseClient.setSubsFreq(subscriptionResponse.getSubsFreq());
        subscriptionResponseClient.setSubsFreqUnit(subscriptionResponse.getSubsFreqUnit());
        subscriptionResponseClient.setUserEmail(subscriptionResponse.getUserEmail());
        subscriptionResponseClient.setUserMobile(subscriptionResponse.getUserMobile());
        subscriptionResponseClient.setCustId(subscriptionResponse.getCustId());
        subscriptionResponseClient.setServiceId(subscriptionResponse.getServiceId());
        subscriptionResponseClient.setAccountType(subscriptionResponse.getAccountType());
        subscriptionResponseClient.setMerchantUniqueReference(subscriptionResponse.getMerchantUniqueReference());
        subscriptionResponseClient.setNextDueDate(subscriptionResponse.getNextDueDate());

        subscriptionResponseClient.setMandateInfo(getMandateInfo(subscriptionResponse.getMandateInfo()));

        subscriptionResponseClient.setSubsStartDate(subscriptionResponse.getSubsStartDate());
        subscriptionResponseClient.setAutoRenewalStatus(subscriptionResponse.isAutoRenewalStatus());
        subscriptionResponseClient.setAutoRetryStatus(subscriptionResponse.isAutoRetryStatus());
        subscriptionResponseClient.setCommunicationManagerStatus(subscriptionResponse.isCommunicationManagerStatus());
        subscriptionResponseClient.setOrderCreated(subscriptionResponse.isOrderCreated());

        subscriptionResponseClient.setGraceDays(subscriptionResponse.getGraceDays());
        subscriptionResponseClient.setSubsCallbackUrl(subscriptionResponse.getSubsCallbackUrl());

        subscriptionResponseClient.setSubscriptionUpiInfo(getSubscriptionUpiInfo(subscriptionResponse
                .getSubscriptionUpiInfo()));

        subscriptionResponseClient.setCardIndexNumber(subscriptionResponse.getCardIndexNumber());
        subscriptionResponseClient.setOrderActiveDays(subscriptionResponse.getOrderActiveDays());
        subscriptionResponseClient
                .setOrderInactiveTimeOutEnabled(subscriptionResponse.getOrderInactiveTimeOutEnabled());
        subscriptionResponseClient.setSubsPurpose(subscriptionResponse.getSubsPurpose());
        subscriptionResponseClient.setSubsMaxAmount(subscriptionResponse.getSubsMaxAmount());
        subscriptionResponseClient.setSiHubMode(subscriptionResponse.isSiHubMode());
        return subscriptionResponseClient;
    }

    private SubscriptionUpiInfo getSubscriptionUpiInfo(com.paytm.pgplus.response.SubscriptionUpiInfo subscriptionUpiInfo) {
        if (subscriptionUpiInfo == null)
            return null;

        SubscriptionUpiInfo subscriptionUpiInfoNative = new SubscriptionUpiInfo();
        subscriptionUpiInfoNative.setPayOption(subscriptionUpiInfo.getPayOption());
        subscriptionUpiInfoNative.setRefServiceInstId(subscriptionUpiInfo.getRefServiceInstId());
        subscriptionUpiInfoNative.setRenewalCount(subscriptionUpiInfo.getRenewalCount());
        subscriptionUpiInfoNative.setRetryAttempt(subscriptionUpiInfo.getRetryAttempt());
        subscriptionUpiInfoNative.setUmn(subscriptionUpiInfo.getUmn());
        subscriptionUpiInfoNative.setVpa(subscriptionUpiInfo.getVpa());

        return subscriptionUpiInfoNative;
    }

    private BankMandateInfo getMandateInfo(com.paytm.pgplus.response.BankMandateInfo mandateInfo) {
        if (mandateInfo == null)
            return null;

        BankMandateInfo bankMandateInfo = new BankMandateInfo();
        bankMandateInfo.setAccountHolderName(mandateInfo.getAccountHolderName());
        bankMandateInfo.setAccountType(mandateInfo.getAccountType());
        bankMandateInfo.setActivationTimeStamp(mandateInfo.getActivationTimeStamp());
        bankMandateInfo.setIssuerBankAccNo(mandateInfo.getIssuerBankAccNo());
        bankMandateInfo.setIssuerBankCode(mandateInfo.getIssuerBankCode());
        bankMandateInfo.setIssuerBankIfsc(mandateInfo.getIssuerBankIfsc());
        bankMandateInfo.setIssuerBankName(mandateInfo.getIssuerBankName());
        bankMandateInfo.setMode(mandateInfo.getMode());
        bankMandateInfo.setSubscriptionId(mandateInfo.getSubscriptionId());
        bankMandateInfo.setUmrnNo(mandateInfo.getUmrnNo());

        return bankMandateInfo;
    }

    /**
     * This method is used to decorate the response based on requirement. It can
     * be use as a hook after processing any request.
     *
     * @param request
     * @param subscriptionTransactionRequest
     * @param trxInfoResponse
     * @throws Exception
     */
    @Override
    protected SubscriptionTransactionResponse postProcess(SubscriptionTransactionRequest request,
            SubscriptionTransactionRequest subscriptionTransactionRequest, TrxInfoResponse trxInfoResponse)
            throws Exception {
        SubscriptionTransactionResponse response = createResponse(request, trxInfoResponse, false);
        return response;
    }

    private SubscriptionTransactionResponse createResponse(SubscriptionTransactionRequest request,
            TrxInfoResponse trxInfoResponse, boolean isPromoCodeValid) throws Exception {
        SubscriptionTransactionResponseBody responseBody = new SubscriptionTransactionResponseBody();
        responseBody.setTxnToken(trxInfoResponse.getTxntoken());
        responseBody.setAuthenticated(!isEmpty(request.getBody().getPaytmSsoToken()));
        responseBody.setSubscriptionId(trxInfoResponse.getSubscriptionId());
        if (trxInfoResponse.isIdempotent()) {
            responseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS_IDEMPOTENT_ERROR));
        }

        setInitiateTxnResponseToCache(trxInfoResponse, responseBody);

        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        SubscriptionTransactionResponse response = new SubscriptionTransactionResponse(responseHeader, responseBody);
        return response;
    }

    private void setInitiateTxnResponseToCache(TrxInfoResponse trxInfoResponse,
            InitiateTransactionResponseBody responseBody) {
        if (!trxInfoResponse.isIdempotent()) {
            nativeSessionUtil.setInitiateTxnResponse(responseBody.getTxnToken(), responseBody);
        }
    }

    private void validate(SubscriptionTransactionRequest request) {
        if (TheiaConstant.EnhancedCashierFlow.ENHANCED_CASHIER_FLOW.equals(request.getHead().getWorkFlow())) {
            if (StringUtils.isBlank(request.getBody().getPaytmSsoToken())) {
                String mid = request.getBody().getMid();

                UserDetailsBiz userDetailsBiz = nativeValidationService.validateLoginViaCookie(mid);

                if (userDetailsBiz != null) {
                    request.getBody().setPaytmSsoToken(userDetailsBiz.getUserToken());
                }

            } else {
                LOGGER.info("Skipping the login via cookie as SSO token is not blank");
            }
        }
        nativeValidationService.validate(request);
        susbcriptionNativeValidationService.validate(request);
    }

    private SubscriptionResponse createSubscription(SubscriptionTransactionRequest request) {
        FreshSubscriptionRequest freshSubscriptionRequest = nativeSubscriptionHelper
                .createFreshSubscriptionRequest(request);
        SubscriptionResponse freshSubscriptionResponse = subscriptionService
                .processFreshSubscription(freshSubscriptionRequest);
        LOGGER.debug("Response from subscription service : {}", freshSubscriptionResponse);
        return freshSubscriptionResponse;
    }

    private void createSubscriptionPaymentRequestData(PaymentRequestBean requestData,
            SubscriptionResponse subscriptionResponse, SubscriptionTransactionRequest subscriptionTransactionRequest)
            throws FacadeCheckedException {

        requestData.setSubscription(true);
        requestData.setSubscriptionID(subscriptionResponse.getSubscriptionId());
        requestData.setSubsPPIOnly(subscriptionTransactionRequest.getBody().getSubsPPIOnly());
        requestData.setSubsPaymentMode(subscriptionTransactionRequest.getBody().getSubscriptionPaymentMode());
        requestData.setRequestType(subscriptionTransactionRequest.getBody().getRequestType());
        requestData.setSubscriptionAmountType(subscriptionTransactionRequest.getBody().getSubscriptionAmountType());
        requestData.setSubscriptionEnableRetry(subscriptionTransactionRequest.getBody().getSubscriptionEnableRetry());
        requestData.setSubscriptionExpiryDate(subscriptionResponse.getSubscriptionExpiryDate());
        requestData.setSubscriptionFrequency(subscriptionResponse.getSubsFreq());
        requestData.setSubscriptionFrequencyUnit(subscriptionResponse.getSubsFreqUnit());
        requestData.setSubscriptionGraceDays(subscriptionTransactionRequest.getBody().getSubscriptionGraceDays());
        requestData.setSubscriptionServiceID(subscriptionResponse.getServiceId());
        requestData.setSubscriptionStartDate(subscriptionTransactionRequest.getBody().getSubscriptionStartDate());
        if (StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())
                && StringUtils.isNotBlank(subscriptionResponse.getPaymentOrderId())) {

            requestData.setAutoRefund(true);

            /**
             * paymentRequestBean - paymentOrderId and paymentMid will have
             * dummy values, so that we can use these values to construct create
             * order request.
             */

            requestData.setPaymentMid(subscriptionResponse.getPaymentMid());
            requestData.setPaymentOrderId(subscriptionResponse.getPaymentOrderId());
        }
        requestData.setMid(subscriptionTransactionRequest.getBody().getMid());
        requestData.setOrderId(subscriptionTransactionRequest.getBody().getOrderId());
        requestData.setSsoToken(subscriptionTransactionRequest.getBody().getPaytmSsoToken());

        requestData.setWebsite(subscriptionTransactionRequest.getBody().getWebsiteName());
        requestData.setAuthMode("3D");
        requestData.setCallbackUrl(subscriptionTransactionRequest.getBody().getCallbackUrl());
        requestData.setTxnAmount(subscriptionTransactionRequest.getBody().getTxnAmount().getValue());

        /*
         * set UserInfo details
         */
        if (subscriptionTransactionRequest.getBody().getUserInfo() != null) {
            requestData.setCustId(subscriptionTransactionRequest.getBody().getUserInfo().getCustId());
            requestData.setEmail(subscriptionTransactionRequest.getBody().getUserInfo().getEmail());
            requestData.setMobileNo(subscriptionTransactionRequest.getBody().getUserInfo().getMobile());
            requestData.setAddress1(subscriptionTransactionRequest.getBody().getUserInfo().getAddress());
            requestData.setPincode(subscriptionTransactionRequest.getBody().getUserInfo().getPincode());
        }

        requestData.setPromoCampId(subscriptionTransactionRequest.getBody().getPromoCode());
        requestData.setEmiOption(subscriptionTransactionRequest.getBody().getEmiOption());

        /*
         * set extendedInfo details
         */
        if (subscriptionTransactionRequest.getBody().getExtendInfo() != null) {
            requestData.setUdf1(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf1());
            requestData.setUdf2(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf2());
            requestData.setUdf3(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf3());
            requestData.setMerchUniqueReference(subscriptionTransactionRequest.getBody().getExtendInfo()
                    .getMercUnqRef());
            requestData.setComments(subscriptionTransactionRequest.getBody().getExtendInfo().getComments());
            requestData.setAdditionalInfo(subscriptionTransactionRequest.getBody().getExtendInfo().getComments());
            requestData.setSubwalletAmount(subscriptionTransactionRequest.getBody().getExtendInfo()
                    .getSubwalletAmount());
            requestData.setExtendInfo(subscriptionTransactionRequest.getBody().getExtendInfo());
        }

        requestData.setGoodsInfo(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody().getGoods()));
        requestData.setShippingInfo(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody()
                .getShippingInfo()));
        requestData.setAdditionalInfoMF(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody()
                .getAdditionalInfo()));

        requestData.setIndustryTypeId(nativeInitiateUtil.getIndustryTypeId(requestData.getMid()));

        if (StringUtils.isNotBlank(subscriptionTransactionRequest.getBody().getAggMid())) {
            requestData.setAggMid(subscriptionTransactionRequest.getBody().getAggMid());
        }

        if (StringUtils.isNotBlank(subscriptionTransactionRequest.getBody().getPEON_URL())) {
            requestData.setPeonURL(subscriptionTransactionRequest.getBody().getPEON_URL());
        }
        if (subscriptionTransactionRequest.getBody().getSplitSettlementInfoData() != null) {
            requestData.setSplitSettlementInfoData(subscriptionTransactionRequest.getBody()
                    .getSplitSettlementInfoData());
        }
        requestData.setAggType(subscriptionTransactionRequest.getBody().getAggType());
        requestData.setOrderPricingInfo(subscriptionTransactionRequest.getBody().getOrderPricingInfo());
        requestData.setOfflineTxnFlow(subscriptionTransactionRequest.getBody().isOfflineFlow());

        if (subscriptionTransactionRequest.getBody().isAoaSubsOnPgMid()) {
            requestData.setAoaSubsOnPgMid(true);
        }
    }

    private void processCreateOrder(SubscriptionResponse subscriptionResponse, SubscriptionTransactionRequest request,
            InitiateTokenBody initiateTokenBody, TrxInfoResponse trxInfoResponse) throws Exception {

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        PaymentRequestBean requestBean = new PaymentRequestBean();
        requestBean.setRequest(servletRequest);
        requestBean.setCreateOrderForInitiateTxnRequest(true);
        createSubscriptionPaymentRequestData(requestBean, subscriptionResponse, request);

        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setClientId(request.getHead().getClientId());
        SubscriptionTransactionResponseBody responseBody = new SubscriptionTransactionResponseBody();
        responseBody.setTxnToken(initiateTokenBody.getTxnId());
        responseBody.setAuthenticated(!isEmpty(request.getBody().getPaytmSsoToken()));
        responseBody.setSubscriptionId(trxInfoResponse.getSubscriptionId());
        SubscriptionTransactionResponse response = new SubscriptionTransactionResponse(responseHeader, responseBody);
        InitiateTransactionResponse initiateTransactionResponse = new InitiateTransactionResponse();
        initiateTransactionResponse.setHead(response.getHead());
        initiateTransactionResponse.setBody(response.getBody());
        nativeInitiateUtil.createOrder(initiateTransactionResponse, requestBean);
    }

    private void prepareExtendInfoForNotify(SubscriptionTransactionRequest request,
            SubscriptionResponse subscriptionResponse) {

        ExtendInfo extendInfo = request.getBody().getExtendInfo();

        if (extendInfo != null && extendInfo.getSubsLinkInfo() != null && subscriptionResponse != null) {
            extendInfo.getSubsLinkInfo().setRenewalAmount(request.getBody().getRenewalAmount());
            extendInfo.getSubsLinkInfo().setSubscriptionStartDate(request.getBody().getSubscriptionStartDate());
            extendInfo.getSubsLinkInfo().setSubscriptionMaxAmount(request.getBody().getSubscriptionMaxAmount());
            extendInfo.getSubsLinkInfo().setSubscriptionExpiryDate(request.getBody().getSubscriptionExpiryDate());
            extendInfo.getSubsLinkInfo().setSubsFreq(subscriptionResponse.getSubsFreq());
            extendInfo.getSubsLinkInfo().setSubsFreqUnit(subscriptionResponse.getSubsFreqUnit());
        }
    }

    private void enrich(SubscriptionTransactionRequest request) {
        if (merchantPreferenceService.isFlexiSubscriptionEnabled(request.getBody().getMid(), false)) {
            request.getBody().setFlexiSubscription(true);
            LOGGER.info(" Flexi Subscription Preference is enabled on merchant ");
        }
    }

    private boolean disableCcDcForSubsCriteria(SubscriptionTransactionRequest request) {
        return susbcriptionNativeValidationService.isDailySubscription(request)
                || susbcriptionNativeValidationService.checkIfTxnAmountLessThanMinAmt(request.getBody())
                || nativeSubscriptionHelper.invalidGraceDaysForCard(request.getBody().getSubscriptionGraceDays());
    }

}