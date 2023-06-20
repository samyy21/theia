package com.paytm.pgplus.theia.nativ.exception;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class ImeiValidationException extends BaseException {
    private ResultInfo resultInfo;

    public ImeiValidationException(ResultCode resultCode) {
        super(resultCode);
        this.resultInfo = OfflinePaymentUtils.resultInfo(resultCode);
    }

    public ResultInfo getImeiValidationExceptionResultInfo() {
        return resultInfo;
    }
}
