package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.dynamicwrapper.service.IWrapperService;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.request.CloseOrderRequest;
import com.paytm.pgplus.request.CloseOrderRequestBody;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.response.CloseOrderResponse;
import com.paytm.pgplus.response.CloseOrderResponseBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.response.SecureResponseHeader;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.CloseOrderStatus;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.CancelTransRequest;
import com.paytm.pgplus.theia.models.CancelTransResponse;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.OrderIdDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.ICloseOrderService;
import com.paytm.pgplus.theia.utils.DynamicWrapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.helper.TheiaResponseGeneratorHelper;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.RESELLER_PARENT_MID;
import static com.paytm.pgplus.theia.enums.CloseOrderStatus.*;
import static com.paytm.pgplus.theia.offline.enums.ResultCode.*;

@NativeControllerAdvice
@RestController
@RequestMapping("api")
public class NativeCloseOrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCloseOrderController.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeCloseOrderController.class);

    @Autowired
    @Qualifier("closeOrderServiceImpl")
    private ICloseOrderService closeOrderServiceImpl;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("merchantResponseService")
    MerchantResponseService merchantResponseService;

    @Autowired
    private DynamicWrapperUtil dynamicWrapperUtil;

    @Autowired
    @Qualifier("wrapperImpl")
    IWrapperService wrapperService;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private TheiaResponseGeneratorHelper theiaResponseGeneratorHelper;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    private ValidationService validationService;

    @ApiOperation(value = "closeOrder", notes = "To close pending orders(Acquiring/TopUp) on user drop")
    @RequestMapping(value = "/v1/closeOrder", method = { RequestMethod.POST })
    public CloseOrderResponse cancelNativeTransOnUserDrop(
            @ApiParam(required = true) @RequestBody CloseOrderRequest request) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Native request received for API: /closeOrder is: {}", request);
        CancelTransResponse cancelTransResponse = null;
        try {
            CloseOrderRequestBody body = request.getBody();
            CancelTransRequest cancelTransRequest = new CancelTransRequest(body.getmId(), body.getOrderId(),
                    body.getUserToken(), body.isForceClose());
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_MID_ENABLE, false)
                    && !validationService.validateMid(body.getmId())) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid MID : " + body.getmId());
            }
            if (ff4JUtils.isFeatureEnabled(THEIA_VALIDATE_ORDER_ID_ENABLE, false)
                    && !validationService.validateOrderId(body.getOrderId())) {
                throw new TheiaServiceException("CRITICAL_ERROR : Invalid ORDER_ID : " + body.getOrderId());
            }
            setMDC(cancelTransRequest);
            final EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(getHttpServletRequest());
            cancelTransResponse = closeOrderServiceImpl.processCancelOrderRequest(cancelTransRequest, envInfo);
            if (cancelTransResponse == null) {
                cancelTransResponse = setDefaultResponse();
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while processing close order request {}", e);
            cancelTransResponse = setDefaultResponse();
        } finally {
            LOGGER.info("Total time taken for NativeCloseOrderController is {} ms", System.currentTimeMillis()
                    - startTime);
        }
        return generateCancelTxnResponse(cancelTransResponse, request.getHead());
    }

    @RequestMapping(value = "/v2/closeOrder", method = { RequestMethod.POST })
    public ResponseEntity closeOrderV2(
            @ApiParam(required = true) @RequestBody com.paytm.pgplus.request.v2.CloseOrderRequest request) {

        long startTime = System.currentTimeMillis();
        LOGGER.info("Native request received for API: /v2/closeOrder is: {}", request);

        CancelTransResponse cancelTransResponse = null;

        SecureRequestHeader head = request.getHead();
        com.paytm.pgplus.request.v2.CloseOrderRequestBody body = request.getBody();
        String txnToken = null;
        InitiateTransactionRequestBody orderDetail = null;
        HttpStatus httpStatusCode = HttpStatus.OK;
        EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(getHttpServletRequest());
        String mid = null;
        String orderId = null;

        try {

            mid = body.getMid();
            orderId = getOrderIdForCloseOrderV2(body);
            nativeValidationService.validateMidOrderId(mid, orderId);

            /*
             * validate request
             */
            if (!nativeValidationService.validateRequest(TokenType.getType(head.getTokenType()), head.getToken(), body,
                    mid)) {

                cancelTransResponse = new CancelTransResponse();
                ResultCode resultCode = REQUEST_PARAMS_VALIDATION_EXCEPTION;

                if (TokenType.TXN_TOKEN.getType().equals(head.getTokenType())) {
                    resultCode = SESSION_EXPIRED_EXCEPTION;
                }

                if (TokenType.CHECKSUM.getType().equals(head.getTokenType())) {
                    resultCode = INVALID_CHECKSUM;
                }

                if (TokenType.SSO.getType().equals(head.getTokenType())) {
                    resultCode = INVALID_SSO_TOKEN;
                    httpStatusCode = HttpStatus.UNAUTHORIZED;
                }

                cancelTransResponse.setStatus(resultCode.getResultStatus());
                cancelTransResponse.setStatusMessage(resultCode.getResultMsg());
                cancelTransResponse.setStatusCode(resultCode.getResultCodeId());

                return sendResponseCloseOrderV2(
                        createCloseOrderResponseV2(cancelTransResponse, orderDetail, txnToken,
                                body.getBackButtonPress()), httpStatusCode);
            }

            if (StringUtils.isNotBlank(body.getRefId())) {
                body.setOrderId(MDC.get(TheiaConstant.RequestParams.ORDER_ID));
            }

            txnToken = getTxnTokenCloseOrderV2(request);
            orderDetail = nativeSessionUtil.getOrderDetail(txnToken);
            nativeValidationService.validateMidOrderIdinRequest(mid, orderId, orderDetail.getMid(),
                    orderDetail.getOrderId());

            /*
             * check ssoToken is same as the cached with orderDetail
             */
            if (TokenType.SSO.getType().equals(head.getTokenType())) {
                if (!validateSSOTokenOnTxnToken(txnToken, orderDetail, head.getToken())) {
                    cancelTransResponse = setCancelTransResponse(INVALID_REQUEST);
                    return sendResponseCloseOrderV2(
                            createCloseOrderResponseV2(cancelTransResponse, orderDetail, txnToken,
                                    body.getBackButtonPress()), httpStatusCode);
                }
            }

            String transId = nativeSessionUtil.getTxnId(txnToken);
            if (StringUtils.isBlank(transId)) {
                LOGGER.error("transId is null or blank");
                cancelTransResponse = setCancelTransResponse(INVALID_ORDER_STATUS);
            } else {
                /*
                 * this means order has been created for this orderId at
                 * platform side
                 */
                cancelTransResponse = closeOrderServiceImpl.processCancelOrderRequest(new CancelTransRequest(mid,
                        orderId, null, BooleanUtils.toBoolean(body.getForceClose())), envInfo);
            }
        } catch (MidDoesnotMatchException | OrderIdDoesnotMatchException midOrderIdMismatch) {
            LOGGER.error("midOrderIdMismatch while processing close order request ");
            cancelTransResponse = setCancelTransResponse(INVALID_REQUEST);
        } catch (SessionExpiredException see) {
            LOGGER.error("SessionExpiredException occurred while processing close order request ", see);
            ResultCode sessionExpiredResultCode = SESSION_EXPIRED_EXCEPTION;
            cancelTransResponse = new CancelTransResponse();
            cancelTransResponse.setStatus(sessionExpiredResultCode.getResultStatus());
            cancelTransResponse.setStatusMessage(sessionExpiredResultCode.getResultMsg());
            cancelTransResponse.setStatusCode(sessionExpiredResultCode.getResultCodeId());
        } catch (Exception e) {
            LOGGER.error("Exception occurred while processing close order request ", e);
            cancelTransResponse = setCancelTransResponse(INTERNAL_PROCESSING_ERROR);
        } finally {
            LOGGER.info("Total time taken for /v2/closeOrder NativeCloseOrderController is {} ms",
                    System.currentTimeMillis() - startTime);
        }

        ResponseEntity responseEntity = sendResponseCloseOrderV2(
                createCloseOrderResponseV2(cancelTransResponse, orderDetail, txnToken, body.getBackButtonPress()),
                httpStatusCode);
        /*
         * deleting keys if closeOrder is success
         */
        if (cancelTransResponse != null && StringUtils.equals("S", cancelTransResponse.getStatus())) {
            // for cancelled link payments, do not remove redis keys here as it
            // will be used later in
            // linkPaymentRedirect and will be removed there only
            String workflow = nativeSessionUtil.getWorkflow(txnToken);
            EXT_LOGGER.customInfo("Workflow in Session: {}", workflow);
            if (!(ERequestType.LINK_BASED_PAYMENT.getType().equalsIgnoreCase(orderDetail.getRequestType())
                    || ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equalsIgnoreCase(orderDetail.getRequestType()) || BizConstant.CHECKOUT
                        .equalsIgnoreCase(workflow))) {
                nativeSessionUtil.deleteKey(txnToken, nativeSessionUtil.getMidOrderIdKeyForRedis(mid, orderId));
            }
            if (BizConstant.CHECKOUT.equalsIgnoreCase(workflow)) {
                nativeSessionUtil.setOrderClosedonCheckoutJS(txnToken, true);
            }
        }
        return responseEntity;
    }

    private ResponseEntity sendResponseCloseOrderV2(Object body, HttpStatus httpStatus) {
        LOGGER.info("sending responseCloseOrderV2: {}", body);
        return new ResponseEntity<>(body, httpStatus);
    }

    private boolean validateSSOTokenOnTxnToken(String txnToken, InitiateTransactionRequestBody orderDetail, String token) {
        return StringUtils.equals(orderDetail.getPaytmSsoToken(), token)
                || StringUtils.equals(token, nativeSessionUtil.getSsoToken(txnToken));
    }

    private com.paytm.pgplus.response.v2.CloseOrderResponse createCloseOrderResponseV2(
            CancelTransResponse cancelTransResponse, InitiateTransactionRequestBody orderDetail, String txnToken,
            Boolean backButtonPress) {

        com.paytm.pgplus.response.v2.CloseOrderResponse closeOrderResponse = new com.paytm.pgplus.response.v2.CloseOrderResponse();
        com.paytm.pgplus.response.v2.CloseOrderResponseBody closeOrderResponseBody = new com.paytm.pgplus.response.v2.CloseOrderResponseBody();

        closeOrderResponseBody.setResultInfo(new ResultInfo(cancelTransResponse.getStatus(), cancelTransResponse
                .getStatusCode(), cancelTransResponse.getStatusMessage()));
        SecureResponseHeader headResponse = new SecureResponseHeader();
        headResponse.setResponseTimestamp(String.valueOf(System.currentTimeMillis()));
        headResponse.setVersion("v2");

        try {

            closeOrderResponseBody.setResultInfo(new ResultInfo(cancelTransResponse.getStatus(), cancelTransResponse
                    .getStatusCode(), cancelTransResponse.getStatusMessage()));

            if (orderDetail != null) {
                Map<String, Object> orderExtraParamsMap = orderDetail.getExtraParamsMap();
                Map<String, String> txnInfo = new TreeMap<>();
                PaymentRequestBean paymentRequestData = new PaymentRequestBean(getHttpServletRequest());
                Object object = theiaTransactionalRedisUtil.get(orderDetail.getMid() + "#" + orderDetail.getOrderId());
                if (object != null) {
                    Map<String, Object> extraParamsMap = (Map<String, Object>) object;
                    paymentRequestData.setExtraParamsMap(extraParamsMap);
                }

                if (dynamicWrapperUtil.isDynamicWrapperEnabled()
                        && (dynamicWrapperUtil.isDynamicWrapperConfigPresent(orderDetail.getMid(),
                                API.PROCESS_TRANSACTION, PayloadType.RESPONSE) || (paymentRequestData
                                .getExtraParamsMap() != null
                                && (StringUtils.equals((String) paymentRequestData.getExtraParamsMap()
                                        .get(WRAPPER_NAME), SBMOPS_WRAPPER)) && dynamicWrapperUtil
                                    .isDynamicWrapperConfigPresent(
                                            (String) paymentRequestData.getExtraParamsMap().get(AGG_MID_WRAPPER),
                                            API.PROCESS_TRANSACTION, PayloadType.RESPONSE)))) {
                    LOGGER.info("Sending Respnse through dynamic wrapper");

                    TransactionResponse transactionResponse = theiaResponseGenerator
                            .createTransactionResponseForNativeCloseOrder(orderDetail, paymentRequestData,
                                    nativeSessionUtil.getTxnId(txnToken));
                    try {
                        if (transactionResponse.getExtraParamsMap() != null
                                && transactionResponse.getExtraParamsMap().get("transitionPageRequired") != null) {
                            String mid = null;
                            if (StringUtils.equals((String) paymentRequestData.getExtraParamsMap().get(WRAPPER_NAME),
                                    SBMOPS_WRAPPER)) {

                                mid = (String) paymentRequestData.getExtraParamsMap().get(AGG_MID_WRAPPER);
                            } else {
                                mid = transactionResponse.getMid();
                            }
                            Map<String, Object> merchantResponseParams = wrapperService.wrapResponse(
                                    transactionResponse, mid, API.PROCESS_TRANSACTION);

                            if (Boolean.parseBoolean((String) transactionResponse.getExtraParamsMap().get(
                                    "transitionPageRequired"))) {
                                String wrapperTransitionPageEncodedJson = theiaResponseGenerator
                                        .wrapperTransitionPageEncodedJson(merchantResponseParams);
                                txnInfo.put("PUSH_JSON_DATA", wrapperTransitionPageEncodedJson);
                                closeOrderResponseBody.setCallBackUrl(ConfigurationUtil
                                        .getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_PATH)
                                        + TheiaConstant.ExtraConstants.DYNAMIC_WRAPPER_PATH_URI);
                            } else if (!Boolean.parseBoolean((String) transactionResponse.getExtraParamsMap().get(
                                    "transitionPageRequired"))) {
                                // Field which needs to be posted on merchant
                                // callback
                                for (Map.Entry responseElement : merchantResponseParams.entrySet()) {
                                    String callbackFieldValue = (String) responseElement.getKey();
                                    String callbackData = (String) responseElement.getValue();
                                    txnInfo.put(callbackFieldValue, callbackData);
                                }
                                closeOrderResponseBody.setCallBackUrl(transactionResponse.getCallbackUrl());

                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Dynamic Wrapper Exception ", e);
                    }
                } else if (orderExtraParamsMap != null
                        && (StringUtils.isNotEmpty((String) orderExtraParamsMap.get(SHOPIFY_DOMAIN)))
                        && backButtonPress != null && Boolean.TRUE.equals(backButtonPress)) {
                    closeOrderResponseBody.setCallBackUrl(orderDetail.getCallbackUrl());
                } else if (orderExtraParamsMap != null
                        && StringUtils.isNotEmpty((String) orderExtraParamsMap.get(SHOPIFY_DOMAIN))
                        && dynamicWrapperUtil.isDynamicWrapperConfigPresent(
                                (String) orderExtraParamsMap.get(RESELLER_PARENT_MID), API.PROCESS_TRANSACTION,
                                PayloadType.RESPONSE)) {
                    TransactionResponse transactionResponse = theiaResponseGenerator
                            .createTransactionResponseForNativeCloseOrder(orderDetail, paymentRequestData,
                                    nativeSessionUtil.getTxnId(txnToken));
                    Map<String, Object> merchantResponseParams = wrapperService.wrapResponse(transactionResponse,
                            (String) orderExtraParamsMap.get(RESELLER_PARENT_MID), API.PROCESS_TRANSACTION);
                    String wrapperTransitionPageEncodedJson = theiaResponseGenerator
                            .wrapperTransitionPageEncodedJson(merchantResponseParams);
                    txnInfo.put("PUSH_JSON_DATA", wrapperTransitionPageEncodedJson);
                } else if (orderExtraParamsMap != null
                        && StringUtils.equals((String) orderExtraParamsMap.get(WRAPPER_NAME), WIX_WRAPPER)
                        && backButtonPress != null && Boolean.TRUE.equals(backButtonPress)) {
                    if (StringUtils.isNotBlank(orderDetail.getCallbackUrl())
                            && orderDetail.getCallbackUrl().contains(WIX_CALLBACK_ACTION_METHOD)) {
                        orderDetail.setCallbackUrl(orderDetail.getCallbackUrl().substring(0,
                                orderDetail.getCallbackUrl().lastIndexOf("|")));
                    }
                    closeOrderResponseBody.setCallBackUrl(orderDetail.getCallbackUrl());
                    closeOrderResponseBody.setIsActionMethodGet(true);
                } else if (orderExtraParamsMap != null
                        && StringUtils.equals((String) orderExtraParamsMap.get(WRAPPER_NAME), WIX_WRAPPER)
                        && dynamicWrapperUtil.isDynamicWrapperConfigPresent(
                                (String) orderExtraParamsMap.get(RESELLER_PARENT_MID), API.PROCESS_TRANSACTION,
                                PayloadType.RESPONSE)) {
                    TransactionResponse transactionResponse = theiaResponseGenerator
                            .createTransactionResponseForNativeCloseOrder(orderDetail, paymentRequestData,
                                    nativeSessionUtil.getTxnId(txnToken));
                    if (StringUtils.isNotBlank(transactionResponse.getCallbackUrl())
                            && transactionResponse.getCallbackUrl().contains(WIX_CALLBACK_ACTION_METHOD)) {
                        transactionResponse.setCallbackUrl(transactionResponse.getCallbackUrl().substring(0,
                                transactionResponse.getCallbackUrl().lastIndexOf("|")));
                    }
                    closeOrderResponseBody.setCallBackUrl(transactionResponse.getCallbackUrl());
                    closeOrderResponseBody.setIsActionMethodGet(true);
                } else {
                    TransactionResponse transactionResponse = theiaResponseGenerator
                            .createTransactionResponseForNativeCloseOrder(orderDetail, paymentRequestData,
                                    nativeSessionUtil.getTxnId(txnToken));

                    boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(orderDetail
                            .getMid());
                    boolean encRequestEnabled = merchantPreferenceService.isEncRequestEnabled(orderDetail.getMid());
                    if ((isAES256Encrypted || encRequestEnabled)
                            && ff4JUtils.isFeatureEnabledOnMid(orderDetail.getMid(), THEIA_ENCRYPTED_RESPONSE_TO_JSON,
                                    false)) {
                        LOGGER.info("Feature encryptedResponseToJson is enabled");
                        merchantResponseService.encryptedResponseJson(orderDetail.getMid(), txnInfo,
                                transactionResponse, isAES256Encrypted, encRequestEnabled);
                    } else
                        merchantResponseService.makeReponseToMerchantEnhancedNative(transactionResponse, txnInfo);

                    closeOrderResponseBody.setCallBackUrl(transactionResponse.getCallbackUrl());
                }
                closeOrderResponseBody.setTxnInfo(txnInfo);
            }
        } catch (Exception e) {
            LOGGER.error("error ", e);
        }

        closeOrderResponse.setHead(headResponse);
        closeOrderResponse.setBody(closeOrderResponseBody);
        return closeOrderResponse;
    }

    private CancelTransResponse setCancelTransResponse(CloseOrderStatus closeOrderStatus) {
        if (closeOrderStatus == null) {
            return null;
        }
        CancelTransResponse cancelTransResponse = new CancelTransResponse();
        cancelTransResponse.setStatus(closeOrderStatus.getStatus());
        cancelTransResponse.setStatusMessage(closeOrderStatus.getStatusMessage());
        cancelTransResponse.setStatusCode(closeOrderStatus.getStatusCode());
        return cancelTransResponse;
    }

    private String getOrderIdForCloseOrderV2(com.paytm.pgplus.request.v2.CloseOrderRequestBody body) {
        if (body == null) {
            return null;
        }
        if (StringUtils.isNotBlank(body.getOrderId())) {
            return body.getOrderId();
        }
        if (StringUtils.isNotBlank(body.getRefId())) {
            String orderId = nativeSessionUtil.getOrderIdMappedToRefId(body.getRefId());
            ;
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, orderId);
            return orderId;
        }
        return null;
    }

    private String getTxnTokenCloseOrderV2(com.paytm.pgplus.request.v2.CloseOrderRequest request) {
        SecureRequestHeader head = request.getHead();
        com.paytm.pgplus.request.v2.CloseOrderRequestBody body = request.getBody();

        TokenType tokenType = TokenType.getType(head.getTokenType());

        if (TokenType.TXN_TOKEN == tokenType) {
            return head.getToken();
        }

        return nativeSessionUtil.getTxnToken(body.getMid(), body.getOrderId());
    }

    private void setMDC(CancelTransRequest cancelTransRequest) {
        MDC.clear();
        if ((cancelTransRequest != null) && StringUtils.isNotBlank(cancelTransRequest.getMerchantId())) {
            MDC.put(TheiaConstant.RequestParams.MID, cancelTransRequest.getMerchantId());
        }
        if ((cancelTransRequest != null) && StringUtils.isNotBlank(cancelTransRequest.getOrderId())) {
            MDC.put(TheiaConstant.RequestParams.ORDER_ID, cancelTransRequest.getOrderId());
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        return httpServletRequest;
    }

    private CancelTransResponse setDefaultResponse() {
        CancelTransResponse cancelTransResponse = new CancelTransResponse();
        cancelTransResponse.setStatus(INTERNAL_PROCESSING_ERROR.getStatus());
        cancelTransResponse.setStatusMessage(INTERNAL_PROCESSING_ERROR.getStatusMessage());
        cancelTransResponse.setStatusCode(INTERNAL_PROCESSING_ERROR.getStatusCode());
        return cancelTransResponse;
    }

    private CloseOrderResponse generateCancelTxnResponse(CancelTransResponse cancelTransResponse,
            SecureRequestHeader requestHeader) {
        CloseOrderResponseBody responseBody = new CloseOrderResponseBody();
        responseBody.setResultInfo(new ResultInfo(cancelTransResponse.getStatus(), cancelTransResponse.getStatusCode(),
                cancelTransResponse.getStatusMessage()));
        SecureResponseHeader responseHeader = new SecureResponseHeader();
        responseHeader.setResponseTimestamp(String.valueOf(System.currentTimeMillis()));
        responseHeader.setVersion(requestHeader.getVersion());
        CloseOrderResponse response = new CloseOrderResponse(responseHeader, responseBody);
        LOGGER.info("Native response returned for API: /closeOrder is: {}", response);
        return response;
    }
}
