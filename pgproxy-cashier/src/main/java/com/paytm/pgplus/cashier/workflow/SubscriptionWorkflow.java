package com.paytm.pgplus.cashier.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.pay.service.model.CashierUserCard;
import com.paytm.pgplus.cashier.refund.model.RefundRequest;
import com.paytm.pgplus.cashier.refund.model.RefundResponse;
import com.paytm.pgplus.cashier.refund.service.ICashierRefundService;
import com.paytm.pgplus.cashier.savecard.service.ICashierSaveCardService;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * Subscription Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 7, 2016
 */
@Component(value = "SubscriptionWorkflow")
public class SubscriptionWorkflow extends CardPaymentWorkflow {

    @Autowired
    ICashierSaveCardService saveCardService;

    @Autowired
    ICashierRefundService cashierRefundServiceImpl;

    final void saveUserCard(CashierUserCard card) throws CashierCheckedException {
        saveCardService.saveCard(card);
    }

    final void activateSubscriptionContext() {
    }

    final RefundResponse triggerRefundAPI(RefundRequest refundRequest) throws CashierCheckedException {
        return cashierRefundServiceImpl.processRefund(refundRequest);
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest payload) throws CashierCheckedException, PaytmValidationException {

        DoPaymentResponse doPaymentResponse = super.doPayment(payload);

        if (doPaymentResponse.getPaymentStatus().getPaymentStatusValue().equals("SUCCESS")) {// Need
            // to
            // check

            /*
             * CashierCardRequest cardRequest = payload.getCashierCardRequest();
             * 
             * StringBuilder expiryDate = new StringBuilder();
             * expiryDate.append(
             * cardRequest.getExpiryMonth()).append("/").append
             * (cardRequest.getExpiryYear());
             * 
             * //TODO String cardNumber = cardRequest.getCardNo();
             * 
             * CashierUserCard card = new CashierUserCard();
             * card.setCardNumber(cardNumber);
             * card.setExpiryDate(expiryDate.toString());
             * card.setCardTypeVal(String
             * .valueOf(cardRequest.getCardSchema().ordinal()));
             * 
             * card.setStatusVal(cardRequest.getCardStatus());
             * 
             * card.setUserId(payload.getUserId());
             * 
             * saveUserCard(card);
             * 
             * activateSubscriptionContext();
             * 
             * //TODO RefundRequest refundRequest = new
             * RefundRequest(payload.getMerchantId(),
             * payload.getFundOrderId(),"");
             * 
             * triggerRefundAPI(refundRequest);
             */
        }

        return doPaymentResponse;
    }

}
