package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.models.UPIPushRequest;

public class CommonServiceUtil {

    public static boolean isDisableLoopingForUPI() {
        return Boolean.parseBoolean(ConfigurationUtil
                .getProperty(CashierConstant.STOP_POLLING_FOR_UPI_COLLECT, "false"));
    }

    /**
     * 
     * @param upiPushRequest
     * @return Check for Push or PushExpress flow.
     */
    public static boolean isUPIPushFlow(UPIPushRequest upiPushRequest) {
        return upiPushRequest != null && (upiPushRequest.isUpiPushExpressSupported() || upiPushRequest.isUpiPushTxn());
    }
}
