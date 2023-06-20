package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.core.merchant.service.IMerchantMappingService;
import com.paytm.pgplus.biz.core.user.service.IUserMappingService;
import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput;
import com.paytm.pgplus.theia.cache.impl.MerchantMappingServiceImpl;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RiskExtendedInfoUtilTest extends AOAUtilsTest {

    @InjectMocks
    RiskExtendedInfoUtil riskExtendedInfoUtil;

    @Mock
    LoginInfo loginInfo;

    @Mock
    OAuthUserInfo authUserInfo;

    @Mock
    IMerchantMappingService merchantMappingService;

    @Mock
    MerchantExtendedInfoResponse merchantExtendedInfoResponse;

    @Mock
    MerchantExtendedInfoResponse.MerchantExtendedInfo extendedInfo;

    @Mock
    IUserMappingService userMappingService;

    @Mock
    UserInfo userInfo;

    @Test
    public void testSelectRiskExtendedInfo() {
        when(loginInfo.getUser()).thenReturn(authUserInfo);
        when(authUserInfo.isKYC()).thenReturn(true);
        Assert.assertNotNull(riskExtendedInfoUtil.selectRiskExtendedInfo(loginInfo));
    }

    @Test
    public void testSetMerchantUserIdInRiskExtendInfo() throws Exception {
        Map<String, String> riskExtendedInfo = new HashMap<>();
        when(merchantMappingService.getMerchantInfoResponse(anyString())).thenReturn(merchantExtendedInfoResponse);
        when(merchantExtendedInfoResponse.getExtendedInfo()).thenReturn(extendedInfo);
        when(extendedInfo.getUserId()).thenReturn("test");
        when(userInfo.getAlipayId()).thenReturn("test");
        when(userMappingService.getUserData("test", UserDataMappingInput.UserOwner.PAYTM)).thenReturn(userInfo);
        riskExtendedInfoUtil.setMerchantUserIdInRiskExtendInfo("test", riskExtendedInfo);
        verify(merchantMappingService, atMost(1)).getMerchantInfoResponse(anyString());
    }

    @Test
    public void testSetMerchantUserIdInRiskExtendInfoWhenThrowsException() throws Exception {
        Map<String, String> riskExtendedInfo = new HashMap<>();
        when(merchantMappingService.getMerchantInfoResponse(anyString())).thenReturn(merchantExtendedInfoResponse);
        when(merchantExtendedInfoResponse.getExtendedInfo()).thenReturn(extendedInfo);
        when(extendedInfo.getUserId()).thenReturn("test");
        when(userInfo.getAlipayId()).thenReturn("");
        when(userMappingService.getUserData("test", UserDataMappingInput.UserOwner.PAYTM)).thenThrow(Exception.class);

        riskExtendedInfoUtil.setMerchantUserIdInRiskExtendInfo("test", riskExtendedInfo);
        verify(merchantMappingService, atMost(1)).getMerchantInfoResponse(anyString());
    }

}