package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class FetchMerchantInfoException extends BaseException {

    private static final long serialVersionUID = -1413749389329942042L;

    public FetchMerchantInfoException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public FetchMerchantInfoException(ResultInfo resultInfo, Throwable throwable) {
        super(throwable, resultInfo);
    }

    public FetchMerchantInfoException() {
        super();
    }

    public static FetchMerchantInfoException getException() {
        return new FetchMerchantInfoException(OfflinePaymentUtils.resultInfo(ResultCode.FETCH_MERCHANT_INFO_EXCEPTION));
    }

    public static FetchMerchantInfoException getException(Throwable throwable) {
        return new FetchMerchantInfoException(OfflinePaymentUtils.resultInfo(ResultCode.FETCH_MERCHANT_INFO_EXCEPTION),
                throwable);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_MerchantInfo_Fetching_Exception;
    }

}
