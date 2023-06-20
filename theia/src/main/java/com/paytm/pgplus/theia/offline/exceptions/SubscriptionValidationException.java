package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import org.apache.commons.lang3.StringUtils;

public class SubscriptionValidationException extends BaseException {

    public SubscriptionValidationException() {
        super();
    }

    public SubscriptionValidationException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public static SubscriptionValidationException getException(String message) {
        ResultCode rc = ResultCode.SUBSCRIPTION_VALIDATION_FAILURE;
        ResultInfo resultInfo = new ResultInfo(rc.getResultStatus(), rc.getResultCodeId(), rc.getCode(),
                rc.getResultMsg());
        if (StringUtils.isNotBlank(message)) {
            resultInfo.setResultMsg(message);
        }
        return new SubscriptionValidationException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Subscription_Exception;
    }
}
