package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.ExtendedInfoRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

public class ErrorPaymentServiceImplTest {

    @InjectMocks
    ErrorPaymentServiceImpl errorPaymentService;

    @Mock
    PaymentRequestBean requestData;

    @Mock
    Model model;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    ExtendedInfoRequestBean infoRequestBean;

    @Mock
    ResponseCodeUtil responseCodeUtil;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenThrow(TheiaDataMappingException.class);
        errorPaymentService.processPaymentRequest(requestData, model);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(null);
        errorPaymentService.processPaymentRequest(requestData, model);
    }

    @Test
    public void testProcessPaymentRequestWhenReturnPageDetailResponse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(workFlowRequestBean);
        when(requestData.getCustId()).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(requestData.getMid()).thenReturn("test");
        when(requestData.getValidationError()).thenReturn("UNKNOWN_VALIDATION_FAILURE");
        when(workFlowRequestBean.getExtendInfo()).thenReturn(infoRequestBean);
        when(infoRequestBean.getCallBackURL()).thenReturn("test");
        when(theiaResponseGenerator.getFinalHtmlResponse(any())).thenReturn("test");
        Assert.assertNotNull(errorPaymentService.processPaymentRequest(requestData, model));
    }

    @Test
    public void testProcessPaymentRequestReturnPageDetailResponse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(workFlowRequestBean);
        when(requestData.getCustId()).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(requestData.getMid()).thenReturn("test");
        when(requestData.getValidationError()).thenReturn("test");
        when(workFlowRequestBean.getExtendInfo()).thenReturn(infoRequestBean);
        when(infoRequestBean.getCallBackURL()).thenReturn("test");
        when(theiaResponseGenerator.getFinalHtmlResponse(any())).thenReturn("test");
        Assert.assertNotNull(errorPaymentService.processPaymentRequest(requestData, model));
    }

    @Test
    public void testValidatePaymentRequest() {
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                errorPaymentService.validatePaymentRequest(requestData));
    }
}