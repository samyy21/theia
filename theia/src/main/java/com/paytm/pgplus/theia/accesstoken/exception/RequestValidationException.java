package com.paytm.pgplus.theia.accesstoken.exception;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;

public class RequestValidationException extends BaseException {

    private static final long serialVersionUID = 3733479141860594053L;

    public RequestValidationException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public RequestValidationException() {
        super();
    }

    public RequestValidationException(Throwable throwable, ResultInfo resultInfo) {
        super(throwable, resultInfo);
    }

    public static RequestValidationException getException() {
        return new RequestValidationException(AccessTokenUtils.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION));
    }

    public static RequestValidationException getException(ResultCode resultCode) {
        return new RequestValidationException(AccessTokenUtils.resultInfo(resultCode));
    }

    public static RequestValidationException getException(Throwable throwable, ResultCode resultCode) {
        return new RequestValidationException(AccessTokenUtils.resultInfo(resultCode));
    }

    public static RequestValidationException getExceptionWithAffixedMessage(ResultCode resultCode, String message) {
        ResultInfo resultInfo = AccessTokenUtils.resultInfo(resultCode);
        resultInfo.setResultMsg(resultInfo.getResultMsg() + message);
        return new RequestValidationException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Request_Validation_Exception;
    }

}
