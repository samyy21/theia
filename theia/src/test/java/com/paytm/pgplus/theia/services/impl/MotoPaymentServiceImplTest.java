package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;

import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;

import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.IBizService;

import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;

import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class MotoPaymentServiceImplTest {

    @InjectMocks
    MotoPaymentServiceImpl motoPaymentService;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    IBizRequestResponseMapper bizRequestResponseMapper;

    @Mock
    WorkFlowRequestBean workFlowRequestBean;

    @Mock
    IBizService bizService;

    @Mock
    GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    ChecksumService checksumService;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    HttpSession httpSession;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPaymentRequest() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenReturn(workFlowRequestBean);
        when(bizService.processWorkFlow(any(), any())).thenReturn(bizResponseBean);
        when(bizResponseBean.isSuccessfullyProcessed()).thenReturn(false);
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.FGW_INVALID_VPA);
        when(bizResponseBean.getFailureDescription()).thenReturn("test");
        when(bizResponseBean.getResponse()).thenReturn(workFlowResponseBean);
        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        Assert.assertNotNull(motoPaymentService.processPaymentRequest(requestBean));
    }

    @Test
    public void testProcessPaymentRequestWhenThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(Exception.class);
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getOrderId()).thenReturn("test");
        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        doNothing().when(httpSession).invalidate();

        Assert.assertNotNull(motoPaymentService.processPaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                motoPaymentService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidationFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                motoPaymentService.validatePaymentRequest(requestBean));
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
        Assert.assertNotNull(motoPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
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
        Assert.assertNotNull(motoPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
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
        Assert.assertNotNull(motoPaymentService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

}