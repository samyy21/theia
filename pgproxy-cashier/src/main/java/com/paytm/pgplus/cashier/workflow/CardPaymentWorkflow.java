package com.paytm.pgplus.cashier.workflow;

import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

public class CardPaymentWorkflow extends PaymentWorkflow {

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_VALIDATION)
    public ValidationHelper validate(ValidationHelper validationHelper) {
        validationHelper = super.validate(validationHelper);
        return validationHelper.validateBinNumber().validateIfSBICardEnabled().validateCardMaxTxnAmountLimit();
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {

        if (null == cashierRequest) {
            throw new CashierCheckedException("Process failed : cashier request can not be null");
        }

        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();

        CacheCardResponseBody cacheCardResponse = null;
        String cacheCardTokenId = "";

        if (cashierRequest.isCardTokenRequired()) {
            cacheCardResponse = getCacheCardResponse(cashierRequest);
            if (null == cacheCardResponse) {
                throw new CashierCheckedException("Process failed : no cache card response found for this request");
            }
            cacheCardTokenId = cacheCardResponse.getTokenId();
        } else {
            cacheCardTokenId = getCacheCardToken(cashierRequest);
        }

        if (StringUtils.isEmpty(cacheCardTokenId)) {
            throw new CashierCheckedException("Process failed : no cache card id found for this request");
        }

        cashierRequest.getPaymentRequest().getPayBillOptions().setCardCacheToken(cacheCardTokenId);
        if (cashierRequest.isCardTokenRequired() && cacheCardResponse != null) {
            cashierRequest.getPaymentRequest().getPayBillOptions().setCardIndexNo(cacheCardResponse.getCardIndexNo());
            cashierRequest.getPaymentRequest().getPayBillOptions().setMaskedCardNo(cacheCardResponse.getMaskedCardNo());
        }
        String cashierRequestId = submitPayment(cashierRequest);

        if (StringUtils.isEmpty(cashierRequestId)) {
            throw new CashierCheckedException("Process failed : no cashier request id found for this request");
        }

        cacheUserInput(cashierRequest, cashierRequestId);

        // populate response object
        initiatePaymentResponse.setCacheCardTokenId(cacheCardTokenId);
        initiatePaymentResponse.setCashierRequestId(cashierRequestId);

        return initiatePaymentResponse;
    }
}
