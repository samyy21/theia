package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 22/9/17.
 */
public class OrderIdGenerationException extends BaseException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -889522805411572381L;

    public OrderIdGenerationException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public OrderIdGenerationException() {
        super();
    }

    public static OrderIdGenerationException getException() {
        return new OrderIdGenerationException(OfflinePaymentUtils.resultInfo(ResultCode.ORDER_ID_GENERATION_EXCEPTION));
    }

    public static OrderIdGenerationException getException(ResultCode resultCode) {
        return new OrderIdGenerationException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_OrderId_Generation_Exception;
    }
}
