package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TopupExpressServiceImplTest {

    @InjectMocks
    TopupExpressServiceImpl topupExpressService;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    Model model;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    IBizService bizService;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    QueryPaymentStatus queryPaymentStatus;

    @Mock
    ValidationService validationService;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    UpiInfoSessionUtil upiInfoSessionUtil;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    TransactionCacheUtils transactionCacheUtils;

    @Mock
    ChecksumService checksumService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(topupExpressService.processPaymentRequest(requestBean, model));
    }

    @Test
    public void testProcessPaymentRequesWhenBizRequestResponseMapperThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(topupExpressService.processPaymentRequest(requestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsTheiaServiceException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        topupExpressService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessesTrue() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(topupExpressService.processPaymentRequest(requestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMessageBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("");

        topupExpressService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNotNull() throws TheiaDataMappingException {

        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(workFlowRequestBean.getPaymentTypeId()).thenReturn("UPI");
        when(workFlowResponseBean.getQueryPaymentStatus()).thenReturn(queryPaymentStatus);
        when(queryPaymentStatus.getPaymentStatusValue()).thenReturn("test");
        when(theiaResponseGenerator.generateResponseForPaytmExpress(any(), any())).thenReturn("test");
        when(workFlowResponseBean.getTransID()).thenReturn("test");
        when(workFlowRequestBean.getPaytmMID()).thenReturn("test");
        when(workFlowRequestBean.getOrderID()).thenReturn("test");

        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        Assert.assertNotNull(topupExpressService.processPaymentRequest(requestBean, model));
    }

    @Test
    public void testProcessPaymentRequestWorkFlowResponseBeanNotNull() throws TheiaDataMappingException {

        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.SUCCESS_RESPONSE_CODE);
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(workFlowRequestBean.getPaymentTypeId()).thenReturn("UPI");
        when(workFlowResponseBean.getQueryPaymentStatus()).thenReturn(queryPaymentStatus);
        when(queryPaymentStatus.getPaymentStatusValue()).thenReturn("FAIL");
        when(theiaResponseGenerator.generateResponseForPaytmExpress(any(), any())).thenReturn("test");
        when(workFlowResponseBean.getTransID()).thenReturn("test");
        when(workFlowRequestBean.getPaytmMID()).thenReturn("test");
        when(workFlowRequestBean.getOrderID()).thenReturn("test");

        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        Assert.assertNotNull(topupExpressService.processPaymentRequest(requestBean, model));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnValidationSuccess() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(true);
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                topupExpressService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(true);
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                topupExpressService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnInvalidRequest() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(false);
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.INVALID_REQUEST, topupExpressService.validatePaymentRequest(requestBean));
    }
}