package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class NativePayviewConsultRequestProcessorTest {

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @InjectMocks
    NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Test
    public void testSetIsMerchantUPIVerified_MerchantPreferenceEnabled_shouldSetTrue() {
        String mid = "ankitmid";
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = Mockito.spy(new NativeCashierInfoResponseBody());
        body.setMerchantUPIVerified(false);
        response.setBody(body);
        Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(true);
        nativePayviewConsultRequestProcessor.setIsMerchantUPIVerified(response, mid);
        Assert.assertTrue(response.getBody().getMerchantUPIVerified());
    }

    @Test
    public void testSetIsMerchantUPIVerified_MerchantPreferenceEnabled_shouldSetFalse() {
        String mid = "ankitmid";
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = Mockito.spy(new NativeCashierInfoResponseBody());
        body.setMerchantUPIVerified(true);
        response.setBody(body);
        Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, false)).thenReturn(false);
        nativePayviewConsultRequestProcessor.setIsMerchantUPIVerified(response, mid);
        Assert.assertTrue(!response.getBody().getMerchantUPIVerified());
    }

    @Test
    public void testSetIsMerchantUPIVerified_MidNull_shouldSetTrue() {
        String mid = null;
        NativeCashierInfoResponse response = new NativeCashierInfoResponse();
        NativeCashierInfoResponseBody body = Mockito.spy(new NativeCashierInfoResponseBody());
        body.setMerchantUPIVerified(false);
        response.setBody(body);
        nativePayviewConsultRequestProcessor.setIsMerchantUPIVerified(response, mid);
        Assert.assertTrue(response.getBody().getMerchantUPIVerified());
    }

    @Test
    public void testDisableUPICollectIfMerchantNotVerified_merchantPrefDisabledAndtxnGreater2000_shouldAddUPIInDisablePayModes() {

        String txnToken = "asdkjaskdlksajdlkjas";
        String mid = "ankitPay";
        Money txnAmt = new Money();
        txnAmt.setValue("2001");

        try {
            Method disableUPICollectIfMerchantNotVerified = NativePayviewConsultRequestProcessor.class
                    .getDeclaredMethod("disableUPICollectIfMerchantNotVerified", InitiateTransactionRequestBody.class,
                            NativeCashierInfoContainerRequest.class);
            disableUPICollectIfMerchantNotVerified.setAccessible(true);

            InitiateTransactionRequestBody orderDetail = Mockito.mock(InitiateTransactionRequestBody.class);
            Mockito.when(orderDetail.getMid()).thenReturn(mid);
            Mockito.when(orderDetail.getTxnAmount()).thenReturn(txnAmt);
            NativeCashierInfoContainerRequest request = Mockito.mock(NativeCashierInfoContainerRequest.class);
            NativeCashierInfoRequest nativeCashierInfoRequest = Mockito.mock(NativeCashierInfoRequest.class);
            NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
            nativeCashierInfoRequestBody.setDisablePaymentMode(new ArrayList<>());
            Mockito.when(nativeCashierInfoRequest.getBody()).thenReturn(nativeCashierInfoRequestBody);
            Mockito.when(request.getNativeCashierInfoRequest()).thenReturn(nativeCashierInfoRequest);

            Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(false);
            disableUPICollectIfMerchantNotVerified.invoke(nativePayviewConsultRequestProcessor, orderDetail, request);
            Assert.assertTrue(request.getNativeCashierInfoRequest().getBody().getDisablePaymentMode().get(0).getMode()
                    .equals("UPI"));
            Assert.assertTrue(request.getNativeCashierInfoRequest().getBody().getDisablePaymentMode().get(0)
                    .getChannels().get(0).equals("UPI"));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void testDisableUPICollectIfMerchantNotVerified_merchantPrefDisabledAndtxnSmaller2000_shouldNotAddUPIInDisablePayModes() {

        String txnToken = "asdkjaskdlksajdlkjas";
        String mid = "ankitPay";
        Money txnAmt = new Money();
        txnAmt.setValue("2000");

        try {
            Method disableUPICollectIfMerchantNotVerified = NativePayviewConsultRequestProcessor.class
                    .getDeclaredMethod("disableUPICollectIfMerchantNotVerified", InitiateTransactionRequestBody.class,
                            NativeCashierInfoContainerRequest.class);
            disableUPICollectIfMerchantNotVerified.setAccessible(true);

            InitiateTransactionRequestBody orderDetail = Mockito.mock(InitiateTransactionRequestBody.class);
            Mockito.when(orderDetail.getMid()).thenReturn(mid);
            Mockito.when(orderDetail.getTxnAmount()).thenReturn(txnAmt);
            NativeCashierInfoContainerRequest request = Mockito.mock(NativeCashierInfoContainerRequest.class);
            NativeCashierInfoRequest nativeCashierInfoRequest = Mockito.mock(NativeCashierInfoRequest.class);
            NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
            nativeCashierInfoRequestBody.setDisablePaymentMode(new ArrayList<>());
            Mockito.when(nativeCashierInfoRequest.getBody()).thenReturn(nativeCashierInfoRequestBody);
            Mockito.when(request.getNativeCashierInfoRequest()).thenReturn(nativeCashierInfoRequest);

            Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(false);
            disableUPICollectIfMerchantNotVerified.invoke(nativePayviewConsultRequestProcessor, orderDetail, request);
            Assert.assertTrue(CollectionUtils.isEmpty(request.getNativeCashierInfoRequest().getBody()
                    .getDisablePaymentMode()));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void testDisableUPICollectIfMerchantVerified_merchantPrefEnabledAndtxnGreater2000_shouldNotAddUPIInDisablePayModes() {

        String txnToken = "asdkjaskdlksajdlkjas";
        String mid = "ankitPay";
        Money txnAmt = new Money();
        txnAmt.setValue("2004");

        try {
            Method disableUPICollectIfMerchantNotVerified = NativePayviewConsultRequestProcessor.class
                    .getDeclaredMethod("disableUPICollectIfMerchantNotVerified", InitiateTransactionRequestBody.class,
                            NativeCashierInfoContainerRequest.class);
            disableUPICollectIfMerchantNotVerified.setAccessible(true);

            InitiateTransactionRequestBody orderDetail = Mockito.mock(InitiateTransactionRequestBody.class);
            Mockito.when(orderDetail.getMid()).thenReturn(mid);
            Mockito.when(orderDetail.getTxnAmount()).thenReturn(txnAmt);
            NativeCashierInfoContainerRequest request = Mockito.mock(NativeCashierInfoContainerRequest.class);
            NativeCashierInfoRequest nativeCashierInfoRequest = Mockito.mock(NativeCashierInfoRequest.class);
            NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
            nativeCashierInfoRequestBody.setDisablePaymentMode(new ArrayList<>());
            Mockito.when(nativeCashierInfoRequest.getBody()).thenReturn(nativeCashierInfoRequestBody);
            Mockito.when(request.getNativeCashierInfoRequest()).thenReturn(nativeCashierInfoRequest);

            Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(true);
            disableUPICollectIfMerchantNotVerified.invoke(nativePayviewConsultRequestProcessor, orderDetail, request);
            Assert.assertTrue(CollectionUtils.isEmpty(request.getNativeCashierInfoRequest().getBody()
                    .getDisablePaymentMode()));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }

    }

}