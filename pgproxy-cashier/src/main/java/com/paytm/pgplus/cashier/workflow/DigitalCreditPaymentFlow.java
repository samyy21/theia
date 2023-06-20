package com.paytm.pgplus.cashier.workflow;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.CashierRequest.CashierRequestBuilder;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.validator.service.IDigitalCreditPassCodeValidator;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * Paytm CC Specific Flow
 *
 * @author kartik
 * @date 09-05-2017
 */
@Component(value = "DigitalCreditPaymentFlow")
public class DigitalCreditPaymentFlow extends PaymentWorkflow {

    @Autowired
    @Qualifier("digitalCreditPassCodeValidator")
    private IDigitalCreditPassCodeValidator passCodeValidator;

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException {
        throw new UnsupportedOperationException(CashierConstant.UNSUPPORTED_OPERATION);
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {

        validateDigitalCreditRequest(cashierRequest);
        passCodeValidator.validatePassCodeForPaytmCC(cashierRequest);

        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);

        if (null == initiatePaymentResponse) {
            throw new CashierCheckedException("Process failed : initiate payment response received null ");
        }

        CashierRequestBuilder cashierRequestBuilder = new CashierRequestBuilder(cashierRequest.getAcquirementId(),
                cashierRequest.getRequestId(), cashierRequest.getCashierWorkflow());

        LooperRequest looperRequest = new LooperRequest(initiatePaymentResponse.getCashierRequestId(), cashierRequest
                .getCashierMerchant().getInternalMerchantId(), cashierRequest.getPaymentRequest().getTransId());
        cashierRequestBuilder.setLooperRequest(looperRequest);

        DoPaymentResponse doPaymentResponse = super.doPayment(cashierRequestBuilder.build());

        if (null == doPaymentResponse) {
            throw new CashierCheckedException("Process failed : do payment response received null");
        }

        return doPaymentResponse;
    }

    private void validateDigitalCreditRequest(CashierRequest cashierRequest) throws CashierCheckedException {

        if (null == cashierRequest) {
            throw new CashierInvalidParameterException("Process failed : cashier request can not be null");
        }

        if (TransType.ACQUIRING != cashierRequest.getPaymentRequest().getTransType()) {
            throw new CashierCheckedException("Process failed : only acquiring as trans type allowed");
        }

        if (StringUtils.isEmpty(cashierRequest.getPaymentRequest().getPayerUserId())) {
            throw new CashierCheckedException("Process failed  : payer user id can not be null");
        }

        if (null == cashierRequest.getDigitalCreditRequest()) {
            throw new CashierCheckedException("Process failed  : digitalCreditRequest can not be null");
        }

        if (StringUtils.isEmpty(cashierRequest.getDigitalCreditRequest().getExternalAccountNo())) {
            throw new CashierCheckedException("Process failed : PaytmCC account number can not be null");
        }

        if (StringUtils.isEmpty(cashierRequest.getDigitalCreditRequest().getLenderId())) {
            throw new CashierCheckedException("Process failed : lender id cannot be null");
        }

        if (cashierRequest.getPaymentRequest().getPayBillOptions().isTopupAndPay()) {
            throw new CashierCheckedException("Process failed : topUpAndPay not supported for PaytmCC");
        }

        if (StringUtils.isBlank(cashierRequest.getDigitalCreditRequest().getSsoToken())) {
            throw new CashierCheckedException("Process failed : Passcode is not required and ssoToken is empty");
        }
    }

}
