package com.paytm.pgplus.theia.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.subscriptionClient.model.request.RenewRequestBean;
import com.paytm.pgplus.subscriptionClient.model.response.SubscriptionRenewalResponse;
import com.paytm.pgplus.subscriptionClient.service.ISubscriptionService;
import com.paytm.pgplus.theia.workflow.SubscriptionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionServiceTest {
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private ISubscriptionService IsubscriptionService;

    @Test
    public void setRenewRequestTest() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        setPaymentRequestBean(paymentRequestBean);
        RenewRequestBean expected = new RenewRequestBean("test", "test", "test", new Money("test"), "test", "test",
                "test", "test");
        RenewRequestBean actual = subscriptionService.setRenewRequest(paymentRequestBean);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void setRenewRequestTestWhenReqBeanIsEmptyTest() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        RenewRequestBean expected = new RenewRequestBean(null, null, null, new Money(null), null, null, null, null);
        RenewRequestBean actual = subscriptionService.setRenewRequest(paymentRequestBean);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void subscriptionRenewalResponseTest() throws Exception {

        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        setPaymentRequestBean(paymentRequestBean);

        SubscriptionRenewalResponse subscriptionRenewalResponse = new SubscriptionRenewalResponse();
        setSubscriptionRenewalResponse(subscriptionRenewalResponse);

        when(IsubscriptionService.processRenewSubscription(any())).thenReturn(subscriptionRenewalResponse);

        String expected = JsonMapper.mapObjectToJson(subscriptionRenewalResponse);
        String actual = subscriptionService.subscriptionRenewalResponse(paymentRequestBean);

        assertEquals(expected, actual);
    }

    @Test
    public void subscriptionRenewalResponseTestWhenReqBeanIsNullTest() throws FacadeCheckedException {

        PaymentRequestBean paymentRequestBean = null;
        String actual = subscriptionService.subscriptionRenewalResponse(paymentRequestBean);
        assertEquals("", actual);
    }

    @Test
    public void subscriptionRenewalResponseTestWhenReqBeanIsEmptyTest() throws Exception {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        SubscriptionRenewalResponse subscriptionRenewalResponse = new SubscriptionRenewalResponse();
        when(IsubscriptionService.processRenewSubscription(any())).thenReturn(subscriptionRenewalResponse);
        String expected = JsonMapper.mapObjectToJson(subscriptionRenewalResponse);
        String actual = subscriptionService.subscriptionRenewalResponse(paymentRequestBean);
        assertEquals(actual, expected);
    }

    public void setPaymentRequestBean(PaymentRequestBean paymentRequestBean) {
        paymentRequestBean.setRequestType("test");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnAmount("test");
        paymentRequestBean.setChecksumhash("test");
        paymentRequestBean.setSubscriptionID("test");
    }

    public void setSubscriptionRenewalResponse(SubscriptionRenewalResponse subscriptionRenewalResponse) {
        subscriptionRenewalResponse.setMid("test");
        subscriptionRenewalResponse.setOrderId("test");
        subscriptionRenewalResponse.setTxnAmount("test");
        subscriptionRenewalResponse.setRespCode("test");
        subscriptionRenewalResponse.setRespMsg("test");
        subscriptionRenewalResponse.setTxnId("test");
        subscriptionRenewalResponse.setSubsId("test");
        subscriptionRenewalResponse.setStatus("test");
    }

}
