package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.theia.acs.service.IAcsUrlService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import mockit.MockUp;
import org.intellij.lang.annotations.MagicConstant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class SeamlessACSPaymentServiceImplTest {

    @InjectMocks
    SeamlessACSPaymentServiceImpl seamlessACSPaymentService;

    @Mock
    PaymentRequestBean requestData;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    IBizService bizService;

    @Mock
    ResponseCodeUtil responseCodeUtil;

    @Mock
    ResponseCodeDetails responseCodeDetails;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpSession httpSession;

    @Mock
    IAcsUrlService acsUrlService;

    @Mock
    TransactionCacheUtils transactionCacheUtils;

    @Mock
    QueryPaymentStatus queryPaymentStatus;

    @Mock
    ChecksumService checksumService;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessedFalse() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.FGW_INVALID_VPA);
        when(requestData.getMid()).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(responseCodeUtil.getResponseCodeDetails(any(), any(), anyString())).thenReturn(responseCodeDetails);
        when(responseCodeDetails.getResponseCode()).thenReturn("test");
        when(responseCodeUtil.getResponseMsg(any())).thenReturn("test");
        when(requestData.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        Assert.assertNotNull(seamlessACSPaymentService.processPaymentRequest(requestData));

    }

    @Test
    public void testProcessPaymentRequestWhenIsSuccessfullyProcessedTrue() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.FGW_INVALID_VPA);
        when(requestData.getMid()).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(responseCodeUtil.getResponseCodeDetails(any(), any(), anyString())).thenReturn(responseCodeDetails);
        when(responseCodeDetails.getResponseCode()).thenReturn("");
        when(responseCodeUtil.getResponseMsg(any())).thenReturn("test");
        when(workFlowRequestBean.getPaytmMID()).thenReturn("test");
        when(workFlowRequestBean.getOrderID()).thenReturn("test");
        when(workFlowResponseBean.getTransID()).thenReturn("test");
        when(workFlowRequestBean.getTxnAmount()).thenReturn("100");
        when(acsUrlService.generateACSUrl(anyString(), anyString(), anyString())).thenReturn("test");
        when(requestData.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(workFlowResponseBean.getQueryPaymentStatus()).thenReturn(queryPaymentStatus);
        when(queryPaymentStatus.getWebFormContext()).thenReturn("test");
        doNothing().when(transactionCacheUtils)
                .putTransInfoInCache(anyString(), anyString(), anyString(), anyBoolean());
        doNothing().when(workFlowResponseBean).setSeamlessACSPaymentResponse(any());
        Assert.assertNotNull(seamlessACSPaymentService.processPaymentRequest(requestData));

    }

    @Test
    public void testProcessPaymentRequestWhenBizReqResponseMapperThrowsTheiaDataMappingException()
            throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenThrow(TheiaDataMappingException.class);
        when(requestData.getMid()).thenReturn("test");
        when(responseCodeUtil.getResponseCodeDetails(any(), any(), anyString())).thenReturn(responseCodeDetails);
        when(responseCodeDetails.getResponseCode()).thenReturn("");
        when(responseCodeUtil.getResponseMsg(any())).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(requestData.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        Assert.assertNotNull(seamlessACSPaymentService.processPaymentRequest(requestData));

    }

    @Test
    public void testProcessPaymentRequestWhenBizReqResponseMapperThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(requestData)).thenThrow(Exception.class);
        when(requestData.getMid()).thenReturn("test");
        when(responseCodeUtil.getResponseCodeDetails(any(), any(), anyString())).thenReturn(responseCodeDetails);
        when(responseCodeDetails.getResponseCode()).thenReturn("");
        when(responseCodeUtil.getResponseMsg(any())).thenReturn("test");
        when(requestData.getOrderId()).thenReturn("test");
        when(requestData.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        Assert.assertNotNull(seamlessACSPaymentService.processPaymentRequest(requestData));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                seamlessACSPaymentService.validatePaymentRequest(requestData));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                seamlessACSPaymentService.validatePaymentRequest(requestData));
    }

    @Test
    public void testGetResponseWithChecksumForJsonResponse() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("MID", "test");
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public HashMap<String, String> mapJsonToObject(String value, Class<HashMap> clazz) {
                return hashMap;
            }
        };

        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenReturn("test");
        Assert.assertNotNull(seamlessACSPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

    @Test
    public void testGetResponseWithChecksumForJsonResponseWhenIsChecksumEnabledFalse() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("MID", "test");
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public HashMap<String, String> mapJsonToObject(String value, Class<HashMap> clazz) {
                return hashMap;
            }
        };

        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenThrow(Exception.class);
        Assert.assertNotNull(seamlessACSPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

    @Test
    public void testGetResponseWithChecksumForJsonResponseWhenResultNotNull() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("MID", "test");
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public HashMap<String, String> mapJsonToObject(String value, Class<HashMap> clazz) {
                return hashMap;
            }
        };

        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenThrow(Exception.class);
        Assert.assertNotNull(seamlessACSPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

}