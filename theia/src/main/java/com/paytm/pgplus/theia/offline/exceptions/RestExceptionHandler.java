package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.paytm.pgplus.common.enums.ResultCode;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.annotations.OfflineControllerAdvice;
import com.paytm.pgplus.theia.offline.model.response.ErrorResponse;
import com.paytm.pgplus.theia.offline.model.response.ResponseBody;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 28/8/17.
 */
@Order(value = 1)
@ControllerAdvice(annotations = OfflineControllerAdvice.class)
public class RestExceptionHandler {

    private Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> requestValidationExceptionHandler(RequestValidationException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(MerchantVelocityBreachException.class)
    public ResponseEntity<ErrorResponse> requestValidationExceptionHandler(MerchantVelocityBreachException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(DuplicatePaymentRequestException.class)
    public ResponseEntity<ErrorResponse> duplicatePaymentRequestExceptionHandler(DuplicatePaymentRequestException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(PaymentRequestProcessingException.class)
    public ResponseEntity<ErrorResponse> paymentRequestProcessingExceptionHandler(PaymentRequestProcessingException e) {
        if (StringUtils.equals(e.getResultInfo().getResultCode(), ResponseConstants.INVALID_SSO_TOKEN
                .getSystemResponseCode().toString())) {
            statsDUtils.pushExceptionToStatsD(HttpStatus.UNAUTHORIZED.value(), e.getClass().getSimpleName());
            return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
        if (StringUtils.equals(e.getResultInfo().getResultCode(),
                com.paytm.pgplus.theia.offline.enums.ResultCode.INVALID_SSO_TOKEN_FF.getCode())) {
            statsDUtils.pushExceptionToStatsD(HttpStatus.UNAUTHORIZED.value(), e.getClass().getSimpleName());
            return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(OrderIdGenerationException.class)
    public ResponseEntity<ErrorResponse> paymentRequestProcessingExceptionHandler(OrderIdGenerationException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(BinDetailException.class)
    public ResponseEntity<ErrorResponse> binDetailValidationExceptionHandler(BinDetailException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(PassCodeValidationException.class)
    public ResponseEntity<ErrorResponse> passCodeValidationExceptionHandler(PassCodeValidationException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> baseExceptionHandler(BaseException e) {
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Throwable e) {
        log.error("Unhandled exception:  {}.", ExceptionUtils.getStackTrace(e));
        ResultInfo resultInfo = new ResultInfo();
        ResultCode resultCode = ResultCode.SYSTEM_ERROR;
        resultInfo.setResultStatus(resultCode.getResultStatus());
        resultInfo.setResultCode(resultCode.getCode());
        resultInfo.setResultCodeId(resultCode.getResultCodeId());
        resultInfo.setResultMsg(resultCode.getResultMsg());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(resultInfo));
        errorResponse.setHead(OfflinePaymentUtils.createResponseHeader());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(), e.getClass().getSimpleName());
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    private ErrorResponse getErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(baseResponseBody(e.getResultInfo()));
        errorResponse.setHead(OfflinePaymentUtils.createResponseHeader());
        return errorResponse;
    }

    private ResponseBody baseResponseBody(ResultInfo resultInfo) {
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }
}