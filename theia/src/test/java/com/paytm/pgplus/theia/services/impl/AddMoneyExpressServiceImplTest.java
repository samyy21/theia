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
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.AddMoneyToGvConsentUtil;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import wiremock.org.xmlunit.validation.ValidationResult;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class AddMoneyExpressServiceImplTest {
    @InjectMocks
    AddMoneyExpressServiceImpl moneyExpressService;

    @Mock
    PaymentRequestBean paymentRequestBean;

    @Mock
    Model modelResponse;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    IBizService bizService;;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean;

    @Mock
    WorkFlowResponseBean flowResponseBean;

    @Mock
    AddMoneyToGvConsentUtil addMoneyToGvConsentUtil;

    @Mock
    PageDetailsResponse pageDetailsResponse;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

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
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.GV_CONSENT_REQUIRED);
        when(addMoneyToGvConsentUtil.showConsentPageForRedirection(anyString(), anyString(), anyBoolean())).thenReturn(
                pageDetailsResponse);
        doNothing().when(theiaSessionDataService).setRedirectPageInSession(any(), anyString());
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessedTrue() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.MERCHANT_RISK_REJECT);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessedFalse() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.KYC_VALIDATION_REQUIRED);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNull() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.RISK_REJECT);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        when(workFlowResponseBean.getResponse()).thenReturn(null);
        when(workFlowResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testProcessPaymentRequestWhenReturnPageDetailRespose() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.UNKNOWN_CLIENT);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        when(workFlowResponseBean.getResponse()).thenReturn(null);
        when(workFlowResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMsgBlank() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.UNKNOWN_CLIENT);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        when(workFlowResponseBean.getResponse()).thenReturn(null);
        when(workFlowResponseBean.getRiskRejectUserMessage()).thenReturn("");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse);

    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNotNull() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getResponseConstant()).thenReturn(ResponseConstants.RISK_REJECT);
        when(workFlowResponseBean.getInternalErrorCode()).thenReturn("test");
        when(workFlowResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        when(workFlowResponseBean.getResponse()).thenReturn(flowResponseBean);
        when(workFlowResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);
        when(theiaResponseGenerator.generateResponseForSeamless(any(), any())).thenReturn("test");
        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testProcessPaymentRequestWhenThrowsTheiaDataServiceMappingException() throws TheiaDataMappingException {
        when(paymentRequestBean.getMid()).thenReturn("test");
        when(paymentRequestBean.getOrderId()).thenReturn("test");
        when(paymentRequestBean.getRequestType()).thenReturn("test");

        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(bizService.processWorkFlow(any(), any())).thenReturn(workFlowResponseBean);

        when(theiaResponseGenerator.getPageDetailsResponse(any(), any())).thenReturn(pageDetailsResponse);

        Assert.assertNotNull(moneyExpressService.processPaymentRequest(paymentRequestBean, modelResponse));

    }

    @Test
    public void testValidatePaymentRequest() {
        when(paymentRequestBean.isGvConsentFlow()).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                moneyExpressService.validatePaymentRequest(paymentRequestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(paymentRequestBean.isGvConsentFlow()).thenReturn(false);
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                moneyExpressService.validatePaymentRequest(paymentRequestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(paymentRequestBean.isGvConsentFlow()).thenReturn(false);
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                moneyExpressService.validatePaymentRequest(paymentRequestBean));
    }

}