package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 7/9/17.
 */
public class PaymentRequestProcessingException extends BaseException {
    /**
	 * 
	 */
    private static final long serialVersionUID = -7829467301699299502L;

    private ResponseConstants responseConstants;

    public PaymentRequestProcessingException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public PaymentRequestProcessingException(ResultInfo resultInfo, ResponseConstants responseConstants) {
        super(resultInfo);
        this.responseConstants = responseConstants;
    }

    public PaymentRequestProcessingException(ResultCode resultCode) {
        super(resultCode);
    }

    public PaymentRequestProcessingException() {
    }

    public static PaymentRequestProcessingException getException(String msg) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(ResultCode.PAYMENT_REQUEST_PROCESSING_EXCEPTION);
        resultInfo.setResultMsg(msg);
        return new PaymentRequestProcessingException(resultInfo);
    }

    public static PaymentRequestProcessingException getException(ResultCode resultCode) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(resultCode);
        return new PaymentRequestProcessingException(resultInfo);
    }

    public static PaymentRequestProcessingException getException(String respMsg, ResponseConstants responseConstants) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(responseConstants.getSystemResponseCode().toString());
        resultInfo.setResultMsg(respMsg);
        return new PaymentRequestProcessingException(resultInfo);
    }

    public static PaymentRequestProcessingException getException(ResponseConstants responseConstants) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(responseConstants.getSystemResponseCode().toString());
        resultInfo.setResultMsg(responseConstants.getMessage());
        return new PaymentRequestProcessingException(resultInfo);
    }

    public static PaymentRequestProcessingException getExceptionWithResultInfoAndResponseConstant(
            ResponseConstants responseConstants) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultCode(responseConstants.getSystemResponseCode().toString());
        resultInfo.setResultMsg(responseConstants.getMessage());
        return new PaymentRequestProcessingException(resultInfo, responseConstants);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Payment_Request_Processing_Exception;
    }

    public ResponseConstants getResponseConstants() {
        return responseConstants;
    }
}