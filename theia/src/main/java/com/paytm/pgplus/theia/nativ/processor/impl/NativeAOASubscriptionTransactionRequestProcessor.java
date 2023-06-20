//package com.paytm.pgplus.theia.nativ.processor.impl;
//
//import com.paytm.pgplus.aoaSubscriptionClient.model.request.AoaSubscriptionCreateRequest;
//import com.paytm.pgplus.aoaSubscriptionClient.model.response.AoaSubscriptionCreateResponse;
//import com.paytm.pgplus.aoaSubscriptionClient.service.IAoaSubscriptionService;
//import com.paytm.pgplus.biz.utils.ConfigurationUtil;
//import com.paytm.pgplus.cache.model.IfscCodeDetails;
//import com.paytm.pgplus.common.enums.ERequestType;
//import com.paytm.pgplus.common.enums.EventNameEnum;
//import com.paytm.pgplus.common.model.ResultInfo;
//import com.paytm.pgplus.facade.exception.FacadeCheckedException;
//import com.paytm.pgplus.facade.utils.JsonMapper;
//import com.paytm.pgplus.logging.ExtendedLogger;
//import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
//import com.paytm.pgplus.mappingserviceclient.service.IIfscDetailsService;
//import com.paytm.pgplus.models.ExtendInfo;
//import com.paytm.pgplus.models.MandateAccountDetails;
//import com.paytm.pgplus.payloadvault.subscription.response.BankMandateInfo;
//import com.paytm.pgplus.payloadvault.subscription.response.SubscriptionUpiInfo;
//import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
//import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
//import com.paytm.pgplus.request.SubscriptionTransactionRequest;
//import com.paytm.pgplus.response.*;
//import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
//import com.paytm.pgplus.theia.constants.TheiaConstant;
//import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
//import com.paytm.pgplus.theia.nativ.model.token.InitiateTokenBody;
//import com.paytm.pgplus.theia.nativ.model.token.TrxInfoResponse;
//import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
//import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
//import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
//import com.paytm.pgplus.theia.nativ.utils.*;
//import com.paytm.pgplus.theia.offline.enums.ResultCode;
//import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
//import com.paytm.pgplus.theia.offline.exceptions.SubscriptionServiceException;
//import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
//import com.paytm.pgplus.theia.utils.EnvInfoUtil;
//import com.paytm.pgplus.theia.utils.EventUtils;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.paytm.pgplus.biz.utils.BizConstant.SUBSCRIPTION_SUCCESS_CODE;
//import static org.apache.commons.lang.StringUtils.isEmpty;
//
//@Service("nativeAOASubscriptionTransactionRequestProcessor")
//public class NativeAOASubscriptionTransactionRequestProcessor
//        extends
//        AbstractRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse, SubscriptionTransactionRequest, TrxInfoResponse> {
//
//    private static final Logger LOGGER = LoggerFactory
//            .getLogger(NativeAOASubscriptionTransactionRequestProcessor.class);
//    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeAOASubscriptionTransactionRequestProcessor.class);
//
//    @Autowired
//    @Qualifier("nativePaymentService")
//    private INativePaymentService nativePaymentService;
//
//    @Autowired
//    @Qualifier("nativeValidationService")
//    private INativeValidationService nativeValidationService;
//
//    @Autowired
//    private IAoaSubscriptionService aoasubscriptionService;
//
//    @Autowired
//    @Qualifier("nativeSubscriptionHelper")
//    private INativeSubscriptionHelper nativeSubscriptionHelper;
//
//    @Autowired
//    @Qualifier("subscriptionNativeValidationService")
//    private ISusbcriptionNativeValidationService susbcriptionNativeValidationService;
//
//    @Autowired
//    private NativeSessionUtil nativeSessionUtil;
//
//    @Autowired
//    @Qualifier("nativeInitiateUtil")
//    private NativeInitiateUtil nativeInitiateUtil;
//
//    @Autowired
//    private IPgpFf4jClient iPgpFf4jClient;
//
//    @Autowired
//    @Qualifier(value = "merchantPreferenceService")
//    private IMerchantPreferenceService merchantPreferenceService;
//
//    @Autowired
//    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;
//
//    @Autowired
//    private IIfscDetailsService ifscDetailsService;
//
//    private static final int DEFAULT_EXPIRY_TIME_IN_MIN = 30;
//
//    /**
//     * This method is use to validate/enrich the request before processing the
//     * request. It can be use as a hook before processing any request.
//     *
//     * @param request
//     */
//    @Override
//    protected SubscriptionTransactionRequest preProcess(SubscriptionTransactionRequest request) {
//
//        if (request.isSkipSubsContractValidation()) {
//            nativeValidationService.validate(request);
//            return request;
//        }
//
//        if (StringUtils.isEmpty(request.getBody().getRequestType())
//                || ERequestType.SUBSCRIBE.getType().equalsIgnoreCase(request.getBody().getRequestType())) {
//            request.getBody().setRequestType(ERequestType.NATIVE_SUBSCRIPTION.getType());
//        }
//
//        EventUtils.pushTheiaEvents(
//                EventNameEnum.ORDER_INITIATED,
//                new ImmutablePair<>("REQUEST_TYPE", String
//                        .valueOf(RequestProcessorFactory.RequestType.NATIVE_AOA_SUBSCRIPTION)));
//        validate(request);
//        return request;
//    }
//
//    /**
//     * This method is used to process the request which includes request
//     * validation, processing, response generator.
//     *
//     * @param request
//     * @param subscriptionTransactionRequest
//     * @return
//     * @throws Exception
//     */
//    @Override
//    protected TrxInfoResponse onProcess(SubscriptionTransactionRequest request,
//            SubscriptionTransactionRequest subscriptionTransactionRequest) throws Exception {
//        boolean cacheSubsDetail = false;
//        AoaSubscriptionCreateResponse aoaSubscriptionResponse = null;
//        TrxInfoResponse trxInfoResponse = new TrxInfoResponse();
//        setChannelCodeFromIFSC(subscriptionTransactionRequest.getBody().getMandateAccountDetails());
//
//        if ((request.getBody().getSubscriptionResponse() != null)) {
//            SubscriptionResponse subscriptionResponseCommon = request.getBody()
//                    .getSubscriptionResponse();
//            aoaSubscriptionResponse = getSubscriptionResponse(subscriptionResponseCommon);
//            prepareExtendInfoForNotify(request, aoaSubscriptionResponse);
//        } else {
//            aoaSubscriptionResponse = this.createSubscription(request);
//        }
//
//        if (!SUBSCRIPTION_SUCCESS_CODE.equalsIgnoreCase(aoaSubscriptionResponse.getRespCode())) {
//            LOGGER.error("Unable to create subscription");
//            throw new SubscriptionServiceException(new ResultInfo(aoaSubscriptionResponse.getStatus().getName(),
//                    aoaSubscriptionResponse.getRespCode(), aoaSubscriptionResponse.getRespMsg()));
//        } else {
//            trxInfoResponse.setSubscriptionId(aoaSubscriptionResponse.getSubscriptionId());
//            cacheSubsDetail = true;
//        }
//
//        InitiateTokenBody initiateTokenBody = nativePaymentService.initiateTransaction(request);
//        if (initiateTokenBody.isIdempotent()) {
//            if (!nativeSessionUtil.isIdempotentRequest(request.getBody(), initiateTokenBody.getTxnId())) {
//                // Fail for Modified request if not idempotent
//                throw RequestValidationException.getException(ResultCode.REPEAT_REQUEST_INCONSISTENT);
//            }
//        }
//
//        if (cacheSubsDetail) {
//            int expiryTimeInSeconds = Integer.parseInt(ConfigurationUtil.getProperty(
//                    "subscriptionDetailCacheExpiryTimeInMinutes", String.valueOf(DEFAULT_EXPIRY_TIME_IN_MIN))) * 60;
//            StringBuilder key = new StringBuilder(request.getBody().getRequestType()).append(initiateTokenBody
//                    .getTxnId());
//            theiaTransactionalRedisUtil.set(key.toString(), aoaSubscriptionResponse, expiryTimeInSeconds);
//
//        }
//        Map<String, Object> context = new HashMap<>();
//        context.put(TheiaConstant.RequestParams.Native.MID, request.getBody().getMid());
//        HttpServletRequest serverRequest = EnvInfoUtil.httpServletRequest();
//        if (StringUtils.equals(TheiaConstant.RequestParams.SUBSCRIPTION_INITIATE_TRANSACTION_URL,
//                serverRequest.getRequestURI())
//                && !request.isSkipOrderCreateInSubs()) {
//            try {
//                processCreateOrder(aoaSubscriptionResponse, request, initiateTokenBody, trxInfoResponse);
//            } catch (Exception ex) {
//                LOGGER.error("Exception occured while creating order in Native initiate subscription request", ex);
//                StringBuilder key = new StringBuilder(request.getBody().getRequestType()).append(initiateTokenBody
//                        .getTxnId());
//                theiaTransactionalRedisUtil.del(key.toString());
//                throw ex;
//            }
//        }
//        trxInfoResponse.setTxntoken(initiateTokenBody.getTxnId());
//        trxInfoResponse.setIdempotent(initiateTokenBody.isIdempotent());
//        return trxInfoResponse;
//    }
//
//    private void setChannelCodeFromIFSC(MandateAccountDetails mandateAccountDetails) throws TheiaDataMappingException {
//        if (mandateAccountDetails != null && StringUtils.isBlank(mandateAccountDetails.getBankCode())
//                && StringUtils.isNotBlank(mandateAccountDetails.getIfsc())) {
//            String ifscCode = mandateAccountDetails.getIfsc();
//            try {
//                LOGGER.info("Calling mapping to get bank code for IFSC code: " + ifscCode);
//                IfscCodeDetails ifscDetails = ifscDetailsService.getIfscCodeDetails(ifscCode);
//                EXT_LOGGER.customInfo("Mapping response - IfscCodeDetails :: {}", ifscDetails);
//                if (ifscDetails != null && StringUtils.isNotBlank(ifscDetails.getBankCode())) {
//                    mandateAccountDetails.setBankCode(ifscDetails.getBankCode());
//                } else {
//                    LOGGER.warn("Bank code not found from mapping for IFSC code " + ifscCode);
//                }
//            } catch (MappingServiceClientException e) {
//                LOGGER.error("Mapping Error while getting bank details for IFSC code {}, error: {}", ifscCode, e);
//            }
//        }
//    }
//
//    private AoaSubscriptionCreateResponse getSubscriptionResponse(
//            SubscriptionResponse subscriptionResponse) {
//        AoaSubscriptionCreateResponse aoasubscriptionResponseClient = new AoaSubscriptionCreateResponse();
//
//        aoasubscriptionResponseClient.setMid(subscriptionResponse.getMid());
//        aoasubscriptionResponseClient.setTxnId(subscriptionResponse.getTxnId());
//        aoasubscriptionResponseClient.setOrderId(subscriptionResponse.getOrderId());
//        aoasubscriptionResponseClient.setTxnAmount(subscriptionResponse.getTxnAmount());
//        aoasubscriptionResponseClient.setTxnDate(subscriptionResponse.getTxnDate());
//        aoasubscriptionResponseClient.setRespCode(subscriptionResponse.getRespCode());
//        aoasubscriptionResponseClient.setRespMsg(subscriptionResponse.getRespMsg());
//        aoasubscriptionResponseClient.setStatus(subscriptionResponse.getStatus());
//        aoasubscriptionResponseClient.setSubscriptionId(subscriptionResponse.getSubscriptionId());
//        aoasubscriptionResponseClient.setPaymentMode(subscriptionResponse.getPaymentMode());
//        aoasubscriptionResponseClient.setPayerUserID(subscriptionResponse.getPayerUserID());
//        aoasubscriptionResponseClient.setPayerAccountNumber(subscriptionResponse.getPayerAccountNumber());
//        aoasubscriptionResponseClient.setSubscriptionExpiryDate(subscriptionResponse.getSubscriptionExpiryDate());
//        aoasubscriptionResponseClient.setWebsite(subscriptionResponse.getWebsite());
//        aoasubscriptionResponseClient.setIndustryType(subscriptionResponse.getIndustryType());
//        aoasubscriptionResponseClient.setSubsFreq(subscriptionResponse.getSubsFreq());
//        aoasubscriptionResponseClient.setSubsFreqUnit(subscriptionResponse.getSubsFreqUnit());
//        aoasubscriptionResponseClient.setUserEmail(subscriptionResponse.getUserEmail());
//        aoasubscriptionResponseClient.setUserMobile(subscriptionResponse.getUserMobile());
//        aoasubscriptionResponseClient.setCustId(subscriptionResponse.getCustId());
//        aoasubscriptionResponseClient.setServiceId(subscriptionResponse.getServiceId());
//        aoasubscriptionResponseClient.setAccountType(subscriptionResponse.getAccountType());
//        aoasubscriptionResponseClient.setMerchantUniqueReference(subscriptionResponse.getMerchantUniqueReference());
//        aoasubscriptionResponseClient.setNextDueDate(subscriptionResponse.getNextDueDate());
//        aoasubscriptionResponseClient.setSubsStartDate(subscriptionResponse.getSubsStartDate());
//        aoasubscriptionResponseClient.setAutoRenewalStatus(subscriptionResponse.isAutoRenewalStatus());
//        aoasubscriptionResponseClient.setAutoRetryStatus(subscriptionResponse.isAutoRetryStatus());
//        aoasubscriptionResponseClient
//                .setCommunicationManagerStatus(subscriptionResponse.isCommunicationManagerStatus());
//
//        aoasubscriptionResponseClient.setGraceDays(subscriptionResponse.getGraceDays());
//        aoasubscriptionResponseClient.setSubsCallbackUrl(subscriptionResponse.getSubsCallbackUrl());
//        aoasubscriptionResponseClient.setSubsPurpose(subscriptionResponse.getSubsPurpose());
//        aoasubscriptionResponseClient.setSubsMaxAmount(subscriptionResponse.getSubsMaxAmount());
//
//        return aoasubscriptionResponseClient;
//    }
//
//
//    /**
//     * This method is used to decorate the response based on requirement. It can
//     * be use as a hook after processing any request.
//     *
//     * @param request
//     * @param subscriptionTransactionRequest
//     * @param trxInfoResponse
//     * @throws Exception
//     */
//    @Override
//    protected SubscriptionTransactionResponse postProcess(SubscriptionTransactionRequest request,
//            SubscriptionTransactionRequest subscriptionTransactionRequest, TrxInfoResponse trxInfoResponse)
//            throws Exception {
//        SubscriptionTransactionResponse response = createResponse(request, trxInfoResponse, false);
//        return response;
//    }
//
//    private SubscriptionTransactionResponse createResponse(SubscriptionTransactionRequest request,
//            TrxInfoResponse trxInfoResponse, boolean isPromoCodeValid) throws Exception {
//        SubscriptionTransactionResponseBody responseBody = new SubscriptionTransactionResponseBody();
//        responseBody.setTxnToken(trxInfoResponse.getTxntoken());
//        responseBody.setAuthenticated(!isEmpty(request.getBody().getPaytmSsoToken()));
//        responseBody.setSubscriptionId(trxInfoResponse.getSubscriptionId());
//        if (trxInfoResponse.isIdempotent()) {
//            responseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS_IDEMPOTENT_ERROR));
//        }
//
//        setInitiateTxnResponseToCache(trxInfoResponse, responseBody);
//
//        SecureResponseHeader responseHeader = new SecureResponseHeader();
//        responseHeader.setClientId(request.getHead().getClientId());
//        SubscriptionTransactionResponse response = new SubscriptionTransactionResponse(responseHeader, responseBody);
//        return response;
//    }
//
//    private void setInitiateTxnResponseToCache(TrxInfoResponse trxInfoResponse,
//            InitiateTransactionResponseBody responseBody) {
//        if (!trxInfoResponse.isIdempotent()) {
//            nativeSessionUtil.setInitiateTxnResponse(responseBody.getTxnToken(), responseBody);
//        }
//    }
//
//    private void validate(SubscriptionTransactionRequest request) {
//        nativeValidationService.validate(request);
//        susbcriptionNativeValidationService.validateAOASubscriptionRequest(request);
//    }
//
//    private AoaSubscriptionCreateResponse createSubscription(SubscriptionTransactionRequest request) {
//        AoaSubscriptionCreateRequest freshSubscriptionRequest = nativeSubscriptionHelper
//                .createFreshAOASubscriptionRequest(request);
//        AoaSubscriptionCreateResponse freshSubscriptionResponse = aoasubscriptionService
//                .createAoaSubscription(freshSubscriptionRequest);
//        LOGGER.debug("Response from AOA subscription service : {}", freshSubscriptionResponse);
//        return freshSubscriptionResponse;
//    }
//
//    private void createSubscriptionPaymentRequestData(PaymentRequestBean requestData,
//            AoaSubscriptionCreateResponse aoasubscriptionResponse,
//            SubscriptionTransactionRequest subscriptionTransactionRequest) throws FacadeCheckedException
//
//    {
//        requestData.setSubscription(true);
//        requestData.setSubscriptionID(aoasubscriptionResponse.getSubscriptionId());
//        requestData.setSubsPPIOnly(subscriptionTransactionRequest.getBody().getSubsPPIOnly());
//        requestData.setSubsPaymentMode(subscriptionTransactionRequest.getBody().getSubscriptionPaymentMode());
//        requestData.setRequestType(subscriptionTransactionRequest.getBody().getRequestType());
//        requestData.setSubscriptionAmountType(subscriptionTransactionRequest.getBody().getSubscriptionAmountType());
//        requestData.setSubscriptionEnableRetry(subscriptionTransactionRequest.getBody().getSubscriptionEnableRetry());
//        requestData.setSubscriptionExpiryDate(aoasubscriptionResponse.getSubscriptionExpiryDate());
//        requestData.setSubscriptionFrequency(aoasubscriptionResponse.getSubsFreq());
//        requestData.setSubscriptionFrequencyUnit(aoasubscriptionResponse.getSubsFreqUnit());
//        requestData.setSubscriptionGraceDays(subscriptionTransactionRequest.getBody().getSubscriptionGraceDays());
//        requestData.setSubscriptionServiceID(aoasubscriptionResponse.getServiceId());
//        requestData.setSubscriptionStartDate(subscriptionTransactionRequest.getBody().getSubscriptionStartDate());
//
//        requestData.setMid(subscriptionTransactionRequest.getBody().getMid());
//        requestData.setOrderId(subscriptionTransactionRequest.getBody().getOrderId());
//        requestData.setSsoToken(subscriptionTransactionRequest.getBody().getPaytmSsoToken());
//
//        requestData.setWebsite(subscriptionTransactionRequest.getBody().getWebsiteName());
//        requestData.setAuthMode("3D");
//        requestData.setCallbackUrl(subscriptionTransactionRequest.getBody().getCallbackUrl());
//        requestData.setTxnAmount(subscriptionTransactionRequest.getBody().getTxnAmount().getValue());
//
//        /*
//         * set UserInfo details
//         */
//        if (subscriptionTransactionRequest.getBody().getUserInfo() != null) {
//            requestData.setCustId(subscriptionTransactionRequest.getBody().getUserInfo().getCustId());
//            requestData.setEmail(subscriptionTransactionRequest.getBody().getUserInfo().getEmail());
//            requestData.setMobileNo(subscriptionTransactionRequest.getBody().getUserInfo().getMobile());
//            requestData.setAddress1(subscriptionTransactionRequest.getBody().getUserInfo().getAddress());
//            requestData.setPincode(subscriptionTransactionRequest.getBody().getUserInfo().getPincode());
//        }
//
//        requestData.setPromoCampId(subscriptionTransactionRequest.getBody().getPromoCode());
//        requestData.setEmiOption(subscriptionTransactionRequest.getBody().getEmiOption());
//
//        /*
//         * set extendedInfo details
//         */
//        if (subscriptionTransactionRequest.getBody().getExtendInfo() != null) {
//            requestData.setUdf1(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf1());
//            requestData.setUdf2(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf2());
//            requestData.setUdf3(subscriptionTransactionRequest.getBody().getExtendInfo().getUdf3());
//            requestData.setMerchUniqueReference(subscriptionTransactionRequest.getBody().getExtendInfo()
//                    .getMercUnqRef());
//            requestData.setComments(subscriptionTransactionRequest.getBody().getExtendInfo().getComments());
//            requestData.setAdditionalInfo(subscriptionTransactionRequest.getBody().getExtendInfo().getComments());
//            requestData.setSubwalletAmount(subscriptionTransactionRequest.getBody().getExtendInfo()
//                    .getSubwalletAmount());
//            requestData.setExtendInfo(subscriptionTransactionRequest.getBody().getExtendInfo());
//        }
//
//        requestData.setGoodsInfo(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody().getGoods()));
//        requestData.setShippingInfo(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody()
//                .getShippingInfo()));
//        requestData.setAdditionalInfoMF(JsonMapper.mapObjectToJson(subscriptionTransactionRequest.getBody()
//                .getAdditionalInfo()));
//
//        requestData.setIndustryTypeId(nativeInitiateUtil.getIndustryTypeId(requestData.getMid()));
//
//        if (StringUtils.isNotBlank(subscriptionTransactionRequest.getBody().getAggMid())) {
//            requestData.setAggMid(subscriptionTransactionRequest.getBody().getAggMid());
//        }
//
//        if (StringUtils.isNotBlank(subscriptionTransactionRequest.getBody().getPEON_URL())) {
//            requestData.setPeonURL(subscriptionTransactionRequest.getBody().getPEON_URL());
//        }
//        if (subscriptionTransactionRequest.getBody().getSplitSettlementInfoData() != null) {
//            requestData.setSplitSettlementInfoData(subscriptionTransactionRequest.getBody()
//                    .getSplitSettlementInfoData());
//        }
//        requestData.setAggType(subscriptionTransactionRequest.getBody().getAggType());
//        requestData.setOrderPricingInfo(subscriptionTransactionRequest.getBody().getOrderPricingInfo());
//        requestData.setOfflineTxnFlow(subscriptionTransactionRequest.getBody().isOfflineFlow());
//    }
//
//    private void processCreateOrder(AoaSubscriptionCreateResponse aoasubscriptionResponse,
//            SubscriptionTransactionRequest request, InitiateTokenBody initiateTokenBody, TrxInfoResponse trxInfoResponse)
//            throws Exception {
//
//        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
//                .getRequest();
//        PaymentRequestBean requestBean = new PaymentRequestBean();
//        requestBean.setRequest(servletRequest);
//        requestBean.setCreateOrderForInitiateTxnRequest(true);
//        createSubscriptionPaymentRequestData(requestBean, aoasubscriptionResponse, request);
//
//        SecureResponseHeader responseHeader = new SecureResponseHeader();
//        responseHeader.setClientId(request.getHead().getClientId());
//        SubscriptionTransactionResponseBody responseBody = new SubscriptionTransactionResponseBody();
//        responseBody.setTxnToken(initiateTokenBody.getTxnId());
//        responseBody.setAuthenticated(!isEmpty(request.getBody().getPaytmSsoToken()));
//        responseBody.setSubscriptionId(trxInfoResponse.getSubscriptionId());
//        SubscriptionTransactionResponse response = new SubscriptionTransactionResponse(responseHeader, responseBody);
//        InitiateTransactionResponse initiateTransactionResponse = new InitiateTransactionResponse();
//        initiateTransactionResponse.setHead(response.getHead());
//        initiateTransactionResponse.setBody(response.getBody());
//        nativeInitiateUtil.createOrder(initiateTransactionResponse, requestBean);
//    }
//
//    private void prepareExtendInfoForNotify(SubscriptionTransactionRequest request,
//            AoaSubscriptionCreateResponse aoasubscriptionResponse) {
//
//        ExtendInfo extendInfo = request.getBody().getExtendInfo();
//
//        if (extendInfo != null && extendInfo.getSubsLinkInfo() != null && aoasubscriptionResponse != null) {
//            extendInfo.getSubsLinkInfo().setRenewalAmount(request.getBody().getRenewalAmount());
//            extendInfo.getSubsLinkInfo().setSubscriptionStartDate(request.getBody().getSubscriptionStartDate());
//            extendInfo.getSubsLinkInfo().setSubscriptionMaxAmount(request.getBody().getSubscriptionMaxAmount());
//            extendInfo.getSubsLinkInfo().setSubscriptionExpiryDate(request.getBody().getSubscriptionExpiryDate());
//            extendInfo.getSubsLinkInfo().setSubsFreq(aoasubscriptionResponse.getSubsFreq());
//            extendInfo.getSubsLinkInfo().setSubsFreqUnit(aoasubscriptionResponse.getSubsFreqUnit());
//        }
//    }
//
// }