package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DccPageDataHelperTest extends AOAUtilsTest {

    @InjectMocks
    DccPageDataHelper dccPageDataHelper;

    @Mock
    MerchantInfoService merchantInfoService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Mock
    MerchantResponseService merchantResponseService;

    @Test
    public void testSetDCCPageDataToCache() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantImageName("test");

        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);

        paymentRequestBean.setWorkflow("enhancedCashierFlow");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnToken("test");
        paymentRequestBean.setTxnAmount("100");

        dccPageDataHelper.setDCCPageDataToCache(paymentRequestBean, workFlowRequestBean, dccPaymentDetail);
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDCCPageDataToCacheWhenWorkFlowDefault() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantImageName("test");

        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
        InitiateTransactionRequestBody initiateTransactionRequestBody = new InitiateTransactionRequestBody();
        initiateTransactionRequest.setBody(initiateTransactionRequestBody);
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);

        TransactionResponse txnResponse = new TransactionResponse();
        txnResponse.setCallbackUrl("test");

        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);
        when(nativeSessionUtil.getNativeInitiateRequest(anyString())).thenReturn(nativeInitiateRequest);
        when(merchantResponseService.createNativeRequestForMerchant(any(), any(), any(), anyBoolean(), anyString()))
                .thenReturn(txnResponse);
        paymentRequestBean.setWorkflow("test");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnToken("test");
        paymentRequestBean.setTxnAmount("100");

        dccPageDataHelper.setDCCPageDataToCache(paymentRequestBean, workFlowRequestBean, dccPaymentDetail);
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDCCPageDataToCacheWhenWorkFlowCheckout() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantImageName("test");

        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);

        paymentRequestBean.setWorkflow("checkout");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnToken("test");
        paymentRequestBean.setTxnAmount("100");

        dccPageDataHelper.setDCCPageDataToCache(paymentRequestBean, workFlowRequestBean, dccPaymentDetail);
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDCCPageDataToCacheWhenMerchantImageNameBlank() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("");
        merchantBussinessLogoInfo.setMerchantImageName("");

        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);

        paymentRequestBean.setWorkflow("checkout");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnToken("test");
        paymentRequestBean.setTxnAmount("100");

        dccPageDataHelper.setDCCPageDataToCache(paymentRequestBean, workFlowRequestBean, dccPaymentDetail);
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDCCPageDataToCacheWhenMerchantBuisnessLogoInfoNull() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean workFlowRequestBean = new WorkFlowRequestBean();
        DccPaymentDetail dccPaymentDetail = new DccPaymentDetail();

        when(merchantInfoService.getMerchantInfo(any())).thenReturn(null);
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);

        paymentRequestBean.setWorkflow("checkout");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        paymentRequestBean.setTxnToken("test");
        paymentRequestBean.setTxnAmount("100");

        dccPageDataHelper.setDCCPageDataToCache(paymentRequestBean, workFlowRequestBean, dccPaymentDetail);
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

}