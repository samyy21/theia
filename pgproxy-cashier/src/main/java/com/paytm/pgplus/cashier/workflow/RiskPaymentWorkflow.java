/**
 *
 */
package com.paytm.pgplus.cashier.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.paytm.pgplus.cashier.redis.IPgProxyCashierTransactionalRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.risk.services.ICashierRisk;
import com.paytm.pgplus.cashier.util.ConfigurationUtil;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amitdubey
 * @date Mar 16, 2017
 */

@Component(value = "RiskPaymentWorkflow")
public class RiskPaymentWorkflow extends CardPaymentWorkflow {
    public static final Logger LOGGER = LoggerFactory.getLogger(RiskPaymentWorkflow.class);
    private static final double RISK_BASE_FEE = Double.parseDouble(ConfigurationUtil.getProperty(
            "risk.convenience.fee", "1.74"));
    private static final String FEE_AMOUNT = "payMethodFeeAmount";
    private static final String FEE_CURRENCY = "payMethodFeeCurrency";
    private static final String FEE_CURRENCY_VAL = "INR";

    @Autowired
    ICashierRisk cashierRiskImpl;

    @Autowired
    private IPgProxyCashierTransactionalRedisUtil pgProxyCashierTransactionalRedisUtil;

    @Override
    public InitiatePaymentResponse riskConsult(CashierRequest cashierRequest, String userId)
            throws PaytmValidationException, CashierCheckedException {
        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();

        String cacheCardToken = getCacheCardToken(cashierRequest);

        // Set the cache card token in the payment request for re-use
        PayBillOptions payBillOptions = cashierRequest.getPaymentRequest().getPayBillOptions();
        payBillOptions.setCardCacheToken(cacheCardToken);

        boolean riskApplied = cashierRiskImpl.riskPolicyConsult(cashierRequest, cacheCardToken, userId);

        if (riskApplied) {
            LOGGER.info("selected convenince fee applicable for this order");

            /* Half Round UP */
            long additionalRiskFee;

            try {
                BigDecimal additionalChargeFee = new BigDecimal(cashierRequest.getPaymentRequest().getPayBillOptions()
                        .getServiceAmount()
                        * RISK_BASE_FEE).divide(new BigDecimal(100), RoundingMode.HALF_UP);
                additionalRiskFee = additionalChargeFee.longValue();
            } catch (Exception e) {
                LOGGER.warn("Exception occured while calculating risk fee", e);
                additionalRiskFee = (long) ((cashierRequest.getPaymentRequest().getPayBillOptions().getServiceAmount() * RISK_BASE_FEE) / 100);
            }
            ConsultDetails consultDetails = cashierRiskImpl.serviceTaxConsult(cashierRequest, additionalRiskFee);

            initiatePaymentResponse.setCacheCardTokenId(cacheCardToken);
            initiatePaymentResponse.setConsultDetails(consultDetails);

            // update the charge fee
            Double feeAmount = consultDetails.getTotalConvenienceCharges().doubleValue() * 100;
            payBillOptions.setChargeFeeAmount(feeAmount.longValue());
            payBillOptions.getExtendInfo().put(FEE_AMOUNT, String.valueOf(additionalRiskFee));
            payBillOptions.getExtendInfo().put(FEE_CURRENCY, FEE_CURRENCY_VAL);

            // Add data on behalf of cache card token
            StringBuilder key = new StringBuilder(CashierConstant.RISK_RETRY_KEY).append(cacheCardToken);
            pgProxyCashierTransactionalRedisUtil.set(key.toString(), cashierRequest, 900);
        } else {
            String cashierRequestId = submitPayment(cashierRequest);
            cacheUserInput(cashierRequest, cashierRequestId);

            // populate response object
            initiatePaymentResponse.setCacheCardTokenId(cacheCardToken);
            initiatePaymentResponse.setCashierRequestId(cashierRequestId);

            CashierPaymentStatus cashierPaymentStatus = fetchBankForm(cashierRequestId);
            initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);
        }

        return initiatePaymentResponse;
    }
}