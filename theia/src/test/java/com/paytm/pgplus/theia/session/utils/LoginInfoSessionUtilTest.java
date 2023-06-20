package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.request.ConsultPayViewResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.facade.acquiring.services.impl.AcquiringOrderImpl;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class LoginInfoSessionUtilTest extends AOAUtilsTest {

    @InjectMocks
    LoginInfoSessionUtil loginInfoSessionUtil;

    @Mock
    PaymentRequestBean requestData;

    @Mock
    WorkFlowResponseBean workFlowResponseBean;

    @Mock
    ITheiaSessionDataService theiaSessionDataService;

    @Mock
    LoginInfo loginInfo;

    @Mock
    OAuthUserInfo userInfo;

    @Mock
    UserDetailsBiz userDetailsBiz;

    @Mock
    ConsultPayViewResponseBizBean payViewResponseBizBean;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    PayMethodViewsBiz payMethodViewsBiz;

    @Test
    public void testSetLoginInfoIntoSession() {
        List<String> testList = new ArrayList<>();
        testList.add("test");

        List<PayMethodViewsBiz> testPayMethodList = new ArrayList<>();
        testPayMethodList.add(payMethodViewsBiz);

        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(userInfo);
        when(workFlowResponseBean.getUserDetails()).thenReturn(userDetailsBiz);
        when(userDetailsBiz.getUserName()).thenReturn("test");
        when(userDetailsBiz.getEmail()).thenReturn("test");
        when(userDetailsBiz.getUserId()).thenReturn("test");
        when(userDetailsBiz.getMobileNo()).thenReturn("1234567890");
        when(userDetailsBiz.getInternalUserId()).thenReturn("test");
        when(userDetailsBiz.getPayerAccountNumber()).thenReturn("123456");
        when(userDetailsBiz.isKYC()).thenReturn(true);
        when(requestData.getPaytmToken()).thenReturn("test");
        when(userDetailsBiz.getUserTypes()).thenReturn(testList);
        when(workFlowResponseBean.getMerchnatViewResponse()).thenReturn(payViewResponseBizBean);
        when(payViewResponseBizBean.isLoginMandatory()).thenReturn(true);
        when(merchantPreferenceService.isAutoLoginEnable(anyString())).thenReturn(true);
        when(merchantPreferenceService.isLoginDisabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.isLoginViaOtpEnabled(anyString())).thenReturn(true);
        when(payViewResponseBizBean.getPayMethodViews()).thenReturn(testPayMethodList);
        when(requestData.getMid()).thenReturn("test");
        when(payMethodViewsBiz.getPayMethod()).thenReturn("BALANCE");
        loginInfoSessionUtil.setLoginInfoIntoSession(requestData, workFlowResponseBean);
        verify(theiaSessionDataService, atMost(1)).getLoginInfoFromSession(any(), anyBoolean());
    }

    @Test
    public void testSetLoginInfoIntoSessionWhenUserDetailsNone() {

        List<PayMethodViewsBiz> testPayMethodList = new ArrayList<>();
        testPayMethodList.add(payMethodViewsBiz);

        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(userInfo);
        when(workFlowResponseBean.getUserDetails()).thenReturn(null);
        when(workFlowResponseBean.getMerchnatViewResponse()).thenReturn(payViewResponseBizBean);
        when(payViewResponseBizBean.isLoginMandatory()).thenReturn(true);
        when(merchantPreferenceService.isAutoLoginEnable(anyString())).thenReturn(false);
        when(merchantPreferenceService.isLoginDisabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.isLoginViaOtpEnabled(anyString())).thenReturn(true);
        when(payViewResponseBizBean.getPayMethodViews()).thenReturn(testPayMethodList);
        when(requestData.getMid()).thenReturn("test");
        when(payMethodViewsBiz.getPayMethod()).thenReturn("CREDIT_CARD");
        loginInfoSessionUtil.setLoginInfoIntoSession(requestData, workFlowResponseBean);
        verify(theiaSessionDataService, atMost(1)).getLoginInfoFromSession(any(), anyBoolean());
    }

    @Test
    public void testSetLoginInfoIntoSessionWhenMidBlank() {

        List<PayMethodViewsBiz> testPayMethodList = new ArrayList<>();
        testPayMethodList.add(payMethodViewsBiz);

        when(theiaSessionDataService.getLoginInfoFromSession(any(), anyBoolean())).thenReturn(loginInfo);
        when(loginInfo.getUser()).thenReturn(userInfo);
        when(workFlowResponseBean.getUserDetails()).thenReturn(null);
        when(workFlowResponseBean.getMerchnatViewResponse()).thenReturn(payViewResponseBizBean);
        when(payViewResponseBizBean.isLoginMandatory()).thenReturn(true);
        when(merchantPreferenceService.isAutoLoginEnable(anyString())).thenReturn(false);
        when(merchantPreferenceService.isLoginDisabled(anyString())).thenReturn(false);
        when(merchantExtendInfoUtils.isLoginViaOtpEnabled(anyString())).thenReturn(true);
        when(payViewResponseBizBean.getPayMethodViews()).thenReturn(testPayMethodList);
        when(requestData.getMid()).thenReturn("");
        when(payMethodViewsBiz.getPayMethod()).thenReturn("test");
        loginInfoSessionUtil.setLoginInfoIntoSession(requestData, workFlowResponseBean);
        verify(theiaSessionDataService, atMost(1)).getLoginInfoFromSession(any(), anyBoolean());
    }

}