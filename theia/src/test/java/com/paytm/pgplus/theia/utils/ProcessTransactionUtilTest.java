package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.impl.TheiaSessionRedisUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.SSO_TOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.TXN_AMOUNT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.*;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ProcessTransactionUtilTest {

    @InjectMocks
    private ProcessTransactionUtil processTransactionUtil;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Mock
    TheiaSessionRedisUtil theiaSessionRedisUtil;

    @Test
    public void testGetAdditionalParamMap_merchantNotVerifiedTxnGreater2000_shouldFail() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String mid = "ankmid";
        String txnToken = "asjdaskjhdakjsdjaN";
        String orderId = "ankOrder";

        Mockito.when(request.getParameter(SSO_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TXN_AMOUNT)).thenReturn("2001");
        Mockito.when(request.getParameter(MID)).thenReturn(mid);
        Mockito.when(request.getParameter(ORDER_ID)).thenReturn(orderId);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE)).thenReturn("UPI");

        InitiateTransactionRequestBody initiateTransactionRequestBody = Mockito
                .mock(InitiateTransactionRequestBody.class);
        Money txnAmt = new Money();
        txnAmt.setValue("2001");
        Mockito.when(initiateTransactionRequestBody.getTxnAmount()).thenReturn(txnAmt);
        Mockito.when(initiateTransactionRequestBody.getEmiSubventionValidationResponse()).thenReturn(null);
        Mockito.when(initiateTransactionRequestBody.getMid()).thenReturn(mid);
        Mockito.when(initiateTransactionRequestBody.getOrderId()).thenReturn(orderId);

        InitiateTransactionRequest initiateTransactionRequest = Mockito.mock(InitiateTransactionRequest.class);
        Mockito.when(initiateTransactionRequest.getBody()).thenReturn(initiateTransactionRequestBody);
        NativeCashierInfoResponseBody nativeCashierInfoResponseBody = Mockito.mock(NativeCashierInfoResponseBody.class);
        Mockito.when(nativeCashierInfoResponseBody.isOnTheFlyKYCRequired()).thenReturn(false);

        NativeInitiateRequest nativeInitiateRequest = Mockito.mock(NativeInitiateRequest.class);
        Mockito.when(nativeInitiateRequest.getInitiateTxnReq()).thenReturn(initiateTransactionRequest);
        NativeCashierInfoResponse nativeCashierInfoResponse = Mockito.mock(NativeCashierInfoResponse.class);
        Mockito.when(nativeCashierInfoResponseBody.getPaymentFlow()).thenReturn(EPayMode.NONE);

        Mockito.when(nativeCashierInfoResponse.getBody()).thenReturn(nativeCashierInfoResponseBody);

        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "orderDetail")).thenReturn(nativeInitiateRequest);
        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "cashierInfo")).thenReturn(nativeCashierInfoResponse);
        Mockito.when(nativeSessionUtil.validate(any())).thenReturn(nativeInitiateRequest);

        Mockito.when(nativeSessionUtil.getCashierInfoResponse(txnToken)).thenReturn(nativeCashierInfoResponse);

        Mockito.when(request.getParameter(CHANNEL_CODE)).thenReturn("UPI");
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.MPIN)).thenReturn(null);
        Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(false);

        try {
            processTransactionUtil.getAdditionalParamMap(request, response);

        } catch (NativeFlowException e) {
            return;
        } catch (Exception e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testGetAdditionalParamMap_merchantVerifiedTxnGreater2000_shouldNotFail() {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String mid = "ankmid";
        String txnToken = "asjdaskjhdakjsdjaN";
        String orderId = "ankOrder";

        Mockito.when(request.getParameter(SSO_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TXN_AMOUNT)).thenReturn("2001");
        Mockito.when(request.getParameter(MID)).thenReturn(mid);
        Mockito.when(request.getParameter(ORDER_ID)).thenReturn(orderId);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE)).thenReturn("UPI");

        InitiateTransactionRequestBody initiateTransactionRequestBody = Mockito
                .mock(InitiateTransactionRequestBody.class);
        Money txnAmt = new Money();
        txnAmt.setValue("2001");
        Mockito.when(initiateTransactionRequestBody.getTxnAmount()).thenReturn(txnAmt);
        Mockito.when(initiateTransactionRequestBody.getEmiSubventionValidationResponse()).thenReturn(null);
        Mockito.when(initiateTransactionRequestBody.getMid()).thenReturn(mid);
        Mockito.when(initiateTransactionRequestBody.getOrderId()).thenReturn(orderId);

        InitiateTransactionRequest initiateTransactionRequest = Mockito.mock(InitiateTransactionRequest.class);
        Mockito.when(initiateTransactionRequest.getBody()).thenReturn(initiateTransactionRequestBody);
        NativeCashierInfoResponseBody nativeCashierInfoResponseBody = Mockito.mock(NativeCashierInfoResponseBody.class);
        Mockito.when(nativeCashierInfoResponseBody.isOnTheFlyKYCRequired()).thenReturn(false);

        NativeInitiateRequest nativeInitiateRequest = Mockito.mock(NativeInitiateRequest.class);
        Mockito.when(nativeInitiateRequest.getInitiateTxnReq()).thenReturn(initiateTransactionRequest);
        NativeCashierInfoResponse nativeCashierInfoResponse = Mockito.mock(NativeCashierInfoResponse.class);
        Mockito.when(nativeCashierInfoResponseBody.getPaymentFlow()).thenReturn(EPayMode.NONE);

        Mockito.when(nativeCashierInfoResponse.getBody()).thenReturn(nativeCashierInfoResponseBody);

        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "orderDetail")).thenReturn(nativeInitiateRequest);
        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "cashierInfo")).thenReturn(nativeCashierInfoResponse);
        Mockito.when(nativeSessionUtil.validate(any())).thenReturn(nativeInitiateRequest);

        Mockito.when(nativeSessionUtil.getCashierInfoResponse(txnToken)).thenReturn(nativeCashierInfoResponse);

        Mockito.when(request.getParameter(CHANNEL_CODE)).thenReturn("UPI");
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.MPIN)).thenReturn(null);
        Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(true);

        try {
            processTransactionUtil.getAdditionalParamMap(request, response);

        } catch (NativeFlowException e) {
            Assert.fail();
            return;
        } catch (Exception e) {
            return;
        }

    }

    @Test
    public void testGetAdditionalParamMap_merchantVerifiedTxnSmaller2000_shouldNotFail() {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String mid = "ankmid";
        String txnToken = "asjdaskjhdakjsdjaN";
        String orderId = "ankOrder";

        Mockito.when(request.getParameter(SSO_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.TXN_TOKEN)).thenReturn(txnToken);
        Mockito.when(request.getParameter(TXN_AMOUNT)).thenReturn("2001");
        Mockito.when(request.getParameter(MID)).thenReturn(mid);
        Mockito.when(request.getParameter(ORDER_ID)).thenReturn(orderId);
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE)).thenReturn("UPI");

        InitiateTransactionRequestBody initiateTransactionRequestBody = Mockito
                .mock(InitiateTransactionRequestBody.class);
        Money txnAmt = new Money();
        txnAmt.setValue("2000");
        Mockito.when(initiateTransactionRequestBody.getTxnAmount()).thenReturn(txnAmt);
        Mockito.when(initiateTransactionRequestBody.getEmiSubventionValidationResponse()).thenReturn(null);
        Mockito.when(initiateTransactionRequestBody.getMid()).thenReturn(mid);
        Mockito.when(initiateTransactionRequestBody.getOrderId()).thenReturn(orderId);

        InitiateTransactionRequest initiateTransactionRequest = Mockito.mock(InitiateTransactionRequest.class);
        Mockito.when(initiateTransactionRequest.getBody()).thenReturn(initiateTransactionRequestBody);
        NativeCashierInfoResponseBody nativeCashierInfoResponseBody = Mockito.mock(NativeCashierInfoResponseBody.class);
        Mockito.when(nativeCashierInfoResponseBody.isOnTheFlyKYCRequired()).thenReturn(false);

        NativeInitiateRequest nativeInitiateRequest = Mockito.mock(NativeInitiateRequest.class);
        Mockito.when(nativeInitiateRequest.getInitiateTxnReq()).thenReturn(initiateTransactionRequest);
        NativeCashierInfoResponse nativeCashierInfoResponse = Mockito.mock(NativeCashierInfoResponse.class);
        Mockito.when(nativeCashierInfoResponseBody.getPaymentFlow()).thenReturn(EPayMode.NONE);

        Mockito.when(nativeCashierInfoResponse.getBody()).thenReturn(nativeCashierInfoResponseBody);

        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "orderDetail")).thenReturn(nativeInitiateRequest);
        Mockito.when(theiaSessionRedisUtil.hget(txnToken, "cashierInfo")).thenReturn(nativeCashierInfoResponse);
        Mockito.when(nativeSessionUtil.validate(any())).thenReturn(nativeInitiateRequest);

        Mockito.when(nativeSessionUtil.getCashierInfoResponse(txnToken)).thenReturn(nativeCashierInfoResponse);

        Mockito.when(request.getParameter(CHANNEL_CODE)).thenReturn("UPI");
        Mockito.when(request.getParameter(TheiaConstant.RequestParams.Native.MPIN)).thenReturn(null);
        Mockito.when(merchantPreferenceService.isUpiCollectWhitelisted(mid, true)).thenReturn(false);

        try {
            processTransactionUtil.getAdditionalParamMap(request, response);

        } catch (NativeFlowException e) {
            Assert.fail();
            return;
        } catch (Exception e) {
            return;
        }

    }
}