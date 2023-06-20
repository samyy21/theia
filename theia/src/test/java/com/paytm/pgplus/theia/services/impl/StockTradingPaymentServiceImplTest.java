package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.ServiceHelper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class StockTradingPaymentServiceImplTest {

    @InjectMocks
    StockTradingPaymentServiceImpl stockTradingPaymentService;

    @Mock
    IBizService bizService;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    ChecksumService checksumService;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    Model model;

    @Mock
    ServiceHelper serviceHelper;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    TheiaResponseGenerator theiaResponseGenerator;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    TransactionCacheUtils transactionCacheUtils;

    @Mock
    HttpServletRequest httpServletRequest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(null);
        stockTradingPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenBizRequestResponseMapperThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(serviceHelper.returnFailureResponseToMerchant(any(), any())).thenReturn(new PageDetailsResponse());
        stockTradingPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(serviceHelper.processBizWorkFlow(any(), any(), any())).thenReturn(bizResponseBean);
        when(serviceHelper.checkIfBizResponseResponseFailed(any())).thenReturn(true);
        when(serviceHelper.returnFailureResponseToMerchant(any(), any())).thenReturn(new PageDetailsResponse());
        Assert.assertNotNull(stockTradingPaymentService.processPaymentRequest(requestBean, model));
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMsgNotBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(serviceHelper.processBizWorkFlow(any(), any(), any())).thenReturn(bizResponseBean);
        when(serviceHelper.checkIfBizResponseResponseFailed(any())).thenReturn(false);
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("test");
        when(serviceHelper.returnFailureResponseToMerchant(any(), any())).thenReturn(new PageDetailsResponse());
        stockTradingPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test(expected = TheiaServiceException.class)
    public void testProcessPaymentRequestWhenRiskRejectUserMsgBlank() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(serviceHelper.processBizWorkFlow(any(), any(), any())).thenReturn(bizResponseBean);
        when(serviceHelper.checkIfBizResponseResponseFailed(any())).thenReturn(false);
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(bizResponseBean.getRiskRejectUserMessage()).thenReturn("");
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(serviceHelper.returnFailureResponseToMerchant(any(), any())).thenReturn(new PageDetailsResponse());
        stockTradingPaymentService.processPaymentRequest(requestBean, model);
    }

    @Test
    public void testProcessPaymentRequestWhenReturnNotNull() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(serviceHelper.processBizWorkFlow(any(), any(), any())).thenReturn(bizResponseBean);
        when(serviceHelper.checkIfBizResponseResponseFailed(any())).thenReturn(false);
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(theiaResponseGenerator.generateResponseForSeamless(any(), any())).thenReturn("test");
        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        when(workFlowResponseBean.getTransID()).thenReturn("test");
        when(workFlowRequestBean.getPaytmMID()).thenReturn("test");
        when(workFlowRequestBean.getOrderID()).thenReturn("test");
        Assert.assertNotNull(stockTradingPaymentService.processPaymentRequest(requestBean, model));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                stockTradingPaymentService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                stockTradingPaymentService.validatePaymentRequest(requestBean));
    }

}