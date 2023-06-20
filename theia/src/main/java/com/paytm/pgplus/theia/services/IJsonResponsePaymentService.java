/**
 * 
 */
package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.NativeJsonResponse;

/**
 * @author namanjain
 *
 */
public interface IJsonResponsePaymentService {

    WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException;

    ValidationResults validatePaymentRequest(PaymentRequestBean requestData) throws PaymentRequestValidationException;

    String getResponseWithChecksumForJsonResponse(String response, String clientId);
}
