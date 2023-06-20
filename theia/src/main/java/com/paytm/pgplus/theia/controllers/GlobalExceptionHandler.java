/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.checksum.utils.AESMerchantService;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.statistics.StatisticConstants;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ErrorResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native;
import com.paytm.pgplus.theia.exceptions.*;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedNativeErrorResponse;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.DuplicatePaymentRequestException;
import com.paytm.pgplus.theia.offline.exceptions.MerchantRedirectRequestException;
import com.paytm.pgplus.theia.offline.exceptions.PassCodeValidationException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPCODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.SubscriptionResponseConstant.RESPMSG;

/**
 * Global exception handler which will handle all the exceptions thrown from
 * different controllers
 *
 * @createdOn 17-Mar-2016
 * @author kesari
 */
@Order(2)
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private InterceptorUtils interceptorUtils;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private NativeRetryUtil nativeRetryUtils;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private AESMerchantService aesMerchantService;

    @Autowired
    private EncryptedParamsRequestServiceHelper encryptedParamsRequestServiceHelper;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    /**
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public String handleExceptions(HttpServletRequest request, Exception e, HttpServletResponse response) {
        LOGGER.error("Exception Occurred for URL :{}", request.getRequestURL(), e);
        String mid = getMid(request);
        String orderId = getOrderId(request);
        if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(orderId)) {
            populateMerchantResponse(e, response, mid, orderId);
            return null;
        }
        statsDUtils.pushExceptionToStatsD(response.getStatus(), e.getClass().getSimpleName());
        return theiaViewResolverService.returnOOPSPage(request);
    }

    @ExceptionHandler(StagingRequestException.class)
    public String showStagingOOPSPage(StagingRequestException e, HttpServletRequest request) {
        LOGGER.error(e.getMessage());
        statsDUtils.pushException(e.getClass().getSimpleName());
        return theiaViewResolverService.returnOOPSPage(request);
    }

    @ExceptionHandler(MerchantRedirectRequestException.class)
    public ResponseEntity<ErrorResponse> baseExceptionHandler(MerchantRedirectRequestException e) {
        LOGGER.error("<Native_Exception_Occurred_While_Process_Transaction_API> : {}", e.getMessage());
        if (e.getResultInfo() != null) {
            EventUtils.logResponseCode(TheiaConstant.ExtraConstants.V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, e
                    .getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg());
        }
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getMerchantRedirectErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(AccountNumberMismatchException.class)
    public void generateResponseForMerchant(AccountNumberMismatchException e, HttpServletResponse response) {
        LOGGER.error(e.getMessage());
        if (e.getpaymentRequestBean().isEnhancedCashierPaymentRequest()) {
            try {
                HttpServletRequest request = OfflinePaymentUtils.gethttpServletRequest();
                InitiateTransactionRequestBody orderDetail = (InitiateTransactionRequestBody) request
                        .getAttribute("orderDetail");
                EnhancedNativeErrorResponse errorResponse = merchantResponseService.getEnhancedNativeErrorResp(
                        orderDetail, request, null, e.getResultInfo(), true, false, null);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(getErrorResponseJSONString(errorResponse));
                return;
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }
        statsDUtils.pushExceptionToStatsD(response.getStatus(), e.getClass().getSimpleName());
        String responsePage = merchantResponseService.processMerchantFailResponse(e.getpaymentRequestBean(),
                ResponseConstants.ACCOUNT_NUMBER_MISMATCH);
        try {
            response.getOutputStream().print(responsePage);
        } catch (IOException io) {
            LOGGER.error("Exception occured : {}", io.getMessage());
        }
        response.setContentType("text/html");
        return;
    }

    @ExceptionHandler(AccountNumberNotExistException.class)
    public void generateResponseForMerchant(AccountNumberNotExistException e, HttpServletResponse response) {
        LOGGER.error(e.getMessage());
        statsDUtils.pushExceptionToStatsD(response.getStatus(), e.getClass().getSimpleName());
        if (e.getpaymentRequestBean().isEnhancedCashierPaymentRequest()) {
            try {
                HttpServletRequest request = OfflinePaymentUtils.gethttpServletRequest();
                InitiateTransactionRequestBody orderDetail = (InitiateTransactionRequestBody) request
                        .getAttribute("orderDetail");
                EnhancedNativeErrorResponse errorResponse = merchantResponseService.getEnhancedNativeErrorResp(
                        orderDetail, request, null, e.getResultInfo(), true, false, null);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(getErrorResponseJSONString(errorResponse));
                return;
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage());
            }
        }
        String responsePage = merchantResponseService.processMerchantFailResponse(e.getpaymentRequestBean(),
                ResponseConstants.ACCOUNT_NUMBER_NOT_EXIST);
        try {
            response.getOutputStream().print(responsePage);
        } catch (IOException io) {
            LOGGER.error("Exception occured : {}", io.getMessage());
        }
        response.setContentType("text/html");
        return;
    }

    @ExceptionHandler(InvalidRequestParameterException.class)
    public void generateResponseForMerchant(InvalidRequestParameterException e, HttpServletResponse response) {
        LOGGER.error(e.getMessage());
        statsDUtils.pushExceptionToStatsD(response.getStatus(), e.getClass().getSimpleName());
        String responsePage = merchantResponseService.processMerchantFailResponse(e.getpaymentRequestBean(),
                ResponseConstants.INVALID_REQUEST);
        try {
            response.getOutputStream().print(responsePage);
        } catch (IOException io) {
            LOGGER.error("Exception occured : {}", io.getMessage());
        }
        response.setContentType("text/html");
        return;
    }

    @ExceptionHandler(PassCodeValidationException.class)
    public ResponseEntity<ErrorResponse> passCodeValidationExceptionHandler(PassCodeValidationException e) {
        LOGGER.error("<Native_Passcode_Validation_Exception> : {}", e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        if (e.getResultInfo() != null) {
            EventUtils.logResponseCode(TheiaConstant.ExtraConstants.V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, e
                    .getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg());
        }
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(DuplicatePaymentRequestException.class)
    public ResponseEntity<ErrorResponse> duplicatePaymentRequestException(DuplicatePaymentRequestException e) {
        LOGGER.error("<Duplicate Payment Request Exception> : {}", e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        if (e.getResultInfo() != null) {
            EventUtils.logResponseCode(TheiaConstant.ExtraConstants.V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, e
                    .getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg());
        }
        return new ResponseEntity<>(this.getDuplicatePaymentExceptionResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(CoreSessionExpiredException.class)
    public void generateResponseForMerchant(HttpServletRequest request, CoreSessionExpiredException e,
            HttpServletResponse response) {
        LOGGER.error(e.getMessage());
        statsDUtils.pushExceptionToStatsD(response.getStatus(), e.getClass().getSimpleName());
        populateMerchantResponse(e, response, getMid(request), getOrderId(request));
        return;
    }

    @ExceptionHandler(NativeFlowException.class)
    public void nativeFlowValidationExceptionHandle(HttpServletRequest request, NativeFlowException nfe,
            HttpServletResponse response) {

        InitiateTransactionRequestBody orderDetail = (InitiateTransactionRequestBody) request
                .getAttribute("orderDetail");

        statsDUtils.pushExceptionToStatsD(response.getStatus(), nfe.getClass().getSimpleName());
        if (orderDetail == null) {
            /*
             * checks if it is present in NativeFlowException, orderDetail is
             * set for AppInvoke flow
             */
            if (nfe.getOrderDetail() != null) {
                orderDetail = nfe.getOrderDetail();
            }
        }

        boolean sendHTMLResponse = nfe.isHTMLResponse();
        boolean isRiskReject = (null != nfe.getResultInfo())
                && (StringUtils.equals(ResponseConstants.RISK_REJECT.getAlipayResultMsg(), nfe.getResultInfo()
                        .getResultCode()));
        String customCallbackMessage = nfe.getCustomCallbackMsg();

        String mid = getMid(request);

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);
        String encParams = request.getParameter(TheiaConstant.RequestParams.ENC_PARAMS);

        if (isAES256Encrypted && encParams != null) {
            String merchantKey = aesMerchantService.fetchAesEncDecKey(mid);
            request = encryptedParamsRequestServiceHelper.decryptParamsTORequest(request, encParams, merchantKey,
                    isAES256Encrypted);
        }

        NativeRetryInfo retryInfo = nfe.getRetryInfo();
        com.paytm.pgplus.common.model.ResultInfo resultInfo = null;

        if ((BooleanUtils.isTrue((Boolean) request.getAttribute(TheiaConstant.DccConstants.PAYMENT_CALL_DCC)))) {
            try {
                Field resultInfoOfSuperClass = nfe.getClass().getSuperclass().getDeclaredField("resultInfo");
                resultInfoOfSuperClass.setAccessible(true);
                resultInfo = (com.paytm.pgplus.common.model.ResultInfo) resultInfoOfSuperClass.get(nfe);
                EnhancedNativeErrorResponse errorResponse = merchantResponseService.getErrorResponseForDcc(orderDetail,
                        request, retryInfo, resultInfo, isAES256Encrypted);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                LOGGER.info("Final respose for dcc payment sent   is {}", getErrorResponseJSONString(errorResponse));
                response.getWriter().write(getErrorResponseJSONString(errorResponse));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            return;
        }

        if ((StringUtils.isNotEmpty(request.getParameter(TheiaConstant.LinkBasedParams.LINK_ID))
                || StringUtils.isNotEmpty((String) request.getAttribute(TheiaConstant.LinkBasedParams.LINK_ID))
                || StringUtils.isNotEmpty(request.getParameter(TheiaConstant.LinkBasedParams.INVOICE_ID)) || StringUtils
                    .isNotEmpty((String) request.getAttribute(TheiaConstant.LinkBasedParams.INVOICE_ID)))
                && sendHTMLResponse) {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setLinkBasedPayment("true");
            if (StringUtils
                    .isNotEmpty((String.valueOf(request
                            .getAttribute(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.INVOICE_ID))))) {
                transactionResponse.setLinkType(TheiaConstant.LinkRiskParams.LINK_INVOICE_TYPE);
            }
            try {

                transactionResponse.setCallbackUrl(ConfigurationUtil.getProperty(THEIA_BUISNESS_BASE_PATH)
                        + TheiaConstant.ExtraConstants.LINK_PAYMENT_STATUS_URL);

                transactionResponse.setTransactionStatus(TheiaConstant.LinkBasedParams.FAIL);
                transactionResponse.setTxnAmount(request.getParameter(TheiaConstant.LinkBasedParams.TXN_AMOUNT));
                ResponseConstants responseConstants = ResponseConstants.fetchResponseConstantByName(nfe.getResultInfo()
                        .getResultCode());

                if (responseConstants != null) {
                    String errorMsg = StringUtils.isNotBlank(responseConstants.getAlipayResultMsg()) ? responseCodeUtil
                            .getResponseMsg(responseCodeUtil.getResponseCodeDetails(
                                    responseConstants.getAlipayResultMsg(), null, PaymentStatus.FAIL.name())) : "";
                    transactionResponse.setResponseMsg(errorMsg);
                }
                String htmlpage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
                response.getOutputStream().print(htmlpage);
                response.setContentType("text/html");
            } catch (Exception e) {
                LOGGER.error("Error while sending response in exception for link based payment ", e);
            }
            return;
        }

        // Handling for invalidate Session- in case of native and native enhance
        String txnToken = request.getParameter(Native.TXN_TOKEN);
        EnvInfoRequestBean envInfo = EnvInfoUtil.fetchEnvInfo(request);
        if (request.getAttribute("NATIVE_ENHANCED_FLOW") != null
                && BooleanUtils.toBoolean(request.getAttribute("NATIVE_ENHANCED_FLOW").toString()) && !sendHTMLResponse) {
            // in case of HTMLResponse: false and sendRedirect: true- invalid
            // session
            if (StringUtils.isBlank(txnToken)) {
                txnToken = (String) request.getAttribute(Native.TXN_TOKEN);
            }

            if (nfe.isRedirectEnhanceFlow()) {
                enhancedCashierPageServiceHelper.invalidateEnhancedNativeData(txnToken, mid, orderDetail.getOrderId());
            }

            nativeRetryUtils.invalidateSession(txnToken, !nfe.isRedirectEnhanceFlow(), orderDetail, envInfo,
                    isRiskReject);

        } else if (!ObjectUtils.isEmpty(orderDetail)) {
            // invalidate session if retryInfo is false
            nativeRetryUtils.invalidateSession(txnToken, retryInfo, orderDetail, envInfo, isRiskReject);
        }

        /*
         * nativePlus: this is case when merchant has sent request in Json, and
         * expects response in Json
         */
        boolean isNativeJsonRequest = processTransactionUtil.isNativeJsonRequest(request) || nfe.isNativeJsonRequest();
        if (isNativeJsonRequest) {
            sendHTMLResponse = false;
        }

        try {
            Field resultInfoOfSuperClass = nfe.getClass().getSuperclass().getDeclaredField("resultInfo");
            resultInfoOfSuperClass.setAccessible(true);

            if (nfe != null && nfe.getResultInfo() != null
                    && StringUtils.isNotBlank(nfe.getResultInfo().getResultCode())
                    && ResponseConstants.TRANS_CLOSED.name().equalsIgnoreCase(nfe.getResultInfo().getResultCode())) {

                TransactionResponse transactionResponse = merchantResponseService
                        .handleCancelledTransactionForNativeAndEnhanced(orderDetail.getMid(), orderDetail.getOrderId());

                if (transactionResponse != null) {
                    nfe.getResultInfo().setResultMsg(transactionResponse.getResponseMsg());
                    nfe.getResultInfo().setResultCodeId(transactionResponse.getResponseCode());
                }
            }

            resultInfo = (com.paytm.pgplus.common.model.ResultInfo) resultInfoOfSuperClass.get(nfe);

            if (sendHTMLResponse) {
                LOGGER.info("Native_Flow_Exception <resultCodeId:{}, resultCode:{}>, sending HTML Response",
                        resultInfo.getResultCodeId(), resultInfo.getResultCode());
                String html = merchantResponseService.processResponseForNativeRequestValidationError(orderDetail,
                        request, retryInfo, resultInfo, customCallbackMessage);
                response.getOutputStream().print(html);

                response.setContentType("text/html");
                return;
            } else {
                // this sends JSON response
                LOGGER.info("Native_Flow_Exception <resultCodeId:{}, resultCode:{}>, sending JSON Response",
                        resultInfo.getResultCodeId(), resultInfo.getResultCode());

                /*
                 * this is for nativePlus
                 */
                if (isNativeJsonRequest) {
                    NativeJsonResponse nativeJsonResponse = merchantResponseService.getErrorNativeJsonResponse(
                            orderDetail, request, retryInfo, resultInfo, customCallbackMessage);
                    if (ResultCode.INVALID_SSO_TOKEN.name().equals(resultInfo.getResultCode())) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    response.setContentType("application/json;charset=UTF-8");
                    if (processTransactionUtil.isRequestOfType(PTR_URL)) {
                        localeFieldAspect.addLocaleFieldsInObject(nativeJsonResponse.getBody(), PTR_URL);
                    } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
                        localeFieldAspect.addLocaleFieldsInObject(nativeJsonResponse.getBody(), V1_PTC);
                    } else if (processTransactionUtil.isRequestOfType(V1_DIRECT_BANK_REQUEST)) {
                        localeFieldAspect.addLocaleFieldsInObject(nativeJsonResponse.getBody(), V1_DIRECT_BANK_REQUEST);
                    }
                    String errorResponseJSONString = getErrorResponseJSONString(nativeJsonResponse);
                    LOGGER.info("Exception - Final nativeJsonResponse sent to merchant : {}", errorResponseJSONString);
                    if (nativeJsonResponse != null && nativeJsonResponse.getBody() != null) {
                        if (nativeJsonResponse.getBody().getResultInfo() != null) {
                            EventUtils.logResponseCode(V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, nativeJsonResponse
                                    .getBody().getResultInfo().getResultCode(), nativeJsonResponse.getBody()
                                    .getResultInfo().getResultMsg());

                            if (!nativeJsonResponse.getBody().getResultInfo().getRetry()
                                    && !BooleanUtils.toBoolean(nativeJsonResponse.getBody().getResendRetry())
                                    && orderDetail != null) {
                                nativePaymentUtil.invalidateNativeJsonRequestSessionData(
                                        request.getParameter(Native.TXN_TOKEN), orderDetail.getMid(),
                                        orderDetail.getOrderId());
                            }

                        }
                        if (nativeJsonResponse.getBody().getTxnInfo() != null) {
                            EventUtils.logResponseCode(V1_PTC, EventNameEnum.TXNINFO_CODE_SENT, nativeJsonResponse
                                    .getBody().getTxnInfo().get(RESPCODE), nativeJsonResponse.getBody().getTxnInfo()
                                    .get(RESPMSG));
                        }
                    }
                    response.getWriter().write(errorResponseJSONString);
                    return;
                }

                boolean isRedirect = nfe.isRedirectEnhanceFlow();

                EnhancedNativeErrorResponse errorResponse = merchantResponseService.getEnhancedNativeErrorResp(
                        orderDetail, request, retryInfo, resultInfo, isRedirect, isAES256Encrypted,
                        customCallbackMessage);
                if (errorResponse != null && errorResponse.getBody() != null) {
                    EventUtils.logResponseCode(V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, errorResponse.getBody()
                            .getResultInfo().getResultCode(), errorResponse.getBody().getResultInfo().getResultMsg());
                    EventUtils.logResponseCode(V1_PTC, EventNameEnum.TXNINFO_CODE_SENT, errorResponse.getBody()
                            .getContent().get(RESPCODE), errorResponse.getBody().getContent().get(RESPMSG));
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                LOGGER.info("Final EnhancedNativeErrorResponse sent to merchant  is {}",
                        getErrorResponseJSONString(errorResponse));
                response.getWriter().write(getErrorResponseJSONString(errorResponse));
                return;
            }
        } catch (Exception e2) {
            LOGGER.error(e2.getMessage());
        }

        return;
    }

    // TODO : add retry logic here
    @ExceptionHandler(MandateException.class)
    public void mandateExceptionHandler(HttpServletRequest request, MandateException mandateException,
            HttpServletResponse response) {
        try {
            String txnToken = request.getParameter(Native.TXN_TOKEN);
            InitiateTransactionRequestBody orderDetail = (InitiateTransactionRequestBody) request
                    .getAttribute("orderDetail");
            /*
             * invalidating the session only if we have transaction token in the
             * PaymentRequest Bean
             */
            if (StringUtils.isNotBlank(txnToken)) {
                nativeRetryUtils.invalidateSession(txnToken, false, orderDetail, null, false);
            }
            Field resultInfoOfSuperClass = mandateException.getClass().getSuperclass().getDeclaredField("resultInfo");
            resultInfoOfSuperClass.setAccessible(true);

            if (processTransactionUtil.isRequestOfType(PTR_URL)) {
                localeFieldAspect.addLocaleFieldsInObject(mandateException.getResultInfo(), PTR_URL);
            } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
                localeFieldAspect.addLocaleFieldsInObject(mandateException.getResultInfo(), V1_PTC);
            }

            PaymentRequestBean requestBean = mandateException.getRequestBean();

            boolean isEnhancedRedirectionFlow = false;
            if (requestBean != null) {
                isEnhancedRedirectionFlow = requestBean.isEnhancedCashierPaymentRequest();
            }

            if (isEnhancedRedirectionFlow) {

                LOGGER.info("Sending response for MANDATE REDIRECTION FLOW");

                EnhancedNativeErrorResponse errorResponse = merchantResponseService
                        .getEnhancedNativeErrorRespForMandateMerchant(mandateException.getCallBackUrl(),
                                mandateException.getMandateResponse(), mandateException.getResultInfo(),
                                mandateException.getRequestBean(), true);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(getErrorResponseJSONString(errorResponse));
                return;

            } else if (requestBean.isNativeJsonRequest()) {
                LOGGER.info("Sending s2s for MANDATE FLOW");
                NativeJsonResponse errorResponse = merchantResponseService.getErrorNativeJsonResponseForBM(
                        mandateException.getCallBackUrl(), mandateException.getMandateResponse(),
                        mandateException.getResultInfo(), mandateException.getRequestBean());
                String errorJson = getErrorResponseJSONString(errorResponse);
                LOGGER.info("Response to send is {}", errorJson);
                response.getOutputStream().print(errorJson);
                response.setContentType("application/json");
                return;

            } else {
                LOGGER.info("Sending redirection for MANDATE NATIVE FLOW");
                String merchantResponseHtml = merchantResponseService.getResponseForMandateMerchant(
                        mandateException.getCallBackUrl(), mandateException.getMandateResponse(),
                        mandateException.getResultInfo(), mandateException.getRequestBean());
                if (StringUtils.isNotBlank(merchantResponseHtml)) {
                    response.getOutputStream().print(merchantResponseHtml);
                    response.setContentType("text/html");
                } else {
                    if (null != mandateException.getMandateResponse()) {
                        request.setAttribute("processedResponse", mandateException.getMandateResponse());
                        request.getRequestDispatcher(
                                TheiaConstant.RetryConstants.JSP_PATH + theiaViewResolverService.returnNpciResPage()
                                        + ".jsp").forward(request, response);
                    } else {
                        request.getRequestDispatcher(
                                TheiaConstant.RetryConstants.JSP_PATH
                                        + theiaViewResolverService.returnOOPSPage(request) + ".jsp").forward(request,
                                response);
                    }
                }
                return;
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
        }
        statsDUtils.pushExceptionToStatsD(response.getStatus(), mandateException.getClass().getSimpleName());
        return;
    }

    private String getErrorResponseJSONString(Object obj) {
        String errorResponseJson = "{}";
        try {
            errorResponseJson = JsonMapper.mapObjectToJson(obj);
        } catch (FacadeCheckedException fce) {
            LOGGER.info("failed mapping object to Json");
        }
        return errorResponseJson;
    }

    private ErrorResponse getMerchantRedirectErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(),
                e.getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        resultInfo.setRedirect(Boolean.TRUE);
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);

        errorResponse.setBody(responseBody);
        errorResponse.setHead(new ResponseHeader());
        if (processTransactionUtil.isRequestOfType(PTR_URL)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, PTR_URL);
        } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, V1_PTC);
        }
        return errorResponse;
    }

    private ErrorResponse getDuplicatePaymentExceptionResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(),
                e.getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        resultInfo.setResultMsg(TheiaConstant.ExtraConstants.DUPLICATE_PAYMENT);
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        if (processTransactionUtil.isRequestOfType(PTR_URL)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, PTR_URL);
        } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, V1_PTC);
        }
        return errorResponse;
    }

    private ErrorResponse getErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(),
                e.getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        resultInfo.setResultCode(ResultCode.INVALID_PASS_CODE.getCode());
        resultInfo.setResultStatus(ResultCode.INVALID_PASS_CODE.getResultStatus());
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        if (processTransactionUtil.isRequestOfType(PTR_URL)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, PTR_URL);
        } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
            localeFieldAspect.addLocaleFieldsInObject(resultInfo, V1_PTC);
        }
        return errorResponse;
    }

    private BaseResponseBody baseResponseBody(ResultInfo resultInfo) {
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }

    @SuppressWarnings("unused")
    private void logExceptions(HttpServletRequest request) {

        long startTime = 0;
        String timeElapsed = "";
        String mid = "";
        String api = "";

        if (null != request.getAttribute(TheiaConstant.ExtraConstants.START_TIME)) {
            startTime = Long.parseLong(request.getAttribute(TheiaConstant.ExtraConstants.START_TIME).toString());
            timeElapsed = String.valueOf(System.currentTimeMillis() - startTime);
        }

        try {
            mid = interceptorUtils.fetchMidRequestType(request)[0];
            api = StringUtils.substringAfterLast(request.getRequestURI(), TheiaConstant.ExtraConstants.FORWARD_SLASH);
            api = StringUtils.substringBefore(api, TheiaConstant.ExtraConstants.QUESTION_MARK);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while fetching Mid from request::", e);
        }

        StatisticsLogger.logMerchantResponse(mid, api, StatisticConstants.PGPLUS, StatisticConstants.RESPONSE,
                timeElapsed, StatisticConstants.EXCEPTION);
    }

    private void populateMerchantResponse(Exception e, HttpServletResponse response, String mid, String orderId) {
        String responsePage = merchantResponseService.generateResponseForMerchant(mid, orderId);

        try {
            HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();

            if (httpServletRequest != null
                    && httpServletRequest.getAttribute(TheiaConstant.RequestParams.IS_CALLBACK_URL_PRESENT) != null
                    && httpServletRequest.getAttribute(TheiaConstant.RequestParams.IS_CALLBACK_URL_PRESENT).equals(
                            false)) {
                try {
                    // handle burl cases in url .when callback url is not
                    // present.
                    httpServletRequest.getRequestDispatcher(
                            TheiaConstant.RetryConstants.JSP_PATH
                                    + theiaViewResolverService.returnOOPSPage(httpServletRequest) + ".jsp").forward(
                            httpServletRequest, response);
                } catch (Exception exception) {
                    response.getOutputStream().print(responsePage);
                }
            } else {
                response.getOutputStream().print(responsePage);
            }
        } catch (IOException io) {
            LOGGER.error("Exception occured while generating exception response: {}", e.getMessage());
        }
        response.setContentType("text/html");
    }

    private String getOrderId(HttpServletRequest request) {
        String orderId = request.getParameter(TheiaConstant.RequestParams.ORDER_ID);
        if (StringUtils.isEmpty(orderId)) {
            return MDC.get(TheiaConstant.RequestParams.ORDER_ID);
        }
        return orderId;
    }

    private String getMid(HttpServletRequest request) {
        String mid = request.getParameter(TheiaConstant.RequestParams.MID);
        if (StringUtils.isEmpty(mid)) {
            return MDC.get(TheiaConstant.RequestParams.MID);
        }
        return mid;
    }

}
