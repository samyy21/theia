package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 13/10/17.
 */
public class PassCodeValidationException extends BaseException {

    private static final long serialVersionUID = -159427680210783622L;

    public PassCodeValidationException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public PassCodeValidationException() {
        super();
    }

    public static PassCodeValidationException getException() {
        return new PassCodeValidationException(OfflinePaymentUtils.resultInfo(ResultCode.INVALID_PASS_CODE));
    }

    public static PassCodeValidationException getException(ResultCode resultCode) {
        return new PassCodeValidationException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static PassCodeValidationException getException(ResultInfo resultInfo) {
        return new PassCodeValidationException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Passcode_Validation_Exception;
    }
}
