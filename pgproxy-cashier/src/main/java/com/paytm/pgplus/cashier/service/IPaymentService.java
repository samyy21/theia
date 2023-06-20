/**
 *
 */
package com.paytm.pgplus.cashier.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

/**
 * @author amit.dubey
 *
 */
public interface IPaymentService {
    GenericCoreResponseBean<InitiatePaymentResponse> initiate(CashierRequest cashierRequest)
            throws CashierCheckedException, PaytmValidationException;

    GenericCoreResponseBean<DoPaymentResponse> submit(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException;

    GenericCoreResponseBean<DoPaymentResponse> initiateAndSubmit(CashierRequest cashierRequest)
            throws CashierCheckedException, PaytmValidationException;

    GenericCoreResponseBean<InitiatePaymentResponse> seamlessBankCardPayment(
            SeamlessBankCardPayRequest seamlessBankCardPayRequest) throws PaytmValidationException;

    GenericCoreResponseBean<InitiatePaymentResponse> chargeFeeOnRiskAnalysis(CashierRequest cashierRequest,
            String externalUserId) throws PaytmValidationException;
}