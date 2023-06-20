package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.utils.MapperUtils;

public class MerchantVelocityBreachException extends BaseException {

    private static final long serialVersionUID = 1L;

    public MerchantVelocityBreachException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public MerchantVelocityBreachException() {
        super();
    }

    public MerchantVelocityBreachException(Throwable throwable, ResultInfo resultInfo) {
        super(throwable, resultInfo);
    }

    public static MerchantVelocityBreachException getException() {
        return new MerchantVelocityBreachException(
                OfflinePaymentUtils.resultInfo(ResultCode.MERCHANT_VELOCITY_LIMIT_BREACH));
    }

    public static MerchantVelocityBreachException getException(ResultCode resultCode) {
        return new MerchantVelocityBreachException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static MerchantVelocityBreachException getException(Throwable throwable, ResultCode resultCode) {
        return new MerchantVelocityBreachException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static MerchantVelocityBreachException getException(String limitType, String limitDuration) {
        return new MerchantVelocityBreachException(OfflinePaymentUtils.resultInfo(MapperUtils
                .getResultCodeForMerchantBreached(limitType, limitDuration)));
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Merchant_Velocity_Breach_Exception;
    }
}
