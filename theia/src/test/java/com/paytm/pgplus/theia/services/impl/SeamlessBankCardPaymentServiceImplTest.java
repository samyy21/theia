package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.service.IPaymentService;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SeamlessBankCardPaymentServiceImplTest {

    @InjectMocks
    SeamlessBankCardPaymentServiceImpl seamlessBankCardPaymentService;

    @Mock
    ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Mock
    SeamlessBankCardPayRequest seamlessBankCardPayRequest;

    @Mock
    PaymentRequest paymentRequest;

    @Mock
    IPaymentService paymentServiceImpl;

    @Mock
    PayBillOptions payBillOptions;

    @Mock
    GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = TheiaServiceException.class)
    public void testDoSeamlessBankCardPaymentWhenSeamlessBankCardPayRequestNull() throws PaytmValidationException {
        when(theiaTransactionalRedisUtil.get(anyString())).thenReturn(null);
        seamlessBankCardPaymentService.doSeamlessBankCardPayment("test", "test");
    }

    @Test
    public void testDoSeamlessBankCardPaymentWhenSeamlessBankCardPayRequestNotNull() throws PaytmValidationException {
        Map<String, String> testMap = new HashMap<>();
        testMap.put(TheiaConstant.ExtendedInfoPay.PAYTM_MERCHANT_ID, "test");
        testMap.put(TheiaConstant.ExtendedInfoPay.MERCHANT_TRANS_ID, "test");
        when(theiaTransactionalRedisUtil.get(anyString())).thenReturn(seamlessBankCardPayRequest);
        when(seamlessBankCardPayRequest.getPaymentRequest()).thenReturn(paymentRequest);
        when(paymentRequest.getExtendInfo()).thenReturn(testMap);
        when(paymentRequest.getPayBillOptions()).thenReturn(payBillOptions);
        when(payBillOptions.getChannelInfo()).thenReturn(testMap);
        when(paymentServiceImpl.seamlessBankCardPayment(any())).thenReturn(initiatePaymentResponse);
        when(theiaResponseGenerator.getBankPage(initiatePaymentResponse)).thenReturn("test");
        Assert.assertNotNull(seamlessBankCardPaymentService.doSeamlessBankCardPayment("test", "test"));
    }

    @Test(expected = TheiaServiceException.class)
    public void testGetSeamlessBankCardPayRequestWhenSeamlessBankCardPayRequestNull() throws PaytmValidationException {
        when(theiaTransactionalRedisUtil.get(anyString())).thenReturn(null);
        seamlessBankCardPaymentService.getSeamlessBankCardPayRequest("test");
    }

    @Test
    public void testGetSeamlessBankCardPayRequestWhenSeamlessBankCardPayRequestNotNull()
            throws PaytmValidationException {
        when(theiaTransactionalRedisUtil.get(anyString())).thenReturn(seamlessBankCardPayRequest);

        Assert.assertNotNull(seamlessBankCardPaymentService.getSeamlessBankCardPayRequest("test"));
    }

}