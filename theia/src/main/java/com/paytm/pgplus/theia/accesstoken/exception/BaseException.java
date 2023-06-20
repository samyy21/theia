package com.paytm.pgplus.theia.accesstoken.exception;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 201926484293185932L;

    private ResultInfo resultInfo;
    private ResultCode resultCode;

    public BaseException(ResultInfo resultInfo) {
        super(resultInfo.getResultMsg());
        this.resultInfo = resultInfo;
    }

    public BaseException(ResultCode resultcode) {
        super(resultcode.getMessage());
        this.resultCode = resultcode;
    }

    public BaseException() {
        super(ResultCode.UNKNOWN_ERROR.getMessage());
        this.resultInfo = getResultInfo(ResultCode.UNKNOWN_ERROR);
    }

    public static BaseException getException(String msg) {
        BaseException exception = new BaseException();
        if (StringUtils.isNotBlank(msg)) {
            exception.getResultInfo().setResultMsg(msg);
        }
        return exception;
    }

    public static BaseException getException(ResultCode resultCode) {
        BaseException exception = new BaseException(getResultInfo(resultCode));
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

    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Exception_Occurred_While_Processing_API;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public static ResultInfo getResultInfo(ResultCode resultCode) {
        if (resultCode == null)
            resultCode = ResultCode.UNKNOWN_ERROR;
        return new ResultInfo(resultCode.getStatus(), resultCode.getId(), resultCode.getCode(), resultCode.getMessage());
    }
}
