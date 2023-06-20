package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.looperclient.async.TaskContainer;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.cache.IFlushRedisKeysService;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.EnhancedCashierFlow;
import com.paytm.pgplus.theia.constants.TheiaConstant.RetryConstants;
import com.paytm.pgplus.theia.enums.UPIPollStatus;
import com.paytm.pgplus.theia.exceptions.*;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusRequest;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.TransactionStatusResponse;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.UpiTransactionStatusResponse;
import com.paytm.pgplus.theia.nativ.model.transactionStatus.UpiTransactionStatusResponseBody;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.impl.TransactionStatusServiceImpl;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRANSACTION_RESPONSE_OBJECT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_TRANSACTION_STATUS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPCODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPMSG;

@Controller
public class TransactionStatusController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStatusController.class);

    @Autowired
    @Qualifier(value = "transactionStatusServiceImpl")
    private TransactionStatusServiceImpl transactionStatusServiceImpl;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("callbackTaskContainer")
    private TaskContainer taskContainer;

    @Autowired
    @Qualifier("flushRedisKeysDataService")
    private IFlushRedisKeysService flushRedisKeysService;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    private static final JSONObject RETRY = new JSONObject();
    private static final JSONObject STOP_POLLING = new JSONObject();

    private static final long DEFAULT_TIMEOUT_IN_MS = 22000;

    static {
        RETRY.put("POLL_STATUS", UPIPollStatus.POLL_AGAIN);
        STOP_POLLING.put("POLL_STATUS", UPIPollStatus.STOP_POLLING);
    }

    @RequestMapping(value = "/transactionStatus")
    public void transactionStatus(HttpServletRequest request, Model model, Locale locale, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Request received for transactionStatus");
        processTxnStatusRequest(request, response);
        return;
    }

    @RequestMapping(value = "v1/transactionStatus")
    public void transactionStatusV1(HttpServletRequest request, Model model, Locale locale, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Request received for v1/transactionStatus");
        if (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
            processJsonTxnStatusRequest(request, response);
            return;
        } else {
            processTxnStatusRequest(request, response);
            return;
        }

    }

    @RequestMapping(value = "/transactionStatusAsync")
    public DeferredResult<ResponseEntity<?>> asyncTransactionStatus(HttpServletRequest request, Model model,
            Locale locale, HttpServletResponse response) {
        LOGGER.info("Request received async-deferredResult TransactionStatus request");
        return generateDeferredAndProcessRequest(request, response);
    }

    @RequestMapping(value = "v1/transactionStatusAsync")
    public DeferredResult<ResponseEntity<?>> asyncTransactionStatusV1(HttpServletRequest request, Model model,
            Locale locale, HttpServletResponse response) {
        LOGGER.info("Request received async-deferredResult v1 TransactionStatus request");
        return generateDeferredAndProcessRequest(request, response);
    }

    private DeferredResult<ResponseEntity<?>> generateDeferredAndProcessRequest(HttpServletRequest request,
            HttpServletResponse response) {
        request.setAttribute(org.apache.catalina.Globals.ASYNC_SUPPORTED_ATTR, true);
        long timeout = NumberUtils.toLong(
                ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.TRANSACTION_STATUS_TIMEOUT),
                DEFAULT_TIMEOUT_IN_MS);
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>(timeout);
        output.onCompletion(() -> LOGGER.info("result completion !!! "));
        output.onTimeout(() -> {
            LOGGER.info("DeferredResult is not set yet. Cleaning up task cache.");
            taskContainer.cleanUpCache();
            if (!output.hasResult()) {
                LOGGER.warn("Still no result, returning error page.");
                output.setResult(createResponseEntity(request, theiaViewResolverService.returnErrorPage(request),
                        HttpStatus.TEMPORARY_REDIRECT));
            }
        });

        try {
            processAsyncTxnStatusRequest(output, request, response);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            output.setResult(createResponseEntity(request, theiaViewResolverService.returnErrorPage(request),
                    HttpStatus.TEMPORARY_REDIRECT));
        }

        LOGGER.info("Servlet Thread Freed For TransId: {}", request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID));
        return output;
    }

    private void processAsyncTxnStatusRequest(DeferredResult<ResponseEntity<?>> output, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        transactionStatusServiceImpl.getAsyncCashierResponseWrapper(
                request,
                response,
                responseData -> {
                    try {
                        if (responseData == null) {
                            throw new TheiaControllerException("Cashier Payment Response data not fetched");
                        } else if (null != responseData.get(EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)
                                && RetryConstants.YES.equals(responseData
                                        .get(EnhancedCashierFlow.ENHANCED_CASHIER_FLOW))
                                && RetryConstants.YES.equals(responseData.get(RetryConstants.IS_RETRY))) {
                            output.setResult(ResponseEntity.status(HttpStatus.OK).body(
                                    responseData.get(TheiaConstant.RetryConstants.RESPONSE_PAGE).getBytes()));
                            return;
                        } else if (TheiaConstant.RetryConstants.YES.equals(responseData
                                .get(TheiaConstant.RetryConstants.IS_REQUEST_ALREADY_DISPATCH))) {
                            LOGGER.info("Request already Dispatched");
                            return;
                        } else if (TheiaConstant.RetryConstants.YES.equals(responseData
                                .get(TheiaConstant.RetryConstants.IS_RETRY))) {
                            LOGGER.info("Sending Retry request");
                            String retryRequest = transactionStatusServiceImpl.generateRetryRequest(request,
                                    responseData);

                            output.setResult(createRetryResponseEntity(request, retryRequest, HttpStatus.FOUND));

                            return;
                        } else {
                            LOGGER.info("Transaction successful, sending response body.");
                            output.setResult(ResponseEntity.status(HttpStatus.OK).body(
                                    responseData.get(TheiaConstant.RetryConstants.RESPONSE_PAGE).getBytes()));
                            return;
                        }
                    } catch (Exception e) {
                        LOGGER.error("SYSTEM_ERROR : ", e);
                    } finally {
                        LOGGER.info("Total time taken for {} is {} ms", "TransactionStatusController",
                                System.currentTimeMillis() - startTime);
                    }

                    output.setResult(createResponseEntity(request, theiaViewResolverService.returnErrorPage(request),
                            HttpStatus.TEMPORARY_REDIRECT));
                    return;
                });
    }

    private void processTxnStatusRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Map<String, String> data = null;
        try {
            String paymentMid = null;
            String transId = request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID);
            if (StringUtils.isNotBlank(transId)) {
                paymentMid = nativeSessionUtil.getFieldValue(transId,
                        com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.PAYMENT_MID);
            }
            if (StringUtils.isNotBlank(paymentMid)) {
                String alipayMerchantId = StringUtils.EMPTY;
                try {
                    MappingMerchantData mappingMerchantData = merchantMappingService.getMappingMerchantData(paymentMid);
                    if (mappingMerchantData != null) {
                        alipayMerchantId = mappingMerchantData.getAlipayId();
                    }
                } catch (Exception e) {
                    LOGGER.error(
                            "Exception occurred while fetching Merchant mapping data from Redis/Mapping Service for merchantId : {}",
                            paymentMid, e);
                }
                if (StringUtils.isBlank(alipayMerchantId)) {
                    throw new TheiaServiceException("Could not map merchant id, due to merchant id is null or blank ");
                }

                LOGGER.info("set dummy alipayMerchantId in transactionStatus:{}", alipayMerchantId);
                request.setAttribute(TheiaConstant.ExtendedInfoPay.ALIPAY_MERCHANT_ID, alipayMerchantId);
            }
            data = transactionStatusServiceImpl.getCashierResponseWrapper(request, response);

            if (data == null) {
                throw new TheiaControllerException("Cashier Payment Response data not fetched");
            } else if (data != null && RetryConstants.YES.equals(data.get(RetryConstants.IS_REQUEST_ALREADY_DISPATCH))) {
                return;
            } else if (null != data.get(EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)
                    && RetryConstants.YES.equals(data.get(EnhancedCashierFlow.ENHANCED_CASHIER_FLOW))
                    && RetryConstants.YES.equals(data.get(RetryConstants.IS_RETRY))) {
                response.setContentType("text/html");
                response.getOutputStream().write(data.get(RetryConstants.RESPONSE_PAGE).getBytes());
                return;
            } else if (RetryConstants.YES.equals(data.get(RetryConstants.IS_RETRY))) {
                String retryRequest = transactionStatusServiceImpl.generateRetryRequest(request, data);
                response.sendRedirect(retryRequest);
                return;
            } else {
                response.getOutputStream().write(data.get(RetryConstants.RESPONSE_PAGE).getBytes());
                return;
            }
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : {}", e);
        } finally {
            try {
                if (data != null && data.get(TRANSACTION_RESPONSE_OBJECT) != null) {
                    TransactionResponse transactionResponse = JsonMapper.mapJsonToObject(
                            data.get(TRANSACTION_RESPONSE_OBJECT), TransactionResponse.class);
                    if (transactionResponse != null
                            && StringUtils.equals(ExternalTransactionStatus.TXN_SUCCESS.name(),
                                    transactionResponse.getTransactionStatus()))
                        flushRedisKeysService.flushRedisKeys(request);
                    if (transactionResponse != null) {
                        EventUtils.logResponseCode(V1_TRANSACTION_STATUS, EventNameEnum.TXNINFO_CODE_SENT,
                                transactionResponse.getResponseCode(), transactionResponse.getResponseMsg());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Could not flush card info");
            }
            LOGGER.info("Total time taken for {} is {} ms", "TransactionStatusController", System.currentTimeMillis()
                    - startTime);
        }

        request.getRequestDispatcher(theiaViewResolverService.returnErrorPage(request)).forward(request, response);
        return;
    }

    private void processJsonTxnStatusRequest(HttpServletRequest request, HttpServletResponse response)
            throws NativeFlowException {

        long startTime = System.currentTimeMillis();
        TransactionStatusRequest transactionStatusRequest = null;
        try {
            String requestData = IOUtils.toString(request.getInputStream(), Charsets.UTF_8.name());
            if (StringUtils.isNotBlank(requestData)) {
                transactionStatusRequest = JsonMapper.mapJsonToObject(requestData, TransactionStatusRequest.class);
                transactionStatusRequest.setHttpServletRequest(request);
                TransactionStatusResponse transactionStatusResponse = processJsonTxnStatusRequest(transactionStatusRequest);
                try {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("RESPONSE_STATUS", transactionStatusResponse.getBody().getResultInfo()
                            .getResultStatus());
                    responseMap.put("RESPONSE_MESSAGE", transactionStatusResponse.getBody().getResultInfo()
                            .getResultMsg());
                    statsDUtils.pushResponse("v1/transactionStatus", responseMap);
                } catch (Exception exception) {
                    LOGGER.error("Error in pushing response message " + "v1/transactionStatus" + "to grafana",
                            exception);
                }

                LOGGER.info("Final Response sent in /theia/v1/transactionStatus is : {}", transactionStatusResponse);
                String txnResponse = JsonMapper.mapObjectToJson(transactionStatusResponse);
                if (transactionStatusResponse != null && transactionStatusResponse.getBody() != null) {
                    if (transactionStatusResponse.getBody().getResultInfo() != null) {
                        EventUtils.logResponseCode(V1_TRANSACTION_STATUS, EventNameEnum.RESPONSE_CODE_SENT,
                                transactionStatusResponse.getBody().getResultInfo().getResultCode(),
                                transactionStatusResponse.getBody().getResultInfo().getResultMsg());
                    }
                    if (transactionStatusResponse.getBody().getTxnInfo() != null) {
                        EventUtils.logResponseCode(V1_TRANSACTION_STATUS, EventNameEnum.TXNINFO_CODE_SENT,
                                transactionStatusResponse.getBody().getTxnInfo().get(RESPCODE),
                                transactionStatusResponse.getBody().getTxnInfo().get(RESPMSG));
                    }
                }
                response.setContentType("application/json");
                response.getOutputStream().print(txnResponse);
            } else {
                LOGGER.error("Invalid request for/theia/v1/transactionStatus");
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PARAM).isHTMLResponse(false)
                        .isNativeJsonRequest(true).build();
            }
        } catch (NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in /theia/v1/transactionStatus {}", nfe);
            throw nfe;
        } catch (Exception ex) {
            LOGGER.error("Exception in /theia/v1/transactionStatus {}", ex);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isHTMLResponse(false)
                    .isNativeJsonRequest(true).build();
        } finally {
            LOGGER.info("Total time taken by /theia/v1/transactionStatus is {} ms", System.currentTimeMillis()
                    - startTime);
        }
        return;
    }

    private TransactionStatusResponse processJsonTxnStatusRequest(TransactionStatusRequest transactionStatusRequest)
            throws Exception {

        LOGGER.info("JSON request recieved for transaction status {}", transactionStatusRequest);
        TransactionStatusResponse transactionStatusResponse = null;
        IRequestProcessor<TransactionStatusRequest, TransactionStatusResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.TRANSACTION_STATUS);
        transactionStatusResponse = requestProcessor.process(transactionStatusRequest);
        return transactionStatusResponse;
    }

    private UpiTransactionStatusResponse getUpiTransactionStatusResponse(HttpServletRequest request,
            UPIPollStatus upiPollStatus) {
        UpiTransactionStatusResponse upiTransactionStatusResponse = new UpiTransactionStatusResponse();
        ResponseHeader responseHeader = new ResponseHeader();
        UpiTransactionStatusResponseBody upiTransactionStatusResponseBody = new UpiTransactionStatusResponseBody();
        upiTransactionStatusResponseBody.setPollStatus(upiPollStatus);
        upiTransactionStatusResponse.setHead(responseHeader);
        upiTransactionStatusResponseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        upiTransactionStatusResponse.setBody(upiTransactionStatusResponseBody);
        return upiTransactionStatusResponse;
    }

    @RequestMapping(value = "/upi/transactionStatus")
    public void upiTransactionStatus(HttpServletRequest request, HttpServletResponse response) throws IOException,
            FacadeCheckedException {
        long startTime = System.currentTimeMillis();
        response.setHeader("Content-Type", "application/json");
        response.setContentType("application/json");
        try {
            Map<String, String> data = transactionStatusServiceImpl.getUpiCashierResponse(request);

            if (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
                response.getOutputStream().print(
                        JsonMapper.mapObjectToJson(getUpiTransactionStatusResponse(request,
                                transactionStatusServiceImpl.getUpiPollStatus(data))));
            } else {
                response.getOutputStream().print(getUpiResponseObject(data).toString());
            }
            return;
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e.getStackTrace());
            if (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
                response.getOutputStream().print(
                        JsonMapper.mapObjectToJson(getUpiTransactionStatusResponse(request, UPIPollStatus.POLL_AGAIN)));
            } else {
                response.getOutputStream().print(RETRY.toString());
            }
        } finally {
            LOGGER.info("Total time taken to fetch UpiTransaction is {} ms", System.currentTimeMillis() - startTime);
        }
    }

    @RequestMapping(value = "/processRetry")
    public void processRetryPayment(HttpServletRequest request, Locale locale, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        response.setContentType("text/html");

        try {
            theiaSessionDataService.validateSession(request, true);
            boolean processed = transactionStatusServiceImpl.getRetryResponse(request);
            if (processed) {
                String viewName = theiaViewResolverService.returnPaymentPage(request);
                StringBuilder path = new StringBuilder();
                path.append(RetryConstants.JSP_PATH).append(viewName).append(".jsp");
                request.getRequestDispatcher(path.toString()).forward(request, response);
                return;
            }
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("request is invalid in processRetry :", e);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR :", e);
        } finally {
            LOGGER.info("Total time taken for TransactionStatusController.processRetryPayment is {} ms",
                    System.currentTimeMillis() - startTime);
        }
        LOGGER.error("Something went wrong. Redirecting to Error Page");
        request.getRequestDispatcher(theiaViewResolverService.returnErrorPage(request)).forward(request, response);
        return;
    }

    @RequestMapping(value = "/abandonTransaction", method = { RequestMethod.GET, RequestMethod.POST })
    public String abandonTransaction(HttpServletRequest request, HttpServletResponse response, Model model,
            Locale locale) {
        long startTime = System.currentTimeMillis();
        try {
            PaymentRequestBean paymentRequestData = new PaymentRequestBean(request);
            LOGGER.info("Request received for abandon transaction : {}", paymentRequestData);

            if (!theiaSessionDataService.isSessionExists(request)) {
                LOGGER.warn("Session does not contains a valid transaction id queryString :{}",
                        request.getQueryString());
                throw new SessionExpiredException("Session does not contains a valid transaction id.");
            }
            TransactionInfo txnData = theiaSessionDataService.getTxnInfoFromSession(request);
            MerchantInfo merchantInfo = theiaSessionDataService.getMerchantInfoFromSession(request);

            /**
             * Setting Manual Failure on PRN Validation and Non PRN Filling
             * Failure Cases
             */
            String key = enhancedCashierPageServiceHelper.fetchRedisKey(paymentRequestData.getMid(),
                    paymentRequestData.getOrderId());
            Object object = nativeSessionUtil.getKey(key);
            if (object != null && merchantPreferenceService.isDynamicQR2FAEnabledWithPCF(paymentRequestData.getMid())) {
                EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) object;
                String responsePage = transactionStatusServiceImpl
                        .fetchResponsePageForAbandonTransaction(enhanceCashierPageCachePayload);
                if (StringUtils.isNotBlank(responsePage)) {
                    theiaSessionDataService.setRedirectPageInSession(request, responsePage);
                    return theiaViewResolverService.returnForwarderPage();
                }
            }

            Assert.notNull(txnData, "TransactionInfo obtained from session is null");
            Assert.notNull(merchantInfo, "MerchantInfo obtained from session is null");
            ExtendedInfoRequestBean extendedInfoRequestBean = theiaSessionDataService
                    .geExtendedInfoRequestBean(request);

            String responsePage = transactionStatusServiceImpl.fetchResponsePageForAbandonTransaction(request, txnData,
                    merchantInfo, extendedInfoRequestBean);
            if (StringUtils.isNotBlank(responsePage)) {
                theiaSessionDataService.setRedirectPageInSession(request, responsePage);
                return theiaViewResolverService.returnForwarderPage();
            }
            LOGGER.error("Could not fetch redirect page");
        } catch (Exception e) {
            throw new CoreSessionExpiredException("Exception Occurred while abandon transaction");
        } finally {
            LOGGER.info("Total time taken for Controller TransactionStatusController.abandonTransaction is {} ms",
                    System.currentTimeMillis() - startTime);
        }
        return theiaViewResolverService.returnOOPSPage(request);
    }

    @RequestMapping(value = "/session-timeout", method = { RequestMethod.GET })
    public String scanAndPayTimeout(HttpServletRequest request, HttpServletResponse response) {
        return theiaViewResolverService.returnScanAndPayTimeout(request);
    }

    private static ResponseEntity<?> createResponseEntity(HttpServletRequest request, String path, HttpStatus status) {
        String context = request.getContextPath();
        String effectivePath = new StringBuilder(context).append(path).toString();
        URI location = URI.create(effectivePath);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(responseHeaders, status);
    }

    private static ResponseEntity<?> createRetryResponseEntity(HttpServletRequest request, String path,
            HttpStatus status) {
        String effectivePath = new StringBuilder(path).toString();
        URI location = URI.create(effectivePath);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(responseHeaders, status);
    }

    private JSONObject getUpiResponseObject(Map<String, String> data) {
        if (data == null
                || (null != data.get("POLL_STATUS") && UPIPollStatus.POLL_AGAIN.getMessage().equalsIgnoreCase(
                        data.get("POLL_STATUS")))) {
            return RETRY;
        }
        return STOP_POLLING;
    }

}