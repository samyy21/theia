package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import com.paytm.pgplus.theia.paymentoffer.helper.PaymentOffersServiceHelper;
import com.paytm.pgplus.theia.test.testflow.AbstractPaymentServiceTest;
import org.mockito.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class NativePaymentBulkOffersServiceTest {

    @InjectMocks
    NativePaymentBulkOffersService nativePaymentBulkOffersService;

    @Mock
    protected PaymentOffersServiceHelper paymentOffersServiceHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processBulkPaymentOffers() throws InterruptedException, ReflectiveOperationException {
        ExecutorService taskExecutor = Mockito.mock(ExecutorService.class);
        when(taskExecutor.invokeAll(any())).thenReturn(null);
        setPrivateStaticField(NativePaymentBulkOffersService.class, "taskExecutor", taskExecutor);
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setOrderAmount("1000");
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody nativeCashierInfoResponseBody = new NativeCashierInfoResponseBody();
        nativeCashierInfoResponseBody.setPaymentFlow(EPayMode.HYBRID);
        when(paymentOffersServiceHelper.getWalletBalance(any())).thenReturn("10");
        response.setBody(nativeCashierInfoResponseBody);
        nativePaymentBulkOffersService.processBulkPaymentOffers(nativeCashierInfoRequest, response,
                new WorkFlowResponseBean());
        verify(paymentOffersServiceHelper, times(1)).getWalletBalance(any());
    }

    private static void setPrivateStaticField(Class<?> clazz, String fieldName, Object value)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}