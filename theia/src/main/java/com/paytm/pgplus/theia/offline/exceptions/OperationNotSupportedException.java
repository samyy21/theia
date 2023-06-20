package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class OperationNotSupportedException extends BaseException {

    private static final long serialVersionUID = -1413749389329942042L;

    public OperationNotSupportedException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public OperationNotSupportedException(ResultInfo resultInfo, Throwable throwable) {
        super(throwable, resultInfo);
    }

    public OperationNotSupportedException() {
        super();
    }

    public static OperationNotSupportedException getException() {
        return new OperationNotSupportedException(OfflinePaymentUtils.resultInfo(ResultCode.OPERATAION_NOT_SUPPORTED));
    }

    public static OperationNotSupportedException getException(Throwable throwable) {
        return new OperationNotSupportedException(OfflinePaymentUtils.resultInfo(ResultCode.OPERATAION_NOT_SUPPORTED),
                throwable);
    }

}
