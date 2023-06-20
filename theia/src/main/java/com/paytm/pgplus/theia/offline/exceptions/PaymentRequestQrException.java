package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class PaymentRequestQrException extends BaseException {
    /**
     *
     */
    private static final long serialVersionUID = -5602367170386111878L;

    private ResponseConstants responseConstants;

    public PaymentRequestQrException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public PaymentRequestQrException(ResultInfo resultInfo, ResponseConstants responseConstants) {
        super(resultInfo);
        this.responseConstants = responseConstants;
    }

    public PaymentRequestQrException(ResultCode resultCode, ResponseConstants responseConstants) {
        super(resultCode);
        this.responseConstants = responseConstants;
    }

    public PaymentRequestQrException(ResultCode resultCode) {
        super(resultCode);
    }

    public PaymentRequestQrException() {
    }

    public static PaymentRequestQrException getException(String msg) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(ResultCode.PAYMENT_REQUEST_QR_EXCEPTION);
        resultInfo.setResultMsg(msg);
        return new PaymentRequestQrException(resultInfo);
    }

    public static PaymentRequestQrException getException(ResultCode resultCode) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(resultCode);
        return new PaymentRequestQrException(resultInfo);
    }

    public static PaymentRequestQrException getException(ResponseConstants responseConstants) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        resultInfo.setResultCode(responseConstants.getCode());
        resultInfo.setResultMsg(responseConstants.getMessage());
        return new PaymentRequestQrException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Qr_Validation_Exception;
    }

    public ResponseConstants getResponseConstants() {
        return responseConstants;
    }
}
