package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.core.user.service.impl.UserMappingServiceImpl;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.cache.model.UserInfo;
import com.paytm.pgplus.facade.user.models.UserDetails;
import com.paytm.pgplus.facade.user.models.UserName;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.mappingserviceclient.service.impl.ConfigurationserviceImpl;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.UserDataMappingInput;
import com.paytm.pgplus.theia.nativ.OAuthHelper;
import com.paytm.pgplus.theia.nativ.service.MerchantUserInfoService;
import org.junit.Ignore;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class FetchMerchantUserInfoControllerTest extends AbstractNativeControllerTest {

    @Autowired
    @InjectMocks
    Ff4jUtils ff4jUtils;

    @Autowired
    @InjectMocks
    OAuthHelper oAuthHelper;

    @Autowired
    @InjectMocks
    MerchantUserInfoService merchantUserInfoService;

    @Mock
    ConfigurationserviceImpl configurationserviceImpl;

    @Mock
    IAuthentication authFacade;

    @Mock
    IPgpFf4jClient iPgpFf4jClient;

    @Before
    public void setup() {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void fetchMerchantUserInfo() throws Exception {

        String fetchMerchantUserInfoRequest = "{\n\"head\": {\n\"tokenType\": \"SSO\",\n\"token\": \"token\",\n\"requestTimestamp\": \"\",\n\"workFlow\": \"fake_data\",\n\"version\": \"v2\",\n\"channelId\": \"APP\",\n\"requestId\": \"requestId\"\n},\n \"body\":{\n\"mid\": \"mid\",\n\"orderId\":\"orderId\"\n}\n}";

        when(iPgpFf4jClient.checkWithdefault(anyString(), anyObject(), anyBoolean())).thenReturn(true)
                .thenReturn(false);
        when(authFacade.fetchUserDetailsV2(any())).thenReturn(
                new FetchUserDetailsResponse(setUserDetails(new UserDetails())));
        when(configurationserviceImpl.getMerchantlogoInfoFromMid("mid")).thenReturn(getMerchantUserInfo());
        assertNotNull(mvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v1/fetchMerchantUserInfo").accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON).content(fetchMerchantUserInfoRequest))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn());

    }

    private MerchantBussinessLogoInfo getMerchantUserInfo() {

        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        MappingServiceResultInfo mappingServiceResultInfo = new MappingServiceResultInfo();
        mappingServiceResultInfo.setResultStatus("S");
        merchantBussinessLogoInfo.setResponse(mappingServiceResultInfo);
        return merchantBussinessLogoInfo;
    }

    private UserDetails setUserDetails(UserDetails userDetails) {

        userDetails.setEmail("email");
        userDetails.setMobileNo("mobile");
        userDetails.setUserId("userId");
        userDetails.setUserName("userName");
        userDetails.setKYC(true);
        userDetails.setPaytmCCEnabled(true);
        userDetails.setPostpaidStatus("postpaid");
        userDetails.setChildUserId("childUserId");
        userDetails.setUserTypes(Collections.singletonList("PPB_CUSTOMER"));
        return userDetails;
    }
}