package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;

import javax.servlet.http.HttpServletRequest;

public interface IOAuthResponseService {

    void processLoginResponse(PaymentRequestBean paymentRequestData);

    void incrementLoginRetryCount(PaymentRequestBean paymentRequestData);

    String processLoginForEnhancedCashierFlow(HttpServletRequest request) throws Exception;
}
