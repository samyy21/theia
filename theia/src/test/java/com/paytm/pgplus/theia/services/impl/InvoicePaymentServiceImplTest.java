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
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class InvoicePaymentServiceImplTest {

    @InjectMocks
    InvoicePaymentServiceImpl invoicePaymentService;

    @Mock
    PaymentRequestBean paymentRequestBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    Model model;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    IBizService bizService;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Mock
    ChecksumService checksumService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        invoicePaymentService.processPaymentRequest(paymentRequestBean, model);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenWorkFlowRequestBeanNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        invoicePaymentService.processPaymentRequest(paymentRequestBean, model);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(null);
        invoicePaymentService.processPaymentRequest(paymentRequestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNotNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        Assert.assertNotNull(invoicePaymentService.processPaymentRequest(paymentRequestBean, model));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                invoicePaymentService.validatePaymentRequest(paymentRequestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                invoicePaymentService.validatePaymentRequest(paymentRequestBean));
    }

}