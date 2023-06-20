package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * Created by rahulverma on 28/8/17.
 */
@Data
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = -7729054899007907873L;

    private ResultInfo resultInfo;
    private ResultCode resultCode;

    public BaseException(ResultInfo resultInfo) {
        super(resultInfo.getResultMsg());
        this.resultInfo = resultInfo;
    }

    public BaseException(ResultCode resultcode) {
        super(resultcode.getResultMsg());
        this.resultCode = resultcode;
    }

    public BaseException() {
        super(ResultCode.UNKNOWN_ERROR.getResultMsg());
        this.resultInfo = OfflinePaymentUtils.resultInfo(ResultCode.UNKNOWN_ERROR);
    }

    public static BaseException getException(String msg) {
        BaseException exception = new BaseException();
        if (StringUtils.isNotBlank(msg)) {
            exception.getResultInfo().setResultMsg(msg);
        }
        return exception;
    }

    public static BaseException getException(ResultCode resultCode) {
        BaseException exception = new BaseException(OfflinePaymentUtils.resultInfo(resultCode));
        return exception;
    }

    public static BaseException getException() {
        BaseException exception = new BaseException();
        return exception;
    }

    public BaseException(Throwable throwable, ResultInfo resultInfo) {
        super(throwable);
        this.resultInfo = resultInfo;
    }

    public static BaseException getException(ResultInfo resultInfo) {
        BaseException exception = new BaseException(resultInfo);
        return exception;
    }

    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
