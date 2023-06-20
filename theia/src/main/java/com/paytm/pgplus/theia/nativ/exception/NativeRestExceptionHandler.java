package com.paytm.pgplus.theia.nativ.exception;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ErrorResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.EnhancedCashierFlow;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.exceptions.CustomInitTxnException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.supergw.exception.JwtValidationException;
import com.paytm.pgplus.theia.offline.exceptions.*;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;

/**
 * Created by rahulverma on 28/8/17.
 */
@Order(value = 1)
@ControllerAdvice(annotations = NativeControllerAdvice.class)
public class NativeRestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRestExceptionHandler.class);
    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> requestValidationExceptionHandler(HttpServletRequest request,
            RequestValidationException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Request_Validation_Exception.getErrorCode() + "due to  : {}",
                e.getMessage());
        if (StringUtils.equals(e.getResultInfo().getResultCode(),
                com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_SSO_TOKEN.getCode())) {
            statsDUtils.pushExceptionToStatsD(HttpStatus.UNAUTHORIZED.value(),
                    NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
            return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(SubscriptionValidationException.class)
    public ResponseEntity<ErrorResponse> subscriptionValidationExceptionHandler(HttpServletRequest request,
            SubscriptionValidationException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Subscription_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Subscription_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(MerchantVelocityBreachException.class)
    public ResponseEntity<ErrorResponse> requestValidationExceptionHandler(HttpServletRequest request,
            MerchantVelocityBreachException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Merchant_Velocity_Breach_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Merchant_Velocity_Breach_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(PaymentRequestValidationException.class)
    public ResponseEntity<ErrorResponse> paymentRequestValidationExceptionHandler(PaymentRequestValidationException e) {
        LOGGER.error("error code : {}, error msg : {}",
                NativeValidationExceptionType.Native_Request_Validation_Exception.getErrorCode(), e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
        return new ResponseEntity<>(getPaymentRequestValidationErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(DuplicatePaymentRequestException.class)
    public ResponseEntity<ErrorResponse> duplicatePaymentRequestExceptionHandler(HttpServletRequest request,
            DuplicatePaymentRequestException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Duplicate_Payment_Request_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Duplicate_Payment_Request_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(OrderIdGenerationException.class)
    public ResponseEntity<ErrorResponse> paymentRequestProcessingExceptionHandler(HttpServletRequest request,
            OrderIdGenerationException e) {
        LOGGER.error(NativeValidationExceptionType.Native_OrderId_Generation_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_OrderId_Generation_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> baseExceptionHandler(HttpServletRequest request, BaseException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getErrorCode()
                + " : {}", e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(com.paytm.pgplus.theia.accesstoken.exception.BaseException.class)
    public ResponseEntity<ErrorResponse> accesstokenExceptionHandler(HttpServletRequest request,
            com.paytm.pgplus.theia.accesstoken.exception.BaseException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getErrorCode()
                + " : {}", e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getType());
        return new ResponseEntity<>(getAccesstokenErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(PaymentRequestProcessingException.class)
    public ResponseEntity<ErrorResponse> paymentRequestProcessingExceptionHandler(PaymentRequestProcessingException e) {
        LOGGER.error(
                NativeValidationExceptionType.Native_Payment_Request_Processing_Exception.getErrorCode() + " : {}",
                e.getMessage());
        if (StringUtils.equals(e.getResultInfo().getResultCode(),
                com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_SSO_TOKEN.getCode())) {
            statsDUtils.pushExceptionToStatsD(HttpStatus.UNAUTHORIZED.value(),
                    NativeValidationExceptionType.Native_Payment_Request_Processing_Exception.getType());
            return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Payment_Request_Processing_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(BinDetailException.class)
    public ResponseEntity<ErrorResponse> binDetailValidationExceptionHandler(BinDetailException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Bin_Detail_Fetching_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Bin_Detail_Fetching_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(PassCodeValidationException.class)
    public ResponseEntity<ErrorResponse> passCodeValidationExceptionHandler(PassCodeValidationException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Passcode_Validation_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Passcode_Validation_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(FetchMerchantInfoException.class)
    public ResponseEntity<ErrorResponse> fetchMerchantInfoExceptionHandler(FetchMerchantInfoException e) {
        LOGGER.error(NativeValidationExceptionType.Native_MerchantInfo_Fetching_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_MerchantInfo_Fetching_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(VPADetailException.class)
    public ResponseEntity<ErrorResponse> vpaDetailValidationExceptionHandler(VPADetailException e) {
        LOGGER.error(NativeValidationExceptionType.Native_ProfileVPA_Fetching_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_ProfileVPA_Fetching_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(SubscriptionServiceException.class)
    public ResponseEntity<ErrorResponse> subscriptionServiceExceptionHandler(SubscriptionServiceException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Subscription_Exception.getErrorCode() + " : {}",
                e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Subscription_Exception.getType());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(FacadeUncheckedException.class)
    public ResponseEntity<ErrorResponse> facadeUncheckedExceptionHandler(HttpServletRequest servletRequest,
            FacadeUncheckedException fue) {

        LOGGER.error("FacadeUncheckedException in Native API: {}  {}", servletRequest.getRequestURI(), fue);
        ResultInfo resultInfo = new ResultInfo();
        ResultCode resultCode = ResultCode.SYSTEM_ERROR;
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getResultCodeId());
        resultInfo.setResultMsg("Looks like a network issue. Please try again.");
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        if (PaymentOfferUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(PaymentOfferUtils.getRequestHeader().getRequestId());
        }
        logNativeException(resultInfo, new FacadeUncheckedException());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), fue.getClass().getName());
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler(CustomInitTxnException.class)
    public ResponseEntity getErrorResponse(CustomInitTxnException e) {
        LOGGER.error("Exception in Custom Initiate Transaction : ", e);
        statsDUtils.pushExceptionToStatsD(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getClass().getName());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Throwable e) {
        LOGGER.error(NativeValidationExceptionType.Native_Unhandled_Exception.getErrorCode() + " :  {}.", e);
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Unhandled_Exception.getType());
        ResultInfo resultInfo = new ResultInfo();
        ResultCode resultCode = ResultCode.SYSTEM_ERROR;
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getResultCodeId());
        resultInfo.setResultMsg(resultCode.getResultMsg());
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(resultInfo));
        String version = MDC.get(VERSION);
        if (StringUtils.isNotBlank(version)) {
            errorResponse.setHead(new ResponseHeader(version));
        } else {
            errorResponse.setHead(new ResponseHeader());
        }
        if (PaymentOfferUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(PaymentOfferUtils.getRequestHeader().getRequestId());
        }
        logNativeException(resultInfo, e);
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(JwtValidationException e, HttpServletRequest request) {
        LOGGER.error(NativeValidationExceptionType.Native_Unhandled_Exception.getErrorCode() + " :  {}.",
                ExceptionUtils.getStackTrace(e));
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Unhandled_Exception.getType());
        ResultInfo resultInfo = new ResultInfo();
        ResultCode resultCode = ResultCode.JWT_VALIDATION_EXCEPTION;
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getResultCodeId());
        resultInfo.setResultMsg(resultCode.getResultMsg());
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(resultInfo));
        String version = MDC.get(VERSION);
        if (StringUtils.isNotBlank(version)) {
            errorResponse.setHead(new ResponseHeader(version));
        } else {
            errorResponse.setHead(new ResponseHeader());
        }
        logNativeException(resultInfo, e);
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    @ExceptionHandler(com.paytm.pgplus.theiacommon.exception.RequestValidationException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(
            com.paytm.pgplus.theiacommon.exception.RequestValidationException e, HttpServletRequest request) {
        LOGGER.error(NativeValidationExceptionType.Native_Unhandled_Exception.getErrorCode() + " :  {}.",
                ExceptionUtils.getStackTrace(e));
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Unhandled_Exception.getType());
        ResultInfo resultInfo = getResultInfo(e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(resultInfo));
        String version = MDC.get(VERSION);
        if (StringUtils.isNotBlank(version)) {
            errorResponse.setHead(new ResponseHeader(version));
        } else {
            errorResponse.setHead(new ResponseHeader());
        }
        logNativeException(resultInfo, e);
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    private ResultInfo getResultInfo(com.paytm.pgplus.theiacommon.exception.RequestValidationException e) {
        ResultInfo resultInfo = new ResultInfo();
        if (e.getResultInfo() != null) {
            resultInfo.setResultStatus(e.getResultInfo().getResultStatus());
            resultInfo.setResultCode(e.getResultInfo().getResultCode());
            resultInfo.setResultMsg(e.getResultInfo().getResultMsg());
        } else {
            ResultCode resultCode = ResultCode.SYSTEM_ERROR;
            resultInfo.setResultStatus(resultCode.getResultStatus());
            resultInfo.setResultCode(resultCode.getResultCodeId());
            resultInfo.setResultMsg(resultCode.getResultMsg());
        }
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        return resultInfo;
    }

    private BaseResponseBody getBaseResponseBody(ResultInfo resultInfo) {
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }

    private ErrorResponse getAccesstokenErrorResponse(com.paytm.pgplus.theia.accesstoken.exception.BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = null;
        if (e.getResultInfo() != null) {
            resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(), e.getResultInfo().getResultCodeId(), e
                    .getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        } else if (e.getResultCode() != null) {

            com.paytm.pgplus.theia.accesstoken.enums.ResultCode resultCode = e.getResultCode();
            resultInfo = new ResultInfo(resultCode.getStatus(), resultCode.getId(), resultCode.getCode());
        }
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        errorResponse.setBody(getBaseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());

        if (AccessTokenUtils.getRequestHeader() != null) {
            TokenRequestHeader requestHeader = AccessTokenUtils.getRequestHeader();
            errorResponse.getHead().setRequestId(requestHeader.getRequestId());
            errorResponse.getHead().setVersion(requestHeader.getVersion());
            errorResponse.getHead().setResponseTimestamp(requestHeader.getRequestTimestamp());
        }
        return errorResponse;
    }

    private ErrorResponse getErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = null;
        if (e.getResultInfo() != null) {
            resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(), e.getResultInfo().getResultCodeId(), e
                    .getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        } else if (e.getResultCode() != null) {
            resultInfo = new ResultInfo(e.getResultCode().getResultStatus(), e.getResultCode().getResultCodeId(), e
                    .getResultCode().getCode());
        }
        errorResponse.setBody(baseResponseBody(resultInfo));
        String version = MDC.get(VERSION);
        if (StringUtils.isNotBlank(version)) {
            errorResponse.setHead(new ResponseHeader(version));
        } else {
            errorResponse.setHead(new ResponseHeader());
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            if (TheiaConstant.ExtraConstants.APPLY_PROMO_URL_V2.equals(request.getRequestURI())) {
                errorResponse.getHead().setVersion(TheiaConstant.RequestHeaders.Version_V2);
            }
        } catch (Exception ex) {
            LOGGER.warn("exception while getting servlet request for URI of apply promo v2");
        }
        if (PaymentOfferUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(PaymentOfferUtils.getRequestHeader().getRequestId());
        }
        if (EmiSubventionUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(EmiSubventionUtils.getRequestHeader().getRequestId());
            errorResponse.getHead().setVersion(EmiSubventionUtils.getRequestHeader().getVersion());
            errorResponse.getHead().setResponseTimestamp(EmiSubventionUtils.getRequestHeader().getRequestTimestamp());
        }
        logNativeException(resultInfo, e);
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        return errorResponse;
    }

    private String getRequestURI() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI();
    }

    private ErrorResponse getPaymentRequestValidationErrorResponse(PaymentRequestValidationException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = null;
        if (e.getResultInfo() != null) {
            resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(), e.getResultInfo().getResultCodeId(), e
                    .getResultInfo().getResultMsg());
        } else {
            resultInfo = new ResultInfo("F", ResponseConstants.SYSTEM_ERROR.getCode(),
                    ResponseConstants.SYSTEM_ERROR.getMessage());
        }
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        if (PaymentOfferUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(PaymentOfferUtils.getRequestHeader().getRequestId());
        }
        if (EmiSubventionUtils.getRequestHeader() != null) {
            errorResponse.getHead().setRequestId(EmiSubventionUtils.getRequestHeader().getRequestId());
            errorResponse.getHead().setVersion(EmiSubventionUtils.getRequestHeader().getVersion());
            errorResponse.getHead().setResponseTimestamp(EmiSubventionUtils.getRequestHeader().getRequestTimestamp());
        }
        logNativeException(resultInfo, e);
        localeFieldAspect.addLocaleFieldsInObject(resultInfo, getRequestURI());
        return errorResponse;
    }

    private BaseResponseBody baseResponseBody(ResultInfo resultInfo) {
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }

    private ResponseEntity<ErrorResponse> getErrorResponse(HttpServletRequest request, BaseException e) {

        String workFlow = (String) request.getAttribute(EnhancedCashierFlow.WORKFLOW);
        if (StringUtils.equals(EnhancedCashierFlow.ENHANCED_CASHIER_FLOW, workFlow)) {
            return new ResponseEntity<>(this.getMerchantRedirectErrorResponse(e), HttpStatus.OK);
        }

        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    private ErrorResponse getMerchantRedirectErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(),
                e.getResultInfo().getResultCodeId(), e.getResultInfo().getResultMsg(), Boolean.TRUE);
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());
        if (e.getResultInfo() != null) {
            EventUtils.logResponseCode(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest().getRequestURI(), EventNameEnum.RESPONSE_CODE_SENT, e.getResultInfo().getResultCode(),
                    e.getResultInfo().getResultMsg());
        }
        localeFieldAspect.addLocaleFieldsInObject(errorResponse, getRequestURI());
        return errorResponse;
    }

    private NativeValidationExceptionType getExceptionType(Throwable exception) {
        if (exception instanceof BaseException) {
            return ((BaseException) exception).getNativeValidationType();
        }
        return null;
    }

    private void logNativeException(ResultInfo resultInfo, Throwable throwable) {
        try {
            String mid = MDC.get("MID") != null ? MDC.get("MID") : "";
            String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : "";

            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String uri = servletRequest.getRequestURI();

            NativeValidationExceptionType nativeValidationExceptionType = getExceptionType(throwable);

            String resultCode = null;
            String resultMessage = null;
            if (resultInfo != null) {
                resultCode = resultInfo.getResultCode();
                resultMessage = resultInfo.getResultMsg();
            }

            String nativeErrorCode = "";
            String nativeErrorMessage = "";
            if (throwable instanceof BaseException && nativeValidationExceptionType != null) {
                nativeErrorCode = nativeValidationExceptionType.getErrorCode();
                nativeErrorMessage = throwable.getMessage();
            }

            String errorMessage = throwable.getMessage();

            LinkedHashMap<String, String> metaData = new LinkedHashMap<>();
            metaData.put("api", uri);
            metaData.put("resultCode", resultCode);
            metaData.put("resultMsg", resultMessage);
            EventUtils.pushTheiaEvents(mid, orderId, EventNameEnum.NATIVE_ERROR, metaData);
            EventUtils.logResponseCode(uri, EventNameEnum.RESPONSE_CODE_SENT, resultCode, resultMessage);
        } catch (Exception e) {
            LOGGER.info("Problem occurred while logging inside into EventLogger : {}", e.getMessage());
        }
    }
}