/**
 *
 */
package com.paytm.pgplus.cashier.risk.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.risk.services.ICashierRisk;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amitdubey
 * @date Mar 16, 2017
 */
@Service("cashierRiskImpl")
public class CashierRiskImpl implements ICashierRisk {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashierRiskImpl.class);

    @Autowired
    IFacadeService facadeService;

    @Override
    public boolean riskPolicyConsult(CashierRequest cashierRequest, String cacheCardToken, String userId)
            throws PaytmValidationException {
        boolean riskFound = false;
        try {
            riskFound = facadeService.riskConsultResponse(cashierRequest, cacheCardToken, userId);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Unable to communicate with RISK_POLICY_CONSULT API", e);
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_RISK_CONSULT);
        }

        return riskFound;
    }

    @Override
    public ConsultDetails serviceTaxConsult(CashierRequest cashierRequest, long additionalChargeFee)
            throws PaytmValidationException {
        try {
            ConsultDetails consultDetails = facadeService.totalChargeFeeAmount(cashierRequest, additionalChargeFee);

            if (consultDetails == null) {
                throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CONSULT_DETAILS);
            }
            return consultDetails;
        } catch (FacadeCheckedException | CashierCheckedException e) {
            LOGGER.error("Unable to communicate with CHARGE_FEE_CONSULT API", e);
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CONSULT_DETAILS);
        }
    }
}
