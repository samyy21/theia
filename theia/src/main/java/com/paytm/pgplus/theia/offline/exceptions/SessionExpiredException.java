package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

/**
 * Created by rahulverma on 1/11/17.
 */
public class SessionExpiredException extends BaseException {

    private static final long serialVersionUID = -182923295003098054L;

    public SessionExpiredException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public SessionExpiredException() {
        super();
    }

    public static SessionExpiredException getException() {
        return new SessionExpiredException(OfflinePaymentUtils.resultInfo(ResultCode.SESSION_EXPIRED_EXCEPTION));
    }

    public static SessionExpiredException getException(ResultCode resultCode) {
        return new SessionExpiredException(OfflinePaymentUtils.resultInfo(resultCode));
    }
}
