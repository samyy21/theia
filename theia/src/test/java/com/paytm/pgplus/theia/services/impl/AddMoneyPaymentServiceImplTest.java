package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
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

public class AddMoneyPaymentServiceImplTest {

    @InjectMocks
    AddMoneyPaymentServiceImpl addMoneyPaymentService;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    PaymentRequestBean paymentRequestBean;

    @Mock
    MerchantResponseService merchantResponseService;

    @Mock
    Model model;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    IBizService bizService;

    @Mock
    ChecksumService checksumService;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    RetryServiceHelper retryServiceHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPaymentRequestThrowsTheiaDataMappingException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        Assert.assertNotNull(addMoneyPaymentService.processPaymentRequest(paymentRequestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestThrowsTheiaServiceException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        addMoneyPaymentService.processPaymentRequest(paymentRequestBean, model);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.PWP_PROMO_NOT_SUPPORTED);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        Assert.assertNotNull(addMoneyPaymentService.processPaymentRequest(paymentRequestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsTheiaServiceException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponse()).thenReturn(null);

        Assert.assertNotNull(addMoneyPaymentService.processPaymentRequest(paymentRequestBean, model));
    }

    @Test
    public void testProcessPaymentRequestWhenReturnPageDetailResponse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantResponseService.processMerchantFailResponse(any(), any(), anyString())).thenReturn("test");
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);

        Assert.assertNotNull(addMoneyPaymentService.processPaymentRequest(paymentRequestBean, model));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                addMoneyPaymentService.validatePaymentRequest(paymentRequestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                addMoneyPaymentService.validatePaymentRequest(paymentRequestBean));
    }

}