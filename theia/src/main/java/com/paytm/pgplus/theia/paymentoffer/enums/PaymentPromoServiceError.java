package com.paytm.pgplus.theia.paymentoffer.enums;

import com.paytm.pgplus.theia.offline.enums.ResultCode;

import java.util.Optional;

public enum PaymentPromoServiceError {

    PROMO_APPLY_4001(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);

    private ResultCode resultCode;

    PaymentPromoServiceError(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public static Optional<PaymentPromoServiceError> fromString(String s) {
        for (PaymentPromoServiceError val : values()) {
            if (val.name().equals(s)) {
                return Optional.ofNullable(val);
            }
        }
        return Optional.empty();
    }
}
