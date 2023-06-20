package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class MidDoesnotMatchException extends BaseException {

    private static final long serialVersionUID = -8345749389329942042L;

    public MidDoesnotMatchException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public MidDoesnotMatchException(ResultInfo resultInfo, Throwable throwable) {
        super(throwable, resultInfo);
    }

    public MidDoesnotMatchException() {
        super();
    }

    public static MidDoesnotMatchException getException() {
        return new MidDoesnotMatchException(OfflinePaymentUtils.resultInfo(ResultCode.MID_DOES_NOT_MATCH));
    }

    public static MidDoesnotMatchException getException(Throwable throwable) {
        return new MidDoesnotMatchException(OfflinePaymentUtils.resultInfo(ResultCode.MID_DOES_NOT_MATCH), throwable);
    }

}
