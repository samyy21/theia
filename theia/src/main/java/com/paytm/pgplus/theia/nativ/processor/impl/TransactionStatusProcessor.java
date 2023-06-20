package com.paytm.pgplus.theia.nativ.processor.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.SubsPaymentMode;
import com.paytm.pgplus.common.validator.GenericBeanValidator;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionResponse;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.PaymentOption;
import com.paytm.pgplus.theia.enums.UPIPollStatus;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.models.ModifiableHttpServletRequest;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusRequest;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusRequestBody;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusResponse;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TransactionStatusAsyncService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_ENCRYPTED_RESPONSE_TO_JSON;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.HEADER_WORKFLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;

@Service("transactionStatusProcessor")
public class TransactionStatusProcessor
        extends
        AbstractRequestProcessor<TransactionStatusRequest, TransactionStatusResponse, HttpServletRequest, TransactionResponse> {
    public static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(TransactionStatusProcessor.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    @Qualifier("merchantResponseService")
    MerchantResponseService merchantResponseService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    private AOAUtils aoaUtils;

    @Autowired
    MappingUtil mapUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    private final ExecutorService transactionStatusExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("TransactionStatus-thread-%d").build());

    @Override
    protected HttpServletRequest preProcess(TransactionStatusRequest request) {

        if (!isValid(request)) {
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PARAM).isHTMLResponse(false)
                    .isNativeJsonRequest(true).build();
        }
        InitiateTransactionRequestBody orderDetail = null;
        HttpServletRequest httpServletRequest = null;
        try {
            String txnToken = request.getHead().getTxnToken();
            if (StringUtils.isBlank(txnToken) && request.getHead().getTokenType() != null
                    && TokenType.TXN_TOKEN.name().equals(request.getHead().getTokenType().name())
                    && StringUtils.isNotBlank(request.getHead().getToken())) {
                txnToken = request.getHead().getToken();
            } else if (request.getHead().getToken() != null && TokenType.SSO.equals(request.getHead().getTokenType())) {
                LOGGER.info("Validatintg txnStatus for MID+SSO flow");
                txnToken = request.getBody().getMerchantId() + request.getBody().getOrderId();
                nativeSessionUtil.validateMidSSOBasedTxnToken(txnToken, request.getHead().getToken());
            }
            NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(txnToken);
            orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
            nativeValidationService.validateMidOrderIdinRequest(orderDetail.getMid(), orderDetail.getOrderId(), request
                    .getBody().getMerchantId(), request.getBody().getOrderId());
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse();

            if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType())
                    || ERequestType.NATIVE_MF_SIP.getType().equals(orderDetail.getRequestType())) {
                String paymentTypeId = nativeSessionUtil.getPaymentTypeId(txnToken);

                if (SubsPaymentMode.BANK_MANDATE.toString().equals(paymentTypeId)) {
                    request.getBody().setPaymentMode(SubsPaymentMode.BANK_MANDATE.getePayMethodName());
                } else {
                    setTransIdCashierRequestIdPaymentModeInRequest(request, txnToken);
                }
                String key = orderDetail.getRequestType() + txnToken;
                if (aoaUtils.isAOAMerchant(request.getBody().getMerchantId())) {
                    LOGGER.error("AOA subscription client call is being used");
                    // AoaSubscriptionCreateResponse
                    // aoaSubscriptionCreateResponse =
                    // (AoaSubscriptionCreateResponse)
                    // theiaTransactionalRedisUtil.get(key);
                    // mapUtils.mapAOASubsResponseIntoSubsResponse(aoaSubscriptionCreateResponse,
                    // subscriptionResponse);
                } else {
                    subscriptionResponse = (SubscriptionResponse) theiaTransactionalRedisUtil.get(key);
                }
            } else {
                setTransIdCashierRequestIdPaymentModeInRequest(request, txnToken);
            }
            // UPI collect FF4J feature :- To support v1/txnStatus for polling
            String paymentOption = StringUtils.EMPTY;
            // Enabling UPI Collect Polling Support only on version V2 in
            // request headers irrespective of FF4J, as of now FF4J flag will be
            // enabled for ALL Mids for this feature.
            if (StringUtils.equalsIgnoreCase(request.getHead().getVersion(), TheiaConstant.RequestHeaders.Version_V2)
                    && ff4jUtils.isFeatureEnabledOnMid(request.getBody().getMerchantId(),
                            TheiaConstant.FF4J.THEIA_USE_V1_TXN_STATUS_FOR_UPI_POLLING, false)) {
                paymentOption = nativeSessionUtil.getPaymentOption(txnToken);
            }

            httpServletRequest = setParametersForCashierResponseInHttpRequest(request, subscriptionResponse,
                    orderDetail, paymentOption, txnToken);
            request.setOrderDetail(orderDetail);
            return httpServletRequest;
        } catch (SessionExpiredException see) {
            if (StringUtils.isNotBlank(request.getHead().getTxnToken())
                    && nativeSessionUtil.getOrderClosedonCheckoutJS(request.getHead().getTxnToken())) {
                EXT_LOGGER.customInfo("Deleting txntoken cached data after txnStatus");
                nativeSessionUtil.deleteKey(request.getHead().getTxnToken(), nativeSessionUtil
                        .getMidOrderIdKeyForRedis(request.getBody().getMerchantId(), request.getBody().getOrderId()));
            }
            throwException(ResultCode.SESSION_EXPIRED_EXCEPTION, orderDetail);
        } catch (MidDoesnotMatchException mme) {
            throwException(ResultCode.MID_DOES_NOT_MATCH, orderDetail);
        } catch (OrderIdDoesnotMatchException orderIdException) {
            throwException(ResultCode.ORDER_ID_DOES_NOT_MATCH, orderDetail);
        }
        return httpServletRequest;
    }

    @Override
    protected TransactionResponse onProcess(TransactionStatusRequest request, HttpServletRequest httpServletRequest)
            throws Exception {

        TransactionResponse transactionResponse = null;
        try {
            // for UPI Collect Polling functionality
            if (!request.getBody().getFinalTxnStatusRequired()
                    && BooleanUtils.isTrue(Boolean.valueOf(httpServletRequest.getParameter(IS_ASYNC_TXN_STATUS_FLOW)))
                    && StringUtils.isNotBlank(httpServletRequest.getParameter(TXN_TOKEN))) {
                transactionResponse = new TransactionResponse();
                String pollingStatus = nativeSessionUtil.getPollingStatus(httpServletRequest.getParameter(TXN_TOKEN));
                if (StringUtils.isBlank(pollingStatus)) {
                    nativeSessionUtil.setPollingStatus(httpServletRequest.getParameter(TXN_TOKEN),
                            UPIPollStatus.POLL_AGAIN.getMessage());
                    transactionResponse.setPollingRequired(true);
                    Runnable transactionPollingStatus = new TransactionStatusAsyncService(httpServletRequest);
                    transactionStatusExecutor.execute(transactionPollingStatus);
                } else if (pollingStatus.equalsIgnoreCase(UPIPollStatus.STOP_POLLING.getMessage())) {
                    transactionResponse.setPollingRequired(false);
                    Map<String, String> data = transactionStatusServiceImpl.getCashierResponse(httpServletRequest);
                    transactionResponse = JsonMapper.mapJsonToObject(data.get(TRANSACTION_RESPONSE_OBJECT),
                            TransactionResponse.class);
                    if (isRetryAllowedInCallBackUrl(transactionResponse.getCallbackUrl())) {
                        nativeSessionUtil.deleteField(httpServletRequest.getParameter(TXN_TOKEN), "pollingStatus");
                    }
                } else {
                    transactionResponse.setPollingRequired(true);
                }
                return transactionResponse;
            }
            Map<String, String> data = transactionStatusServiceImpl.getCashierResponse(httpServletRequest);
            transactionResponse = JsonMapper.mapJsonToObject(data.get(TRANSACTION_RESPONSE_OBJECT),
                    TransactionResponse.class);
        } catch (Exception e) {
            // LOGGER.error("Exception in TransactionStatus Processor {}",
            // ExceptionUtils.getStackTrace(e));
            LOGGER.error("Exception in TransactionStatus Processor {}", ExceptionLogUtils.limitLengthOfStackTrace(e));
            throwException(ResultCode.FAILED, request.getOrderDetail());
        }
        return transactionResponse;
    }

    @Override
    protected TransactionStatusResponse postProcess(TransactionStatusRequest request,
            HttpServletRequest httpServletRequest, TransactionResponse transactionResponse) throws Exception {

        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        if (BooleanUtils.isTrue(Boolean.valueOf(httpServletRequest.getParameter(IS_ASYNC_TXN_STATUS_FLOW)))) {
            transactionStatusResponse.setHead(new ResponseHeader(request.getHead().getVersion()));
        } else {
            transactionStatusResponse.setHead(new ResponseHeader());
        }
        TransactionStatusResponseBody body = new TransactionStatusResponseBody();
        /*
         * by default setting resultCode and success, later we update it
         * depending on the status
         */
        ResultInfo resultInfo = null;
        Map<String, String> txnInfo = new TreeMap<>();
        String mid = "";
        if (request.getOrderDetail() != null) {
            mid = request.getOrderDetail().getMid();
        }
        if (StringUtils.isBlank(mid) && transactionResponse != null) {
            mid = transactionResponse.getMid();
        }
        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);
        boolean encRequestEnabled = merchantPreferenceService.isEncRequestEnabled(mid);
        if ((isAES256Encrypted || encRequestEnabled)
                && ff4jUtils.isFeatureEnabledOnMid(mid, THEIA_ENCRYPTED_RESPONSE_TO_JSON, false)) {
            LOGGER.info("Feature encryptedResponseToJson is enabled");
            if (transactionResponse.getMid() != null) {
                merchantResponseService.encryptedResponseJson(mid, txnInfo, transactionResponse, isAES256Encrypted,
                        encRequestEnabled);
            }
        } else {
            merchantResponseService.makeReponseToMerchantEnhancedNative(transactionResponse, txnInfo);
        }
        if (!StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(),
                transactionResponse.getTransactionStatus())) {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);
        } else {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
        }
        if (retryServiceHelper.checkNativePaymentRetry(request.getBody().getMerchantId(), request.getBody()
                .getOrderId())
                && StringUtils.equals(ExternalTransactionStatus.PENDING.name(),
                        transactionResponse.getTransactionStatus())) {
            resultInfo.setUserRetryAllowed(true);
        }
        resultInfo.setResultCode(transactionResponse.getResponseCode());
        resultInfo.setResultMsg(transactionResponse.getResponseMsg());

        // setting response in case further polling is required for UPI collect
        boolean isPollingRequired = transactionResponse.isPollingRequired();

        String callBackUrl = transactionResponse.getCallbackUrl();

        if (isRetryAllowedInCallBackUrl(callBackUrl)) {
            resultInfo.setRetry(true);
        } else {
            resultInfo.setRetry(false);
        }
        if (isPollingRequired) {
            resultInfo = NativePaymentUtil.resultInfo(ResultCode.SUCCESS);
            txnInfo = null;
        }
        body.setResultInfo(resultInfo);
        body.setTxnInfo(txnInfo);
        if (request.getBody().isCallbackUrlRequired()) {
            body.setCallbackUrl(callBackUrl);
        }
        body.setPollingRequired(isPollingRequired);
        body.setDeclineReason(transactionResponse.getDeclineReason());
        transactionStatusResponse.setBody(body);
        return transactionStatusResponse;

    }

    private HttpServletRequest setParametersForCashierResponseInHttpRequest(
            TransactionStatusRequest transactionStatusRequest, SubscriptionResponse subscriptionResponse,
            InitiateTransactionRequestBody orderDetail, String paymentOption, String txnToken) {
        Map<String, String[]> paramMap = new HashMap<>();
        TransactionStatusRequestBody transactionStatusRequestBody = transactionStatusRequest.getBody();
        paramMap.put(CASHIER_REQUEST_ID, new String[] { transactionStatusRequestBody.getCashierRequestId() });
        paramMap.put(TRANS_ID, new String[] { transactionStatusRequestBody.getTransId() });
        if (null != subscriptionResponse && StringUtils.isNotBlank(subscriptionResponse.getPaymentMid())) {
            paramMap.put(MERCHANT_ID, new String[] { subscriptionResponse.getMid() });
            String alipayMerchantId = StringUtils.EMPTY;
            try {
                MappingMerchantData mappingMerchantData = merchantMappingService
                        .getMappingMerchantData(subscriptionResponse.getPaymentMid());
                if (mappingMerchantData != null) {
                    alipayMerchantId = mappingMerchantData.getAlipayId();
                }
            } catch (Exception e) {
                LOGGER.error(
                        "Exception occurred while fetching Merchant mapping data from Redis/Mapping Service for merchantId : {}",
                        subscriptionResponse.getMid(), e);
            }
            if (StringUtils.isBlank(alipayMerchantId)) {
                throw new TheiaServiceException("Could not map merchant id, due to merchant id is null or blank ");
            }

            LOGGER.info("set dummy alipayMerchantId in transactionStatus:{}", alipayMerchantId);
            paramMap.put(TheiaConstant.ExtendedInfoPay.ALIPAY_MERCHANT_ID, new String[] { alipayMerchantId });
        } else {
            paramMap.put(MERCHANT_ID, new String[] { transactionStatusRequestBody.getMerchantId() });
        }
        paramMap.put(PAYMENT_MODE, new String[] { transactionStatusRequestBody.getPaymentMode() });
        paramMap.put(REQUEST_TYPE, new String[] { orderDetail.getRequestType() });
        if (StringUtils.isNotBlank(paymentOption)
                && (PaymentOption.UPI.getPaymentOption().equalsIgnoreCase(paymentOption) || PaymentOption.UPI_INTENT
                        .getPaymentOption().equalsIgnoreCase(paymentOption))) {
            paramMap.put(TXN_TOKEN, new String[] { txnToken });
            paramMap.put(IS_ASYNC_TXN_STATUS_FLOW, new String[] { TRUE });
            paramMap.put(PAYMENT_OPTION, new String[] { PaymentOption.UPI.getPaymentOption() });
        }
        if ((ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType()) || ERequestType.NATIVE_MF_SIP
                .getType().equals(orderDetail.getRequestType()))
                && subscriptionResponse != null
                && SubsPaymentMode.BANK_MANDATE.toString().equals(transactionStatusRequestBody.getPaymentMode())) {
            paramMap.put(SUBSCRIPTION_ID, new String[] { subscriptionResponse.getSubscriptionId() });
            paramMap.put(CUSTOMER_ID, new String[] { subscriptionResponse.getCustId() });
        }
        paramMap.put(ORDER_ID, new String[] { orderDetail.getOrderId() });
        paramMap.put(TRANSACTION_STATUS_JSON_REQUEST, new String[] { "true" });
        paramMap.put(HEADER_WORKFLOW, new String[] { transactionStatusRequest.getHead().getWorkFlow() });
        ModifiableHttpServletRequest modifiableHttpServletRequest = new ModifiableHttpServletRequest(
                transactionStatusRequest.getHttpServletRequest(), paramMap);
        return modifiableHttpServletRequest;
    }

    private boolean isRetryAllowedInCallBackUrl(String callBackUrl) {
        return StringUtils.contains(callBackUrl, "retryAllowed=true");
    }

    private void throwException(ResultCode resultCode, InitiateTransactionRequestBody orderDetail) {
        throw new NativeFlowException.ExceptionBuilder(resultCode).isHTMLResponse(false).isNativeJsonRequest(true)
                .setOrderDetail(orderDetail).build();
    }

    private boolean isValid(TransactionStatusRequest transactionStatusRequest) {
        GenericBeanValidator<TransactionStatusRequest> validator = new GenericBeanValidator<>(transactionStatusRequest);
        if (validator.validate()) {
            return true;
        } else {
            LOGGER.error("Invalid Transaction Status Request:{}", transactionStatusRequest);
            return false;
        }
    }

    private void setTransIdCashierRequestIdPaymentModeInRequest(final TransactionStatusRequest request, String txnToken) {
        String transId = nativeSessionUtil.getTxnId(txnToken);
        String cashierRequestId = nativeSessionUtil.getCashierRequestId(txnToken);
        String paymentMode = nativeSessionUtil.getPaymentTypeId(txnToken);
        if (StringUtils.isBlank(transId) || StringUtils.isBlank(cashierRequestId) || StringUtils.isBlank(paymentMode)) {
            LOGGER.info("TransId {}, cashierRequestId {}, paymentMode {}", transId, cashierRequestId, paymentMode);
            throw SessionExpiredException.getException();
        }
        request.getBody().setTransId(transId);
        request.getBody().setCashierRequestId(cashierRequestId);
        request.getBody().setPaymentMode(paymentMode);
    }

}
