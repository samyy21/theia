/**
 *
 */
package com.paytm.pgplus.theia.services;

import java.io.Serializable;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import org.springframework.ui.Model;

import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author kesari
 * @createdOn 27-Mar-2016
 */
public interface IPaymentService extends Serializable {

    /**
     * @param requestData
     * @param responseModel
     * @return
     * @throws TheiaServiceException
     */
    PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException;

    /**
     * @param requestData
     * @return
     * @throws PaymentRequestValidationException
     */
    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    ValidationResults validatePaymentRequest(PaymentRequestBean requestData) throws PaymentRequestValidationException;
}
