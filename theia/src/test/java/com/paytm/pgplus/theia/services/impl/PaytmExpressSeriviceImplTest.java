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
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.*;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.testng.Assert;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class PaytmExpressSeriviceImplTest {

    @InjectMocks
    PaytmExpressSeriviceImpl paytmExpressSerivice;

    @Mock
    PaymentRequestBean requestData;

    @Mock
    Model model;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    MerchantResponseService merchantResponseService;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    IBizService bizService;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    MerchantPreferenceProvider merchantPreferenceProvider;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Mock
    UpiInfoSessionUtil upiInfoSessionUtil;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    TransactionCacheUtils transactionCacheUtils;

    @Mock
    QueryPaymentStatus queryPaymentStatus;

    @Mock
    ValidationService validationService;

    @Mock
    ChecksumService checksumService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(requestData.getOrderId()).thenReturn("test");
        when(merchantResponseService.processMerchantFailResponse(requestData, ResponseConstants.SUCCESS)).thenReturn(
                "test");
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        Assert.assertNotNull(paytmExpressSerivice.processPaymentRequest(requestData, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(null);
        paytmExpressSerivice.processPaymentRequest(requestData, model);
    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessesdFalse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.INVALID_CHECKSUM);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(
                merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE)).thenReturn("test");
        Assert.assertNotNull(paytmExpressSerivice.processPaymentRequest(requestData, model));
    }

    @Test
    public void testProcessPaymentRequestWhenRiskRejectUserMsgNotBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.INVALID_CHECKSUM);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(
                merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE)).thenReturn("test");
        Assert.assertNotNull(paytmExpressSerivice.processPaymentRequest(requestData, model));
    }

    @Test
    public void testProcessPaymentRequestIsPostConvenienceFeesEnabledFalse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)).thenReturn(false);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.INVALID_CHECKSUM);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(
                merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE)).thenReturn("test");
        Assert.assertNotNull(paytmExpressSerivice.processPaymentRequest(requestData, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMsgBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.INVALID_CHECKSUM);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("");
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(
                merchantResponseService.processMerchantFailResponse(requestData,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE)).thenReturn("test");
        paytmExpressSerivice.processPaymentRequest(requestData, model);
    }

    @Test
    public void testProcessPaymentRequestWhenWorkFlowResponseBeanNotNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)).thenReturn(true);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.getInternalErrorCode()).thenReturn("test");
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.INVALID_CHECKSUM);
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(workFlowResponseBean.getQueryPaymentStatus()).thenReturn(queryPaymentStatus);
        when(queryPaymentStatus.getPaymentStatusValue()).thenReturn("test");
        when(theiaResponseGenerator.generateResponseForPaytmExpress(any(), any())).thenReturn("test");
        Assert.assertNotNull(paytmExpressSerivice.processPaymentRequest(requestData, model));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnValidationSuccess() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(true);
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                paytmExpressSerivice.validatePaymentRequest(requestData));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(true);
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                paytmExpressSerivice.validatePaymentRequest(requestData));
    }

    @Test
    public void testvalidatePaymentRequestWhenReturnInvalidRequest() {
        when(validationService.validatePaytmExpressData(any())).thenReturn(false);
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.INVALID_REQUEST, paytmExpressSerivice.validatePaymentRequest(requestData));
    }

}