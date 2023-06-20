package com.paytm.pgplus.theia.accesstoken.exception;

import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ErrorResponse;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.annotation.AccessTokenControllerAdvice;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
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

import javax.servlet.http.HttpServletRequest;

@Order(value = 1)
@ControllerAdvice(annotations = AccessTokenControllerAdvice.class)
public class AccessTokenExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenExceptionHandler.class);

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> rootExceptionHandler(HttpServletRequest request, BaseException e) {
        LOGGER.error(NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getErrorCode()
                + " : {}", e.getMessage());
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> requestValidationExceptionHandler(HttpServletRequest request,
            RequestValidationException e) {

        LOGGER.error(NativeValidationExceptionType.Native_Request_Validation_Exception.getErrorCode() + "due to  : {}",
                e.getMessage());
        // remove this after 21 oct
        try {
            if (StringUtils.equals(e.getResultInfo().getResultCode(), ResultCode.INVALID_FK_SSO_TOKEN.getCode())) {
                statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                        NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
                return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
            }
        } catch (Exception ex) {

        }
        if (StringUtils.equals(e.getResultInfo().getResultCode(), ResultCode.INVALID_TOKEN_TYPE.getCode())
                || (StringUtils.equals(e.getResultInfo().getResultCode(), ResultCode.INVALID_SSO_TOKEN.getCode()))) {
            statsDUtils.pushExceptionToStatsD(HttpStatus.UNAUTHORIZED.value(),
                    NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
            return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Request_Validation_Exception.getType());
        return getErrorResponse(request, e);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Throwable e) {
        LOGGER.error(NativeValidationExceptionType.Native_Unhandled_Exception.getErrorCode() + " :  {}.",
                ExceptionUtils.getStackTrace(e));

        statsDUtils.pushExceptionToStatsD(HttpStatus.OK.value(),
                NativeValidationExceptionType.Native_Unhandled_Exception.getType());
        ResultCode resultCode = ResultCode.SYSTEM_ERROR;
        ResultInfo resultInfo = new ResultInfo(resultCode.getStatus(), resultCode.getId(), resultCode.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setBody(getBaseResponseBody(resultInfo));
        errorResponse.setHead(new ResponseHeader());

        TokenRequestHeader requestHeader = AccessTokenUtils.getRequestHeader();
        if (null != requestHeader) {
            errorResponse.getHead().setRequestId(requestHeader.getRequestId());
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

    private ErrorResponse getErrorResponse(BaseException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        ResultInfo resultInfo = null;
        if (e.getResultInfo() != null) {
            resultInfo = new ResultInfo(e.getResultInfo().getResultStatus(), e.getResultInfo().getResultCodeId(), e
                    .getResultInfo().getResultMsg(), e.getResultInfo().isRedirect());
        } else if (e.getResultCode() != null) {

            ResultCode resultCode = e.getResultCode();
            resultInfo = new ResultInfo(resultCode.getStatus(), resultCode.getId(), resultCode.getCode());
        }
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

    private BaseResponseBody getBaseResponseBody(ResultInfo resultInfo) {
        BaseResponseBody responseBody = new BaseResponseBody();
        responseBody.setResultInfo(resultInfo);
        return responseBody;
    }

    private ResponseEntity<ErrorResponse> getErrorResponse(HttpServletRequest request, BaseException e) {
        return new ResponseEntity<>(this.getErrorResponse(e), HttpStatus.OK);
    }

}