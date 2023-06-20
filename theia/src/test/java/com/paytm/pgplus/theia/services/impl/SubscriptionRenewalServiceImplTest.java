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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class SubscriptionRenewalServiceImplTest {

    @InjectMocks
    SubscriptionRenewalServiceImpl subscriptionRenewalService;

    @Mock
    PaymentRequestBean requestBean;

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
    HttpServletRequest httpServletRequest;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    HttpSession httpSession;

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
        when(bizResponseBean.getResponseConstant()).thenReturn(ResponseConstants.NO_PWP_SUPPORT_MERCHANT);
        when(bizResponseBean.getResponse()).thenReturn(null);
        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        Assert.assertNotNull(subscriptionRenewalService.processPaymentRequest(requestBean));
    }

    @Test
    public void testProcessPaymentRequestThrowsException() throws TheiaDataMappingException {
        when(bizRequestResponseMapper.mapWorkFlowRequestData(any())).thenThrow(TheiaDataMappingException.class);
        when(requestBean.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(requestBean.getOrderId()).thenReturn("test");
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getSubscriptionID()).thenReturn("test");
        when(requestBean.getMerchUniqueReference()).thenReturn("test");
        doNothing().when(httpSession).invalidate();
        Assert.assertNotNull(subscriptionRenewalService.processPaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnValidationSuccess() {
        when(checksumService.validateChecksum(any())).thenReturn(true);
        Assert.assertEquals(ValidationResults.VALIDATION_SUCCESS,
                subscriptionRenewalService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testValidatePaymentRequestWhenReturnChecksumValidtionFailure() {
        when(checksumService.validateChecksum(any())).thenReturn(false);
        Assert.assertEquals(ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                subscriptionRenewalService.validatePaymentRequest(requestBean));
    }

    @Test
    public void testGetResponseWithChecksumForJsonResponse() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("MID", "test");
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public TreeMap<String, String> mapJsonToObject(String value, Class<TreeMap> clazz) {
                return treeMap;
            }
        };

        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(true);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenReturn("test");
        Assert.assertNotNull(subscriptionRenewalService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

    @Test
    public void testGetResponseWithChecksumForJsonResponseWhenIsChecksumEnabledFalse() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("MID", "test");
        new MockUp<JsonMapper>() {
            @mockit.Mock
            public TreeMap<String, String> mapJsonToObject(String value, Class<TreeMap> clazz) {
                return treeMap;
            }
        };

        when(merchantPreferenceService.isChecksumEnabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.getMerchantKey(anyString(), anyString())).thenThrow(Exception.class);
        Assert.assertNotNull(subscriptionRenewalService.getResponseWithChecksumForJsonResponse("test", "test"));
    }

}