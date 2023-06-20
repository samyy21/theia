package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.model.common.DccPaymentDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DccMockHelperTest extends AOAUtilsTest {

    @InjectMocks
    DccMockHelper dccMockHelper;

    @Mock
    IRequestProcessor<DccPaymentDetailRequest, DccPaymentDetail> dccFetchRatesRequestProcessor;

    @Mock
    RequestProcessorFactory requestProcessorFactory;

    @Mock
    DccPageDataHelper dccPageDataHelper;

    @Mock
    MerchantInfoService merchantInfoService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    NativeSessionUtil nativeSessionUtil;

    @Test
    public void testSetDccPaymentDetailsResponse() {
        Assert.assertNotNull(dccMockHelper.setDccPaymentDetailResponse());
    }

    @Test
    public void testBuildDccData1() throws Exception {
        when(requestProcessorFactory.getRequestProcessor(any())).thenReturn(dccFetchRatesRequestProcessor);
        when(dccFetchRatesRequestProcessor.process(any())).thenReturn(new DccPaymentDetail());
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        paymentRequestBean.setTxnAmount("100");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        flowRequestBean.setCardNo("1234567890");
        paymentRequestBean.setPaymentTypeId("test");
        dccMockHelper.buildDccData1(paymentRequestBean, flowRequestBean);
        verify(dccPageDataHelper, atMost(1)).setDCCPageDataToCache(any(), any(), any());
    }

    @Test
    public void testSetDccPageData() {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantDisplayName("test");
        merchantBussinessLogoInfo.setMerchantImageName("test");
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);
        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);

        dccMockHelper.setDccPageData("test", "test", "test");
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDccPageDataWhenMerchantDetailResponseNull() {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantDisplayName("test");

        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);
        when(merchantInfoService.getMerchantInfo(any())).thenReturn(null);

        dccMockHelper.setDccPageData("test", "test", "test");
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDccPageDataWhenMerchantBusinessNameBlank() {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("");
        merchantBussinessLogoInfo.setMerchantDisplayName("test");
        merchantBussinessLogoInfo.setMerchantImageName("test");
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);
        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);

        dccMockHelper.setDccPageData("test", "test", "test");
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testSetDccPageDataWhenMerchantImageNameBlank() {
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setMerchantBusinessName("test");
        merchantBussinessLogoInfo.setMerchantDisplayName("test");
        merchantBussinessLogoInfo.setMerchantImageName("");
        when(merchantExtendInfoUtils.isMerchantOnPaytm(anyString())).thenReturn(true);
        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantBussinessLogoInfo);

        dccMockHelper.setDccPageData("test", "test", "test");
        verify(nativeSessionUtil, atMost(1)).setDccPageData(anyString(), any());

    }

    @Test
    public void testBuildDccData1WhenThrowException() throws Exception {
        when(requestProcessorFactory.getRequestProcessor(any())).thenReturn(dccFetchRatesRequestProcessor);

        doThrow(Exception.class).when(dccFetchRatesRequestProcessor).process(any());
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        paymentRequestBean.setTxnAmount("100");
        paymentRequestBean.setMid("test");
        paymentRequestBean.setOrderId("test");
        flowRequestBean.setCardNo("1234567890");
        paymentRequestBean.setPaymentTypeId("test");
        dccMockHelper.buildDccData1(paymentRequestBean, flowRequestBean);
        verify(dccPageDataHelper, atMost(1)).setDCCPageDataToCache(any(), any(), any());
    }

}