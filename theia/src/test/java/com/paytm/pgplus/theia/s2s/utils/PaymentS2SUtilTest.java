package com.paytm.pgplus.theia.s2s.utils;

import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequest;
import com.paytm.pgplus.theia.s2s.models.request.PaymentS2SRequestBody;
import org.junit.Test;

import static org.junit.Assert.*;

public class PaymentS2SUtilTest {

    @Test
    public void setMDC() {

        PaymentS2SRequest paymentS2SRequest = new PaymentS2SRequest();
        paymentS2SRequest.setBody(new PaymentS2SRequestBody());
        paymentS2SRequest.getBody().setMid("mid");
        paymentS2SRequest.getBody().setOrderId("orderId");
        PaymentS2SUtil.setMDC(paymentS2SRequest);
    }
}