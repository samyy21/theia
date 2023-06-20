/**
 *
 */
package com.paytm.pgplus.cashier.risk.services;

import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amitdubey
 * @date Mar 16, 2017
 */
public interface ICashierRisk {

    boolean riskPolicyConsult(CashierRequest cashierRequest, String cacheCardToken, String userId)
            throws PaytmValidationException;

    ConsultDetails serviceTaxConsult(CashierRequest cashierRequest, long additionalChargeFee)
            throws PaytmValidationException;

}
