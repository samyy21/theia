package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SeamlessNBPaymentServiceImplTest {

    @InjectMocks
    SeamlessNBPaymentServiceImpl seamlessNBPaymentService;

    @Mock
    ChecksumService checksumService;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Mock
    Model model;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    IBizService bizService;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    TransactionCacheUtils transactionCacheUtils;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                seamlessNBPaymentService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                seamlessNBPaymentService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(seamlessNBPaymentService.processPaymentRequest(requestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenWorkFLowReqBeanNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        seamlessNBPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenWorkFLowReqBeanNotNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled(anyString())).thenReturn(true);
        when(workFlowRequestBean.isNativeAddMoney()).thenReturn(false);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(seamlessNBPaymentService.processPaymentRequest(requestBean, model));
    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessedTrue() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled(anyString())).thenReturn(true);
        when(workFlowRequestBean.isNativeAddMoney()).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(seamlessNBPaymentService.processPaymentRequest(requestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMsgBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled(anyString())).thenReturn(true);
        when(workFlowRequestBean.isNativeAddMoney()).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        seamlessNBPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNotNll() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceService.isSlabBasedMDREnabled(anyString())).thenReturn(true);
        when(merchantPreferenceService.isDynamicFeeMerchant(anyString())).thenReturn(true);
        when(merchantPreferenceService.isPostConvenienceFeesEnabled(anyString())).thenReturn(true);
        when(workFlowRequestBean.isNativeAddMoney()).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED);
        when(theiaResponseGenerator.generateResponseForSeamless(any(), any())).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(requestBean.getRequest()).thenReturn(null);
        when(workFlowResponseBean.getTransID()).thenReturn("test");
        when(workFlowRequestBean.getPaytmMID()).thenReturn("test");
        when(workFlowRequestBean.getOrderID()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(seamlessNBPaymentService.processPaymentRequest(requestBean, model));
    }

}