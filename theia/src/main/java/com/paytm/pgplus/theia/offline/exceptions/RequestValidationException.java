package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 5/9/17.
 */
public class RequestValidationException extends BaseException {

    private static final long serialVersionUID = 8140856858915498528L;

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
        return new RequestValidationException(
                OfflinePaymentUtils.resultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION));
    }

    public static RequestValidationException getException(ResultCode resultCode) {
        return new RequestValidationException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static RequestValidationException getException(Throwable throwable, ResultCode resultCode) {
        return new RequestValidationException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Request_Validation_Exception;
    }

}
