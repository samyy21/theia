package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class OrderIdDoesnotMatchException extends BaseException {

    private static final long serialVersionUID = -7639749389329942042L;

    public OrderIdDoesnotMatchException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public OrderIdDoesnotMatchException(ResultInfo resultInfo, Throwable throwable) {
        super(throwable, resultInfo);
    }

    public OrderIdDoesnotMatchException() {
        super();
    }

    public static OrderIdDoesnotMatchException getException() {
        return new OrderIdDoesnotMatchException(OfflinePaymentUtils.resultInfo(ResultCode.ORDER_ID_DOES_NOT_MATCH));
    }

    public static OrderIdDoesnotMatchException getException(Throwable throwable) {
        return new OrderIdDoesnotMatchException(OfflinePaymentUtils.resultInfo(ResultCode.ORDER_ID_DOES_NOT_MATCH),
                throwable);
    }

}
