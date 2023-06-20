package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;

public class SubscriptionServiceException extends BaseException {

    private static final long serialVersionUID = -874500287107394655L;

    public SubscriptionServiceException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public SubscriptionServiceException() {
        super();
    }

    public SubscriptionServiceException(Throwable throwable, ResultInfo resultInfo) {
        super(throwable, resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Subscription_Exception;
    }
}
