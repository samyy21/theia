package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CreateInvoicePaymentServiceImplTest {

    @InjectMocks
    CreateInvoicePaymentServiceImpl invoicePaymentService;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> flowResponseBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    IBizService bizService;

    @Mock
    ChecksumService checksumService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);

        invoicePaymentService.processPaymentRequest(requestBean);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        invoicePaymentService.processPaymentRequest(requestBean);
    }

    @Test
    public void testProcessPaymentRequestWhenReturnWorkFlowRequestBean() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(flowResponseBean);
        Assert.assertNull(invoicePaymentService.processPaymentRequest(requestBean));
    }

    @Test(expected = TheiaServiceException.class)
    public void testGetResponseWithChecksumForJsonResponse() {
        invoicePaymentService.getResponseWithChecksumForJsonResponse("test", "test");
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                invoicePaymentService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidateFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                invoicePaymentService.validatePaymentRequest(requestBean));
    }
}