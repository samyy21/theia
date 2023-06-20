package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidRequestParameterException extends RuntimeException {
    private static final long serialVersionUID = 8872092576584634560L;
    private PaymentRequestBean paymentRequestBean;

    public InvalidRequestParameterException() {
    }

    public InvalidRequestParameterException(String message) {
        super(message);
    }

    public InvalidRequestParameterException(String message, PaymentRequestBean paymentRequestBean) {
        super(message);
        this.paymentRequestBean = paymentRequestBean;
    }

    public PaymentRequestBean getpaymentRequestBean() {
        return this.paymentRequestBean;
    }
}
