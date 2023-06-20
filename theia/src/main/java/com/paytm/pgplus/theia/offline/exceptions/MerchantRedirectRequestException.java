package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class MerchantRedirectRequestException extends BaseException {

    private static final long serialVersionUID = 1L;

    public MerchantRedirectRequestException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public MerchantRedirectRequestException() {
    }

    public static MerchantRedirectRequestException getException() {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        return new MerchantRedirectRequestException(resultInfo);
    }

    public static MerchantRedirectRequestException getException(String msg) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_REDIRECT_REQUEST_EXCEPTION);
        resultInfo.setResultMsg(msg);
        return new MerchantRedirectRequestException(resultInfo);
    }

    public static MerchantRedirectRequestException getException(ResultCode resultCode) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(resultCode);
        return new MerchantRedirectRequestException(resultInfo);
    }

}
