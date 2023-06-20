package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.common.bankForm.model.DirectAPIResponse;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.bankForm.model.ResponseDetail;
import com.paytm.pgplus.facade.payment.services.IBankProxyService;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageRequest;
import com.paytm.pgplus.theia.nativ.model.directpage.NativeDirectBankPageServiceRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import org.intellij.lang.annotations.MagicConstant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class NativeDirectBankPageServiceImplTest {

    @InjectMocks
    NativeDirectBankPageServiceImpl nativeDirectBankPageService;

    @Mock
    NativeDirectBankPageRequest request;

    @Mock
    NativeDirectBankPageServiceRequest serviceRequest;

    @Mock
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Mock
    IBankProxyService bankProxyServiceImpl;

    @Mock
    HttpRequestPayload<String> payload;

    @Mock
    DirectAPIResponse directAPIResponse;

    @Mock
    ResponseDetail responseDetail;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxyResendOtpWhenDirectApiResponseNull() throws Exception {
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createResendOtpRequest(any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateResendOtpRequest(any())).thenReturn(null);
        nativeDirectBankPageService.callInstaProxyResendOtp(request, serviceRequest);
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxyResendOtp() throws Exception {
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createResendOtpRequest(any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateResendOtpRequest(any())).thenReturn(null);
        nativeDirectBankPageService.callInstaProxyResendOtp(request, serviceRequest);
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxyResendOtpWhenDirectApiResponseNotNull() throws Exception {
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createResendOtpRequest(any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateResendOtpRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("test");
        nativeDirectBankPageService.callInstaProxyResendOtp(request, serviceRequest);
    }

    @Test
    public void testCallInstaProxyResendOtpWhenReturnNativeDirectBankPageServiceResponse() throws Exception {
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createResendOtpRequest(any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateResendOtpRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        Assert.assertNotNull(nativeDirectBankPageService.callInstaProxyResendOtp(request, serviceRequest));
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxySubmitWhenThrowsException() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createSubmitRequest(any(), any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateSubmitRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        when(nativeDirectBankPageHelper.isApiRequestOriginPG(any())).thenReturn(true);
        when(directAPIResponse.getExtendInfo()).thenReturn(testMap);
        nativeDirectBankPageService.callInstaProxySubmit(request, serviceRequest);
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxySubmitWhenThrowsNativeFlowException() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("test", "");
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createSubmitRequest(any(), any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateSubmitRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        when(nativeDirectBankPageHelper.isApiRequestOriginPG(any())).thenReturn(true);
        when(directAPIResponse.getExtendInfo()).thenReturn(testMap);
        nativeDirectBankPageService.callInstaProxySubmit(request, serviceRequest);
    }

    @Test(expected = NativeFlowException.class)
    public void testCallInstaProxySubmitThrowsException() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put(CASHIER_REQUEST_ID, "test");
        testMap.put(TRANS_ID, "test");
        testMap.put(MERCHANT_ID, null);
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createSubmitRequest(any(), any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateSubmitRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        when(nativeDirectBankPageHelper.isApiRequestOriginPG(any())).thenReturn(true);
        when(directAPIResponse.getExtendInfo()).thenReturn(testMap);
        nativeDirectBankPageService.callInstaProxySubmit(request, serviceRequest);
    }

    @Test
    public void testCallInstaProxySubmit() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put(CASHIER_REQUEST_ID, "test");
        testMap.put(TRANS_ID, "test");
        testMap.put(MERCHANT_ID, "test");
        testMap.put(PAYMENT_MODE, "test");
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createSubmitRequest(any(), any(), any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateSubmitRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        when(nativeDirectBankPageHelper.isApiRequestOriginPG(any())).thenReturn(true);
        when(directAPIResponse.getExtendInfo()).thenReturn(testMap);
        Assert.assertNotNull(nativeDirectBankPageService.callInstaProxySubmit(request, serviceRequest));
    }

    @Test
    public void testcallInstaProxyCancel() throws Exception {
        when(nativeDirectBankPageHelper.getContentForInstaProxyDirectApis(any(), any())).thenReturn(new FormDetail());
        when(nativeDirectBankPageHelper.createCancelRequest(any())).thenReturn(payload);
        when(bankProxyServiceImpl.initiateCancelRequest(any())).thenReturn(directAPIResponse);
        when(directAPIResponse.getResultInfo()).thenReturn(responseDetail);
        when(responseDetail.getResultStatus()).thenReturn("ACCEPTED_SUCCESS");
        Assert.assertNotNull(nativeDirectBankPageService.callInstaProxyCancel(serviceRequest));
    }

}