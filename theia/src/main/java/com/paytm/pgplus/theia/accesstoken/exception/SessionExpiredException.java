package com.paytm.pgplus.theia.accesstoken.exception;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;

public class SessionExpiredException extends BaseException {

    private static final long serialVersionUID = 2466142695033947422L;

    public SessionExpiredException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public SessionExpiredException() {
        super();
    }

    public static SessionExpiredException getException() {
        return new SessionExpiredException(AccessTokenUtils.resultInfo(ResultCode.SESSION_EXPIRED));
    }

    public static SessionExpiredException getException(ResultCode resultCode) {
        return new SessionExpiredException(AccessTokenUtils.resultInfo(resultCode));
    }
}
