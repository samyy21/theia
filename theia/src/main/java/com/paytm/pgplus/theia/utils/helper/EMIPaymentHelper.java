package com.paytm.pgplus.theia.utils.helper;

import com.paytm.pgplus.common.enums.PayMethod;

/**
 * 
 * @author ruchikagarg
 *
 */
public class EMIPaymentHelper {

    /**
     * @param input
     * @return
     */
    public static String isEmi(final String paymentTypeID) {
        String isEMI = "N";
        if (paymentTypeID.equals(PayMethod.EMI.name()))
            isEMI = "Y";
        return isEMI;
    }

    /**
     * 
     * @param months
     * @param bankCode
     * @return
     */
    public static String getEmiPlanId(final String months, final String bankCode) {
        return String.format("%s|%s", bankCode, months);
    }
}
