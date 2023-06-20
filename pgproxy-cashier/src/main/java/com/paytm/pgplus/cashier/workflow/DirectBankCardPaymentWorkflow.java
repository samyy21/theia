/**
 *
 */
package com.paytm.pgplus.cashier.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.service.ILooperService;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amitdubey
 * @date Dec 4, 2016
 */
@Component(value = "DirectBankCardPaymentWorkflow")
public class DirectBankCardPaymentWorkflow extends PaymentWorkflow {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectBankCardPaymentWorkflow.class);

    @Autowired
    IFacadeService facadeServiceImpl;

    @Autowired
    ILooperService looperServiceImpl;

    @Override
    public InitiatePaymentResponse seamlessBankCardPayment(final SeamlessBankCardPayRequest seamlessBankCardPayRequest)
            throws PaytmValidationException, CashierCheckedException {
        if (seamlessBankCardPayRequest == null) {
            throw new CashierCheckedException(
                    "Unable to process the direct bank card payment as directBankCardPayRequest is NULL");
        }

        LOGGER.debug("DirectBankCardPayRequest Received : {}", seamlessBankCardPayRequest);

        long startTime = System.currentTimeMillis();

        try {
            InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();
            String magicCashierRequestId = facadeServiceImpl.getCashierRequestId(
                    seamlessBankCardPayRequest.getPaymentRequest(),
                    seamlessBankCardPayRequest.getCashierPayOptionBills());
            LOGGER.info("Cashier request id generated for magic retry : {}", magicCashierRequestId);

            CashierPaymentStatus cashierPaymentStatus = looperServiceImpl.fetchBankForm(magicCashierRequestId);
            LOGGER.debug("Cashier payment status in magic retry : {}", cashierPaymentStatus);

            initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);
            initiatePaymentResponse.setCacheCardTokenId(seamlessBankCardPayRequest.getPaymentRequest()
                    .getPayBillOptions().getCardCacheToken());
            initiatePaymentResponse.setCashierRequestId(magicCashierRequestId);

            return initiatePaymentResponse;
        } catch (Exception e) {
            throw new CashierCheckedException("Unable to process the direct bank card payment due to : ", e);
        } finally {
            LOGGER.info("Total time taken for magic bank card payment : {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
