package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 30/10/17.
 */
public class DuplicatePaymentRequestException extends BaseException {

    private static final long serialVersionUID = 8138798019622151386L;

    public DuplicatePaymentRequestException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public DuplicatePaymentRequestException() {
        super();
    }

    public static DuplicatePaymentRequestException getException() {
        return new DuplicatePaymentRequestException(
                OfflinePaymentUtils.resultInfo(ResultCode.DUPLICATE_PAYMENT_REQUEST_EXCEPTION));
    }

    public static DuplicatePaymentRequestException getException(ResultCode resultCode) {
        return new DuplicatePaymentRequestException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static DuplicatePaymentRequestException getException(ResultInfo resultInfo) {
        return new DuplicatePaymentRequestException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Duplicate_Payment_Request_Exception;
    }
}