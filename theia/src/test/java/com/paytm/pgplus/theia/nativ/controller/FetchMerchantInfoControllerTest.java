package com.paytm.pgplus.theia.nativ.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.UserInfo;
import com.paytm.pgplus.request.BaseHeader;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.controller.test.NativeControllerTest;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.FetchMerchantInfoRequest;
import com.paytm.pgplus.theia.nativ.processor.impl.FetchMerchantInfoRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.redis.impl.TheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import mockit.MockUp;
import org.apache.commons.io.IOUtils;
import org.hibernate.engine.config.internal.ConfigurationServiceImpl;
import org.hsqldb.rights.User;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.paytm.pgplus.facade.utils.JsonMapper.mapJsonToObject;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_THEIA_APP_INVOKE_AS_COLLECT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class FetchMerchantInfoControllerTest extends AbstractNativeControllerTest {

    @Autowired
    @InjectMocks
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @InjectMocks
    MerchantInfoService merchantInfoService;

    @Autowired
    @InjectMocks
    FetchMerchantInfoRequestProcessor requestProcessor;

    @Mock
    IMerchantPreferenceService merchantPreferenceService;

    @Mock
    IConfigurationService configurationServiceImpl;

    @Mock
    NativeValidationService nativeValidationService;

    @Mock
    TheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    Ff4jUtils ff4jUtils;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws MappingServiceClientException {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        new MockUp<EnvInfoUtil>() {
            @mockit.Mock
            public void setChannelDFromUserAgent(BaseHeader requestHeader) {

            }
        };
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);
        initiateTransactionRequest.setBody(new InitiateTransactionRequestBody());
        initiateTransactionRequest.getBody().setMid("mid");
        initiateTransactionRequest.getBody().setOrderId("orderId");
        initiateTransactionRequest.getBody().setTxnAmount(new Money());
        initiateTransactionRequest.getBody().setUserInfo(new UserInfo());
        initiateTransactionRequest.getBody().setPromoCode("promo");
        initiateTransactionRequest.getBody().setNeedAppIntentEndpoint(true);
        InitiateTransactionRequestBody orderDetail = initiateTransactionRequest.getBody();
        when(theiaSessionRedisUtil.hget("txnToken", "orderDetail")).thenReturn(nativeInitiateRequest);
        when(theiaSessionRedisUtil.hget("txnToken", "sendNotificationAppInvokeUserId")).thenReturn("userId");
        doNothing().when(theiaSessionRedisUtil).hdel(orderDetail.getOrderId().concat("_").concat(orderDetail.getMid()),
                TheiaConstant.ExtraConstants.APP_INVOKE_CALLBACK_URL);
        when(theiaSessionRedisUtil.hsetIfExist("txnToken", "orderDetail", nativeInitiateRequest)).thenReturn(true);
        UserDetailsBiz userDetailsBiz = new UserDetailsBiz();
        userDetailsBiz.setUserId("userId");
        when(nativeValidationService.validateSSOToken("ssoToken", "mid")).thenReturn(userDetailsBiz);
        doNothing().when(nativeValidationService).validateTxnAmount(
                initiateTransactionRequest.getBody().getTxnAmount().getValue());
        when(
                ff4jUtils.isFeatureEnabledOnMidAndCustIdOrUserId(FEATURE_THEIA_APP_INVOKE_AS_COLLECT,
                        initiateTransactionRequest.getBody().getMid(), initiateTransactionRequest.getBody()
                                .getUserInfo().getCustId(), "userId")).thenReturn(true);
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setResponse(new MappingServiceResultInfo());
        merchantBussinessLogoInfo.getResponse().setResultStatus("S");
        when(configurationServiceImpl.getMerchantlogoInfoFromMid("mid")).thenReturn(merchantBussinessLogoInfo);
        when(merchantPreferenceService.isAppInvokeAllowed("mid", true)).thenReturn(true);
    }

    @BeforeClass
    public static void setSystemProperty() {
        System.setProperty("catalina.base", "");
    }

    @Test
    public void fetchMerchantInfo() throws Exception {

        String merchantInfoRequest = "{\"head\":{\"ssoToken\":\"ssoToken\",\"txnToken\":\"txnToken\",\"tokenType\":\"SSO\",\"token\":\"token\",\"requestTimestamp\":\"\",\"workFlow\":\"\",\"version\":\"v2\",\"channelId\":\"APP\",\"requestId\":\"request\"},\"body\":{\"mid\":\"mid\",\"orderId\":\"orderId\"}}";
        assertNotNull(mvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v1/fetchMerchantInfo").accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON).content(merchantInfoRequest))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn());

    }

    @Test
    public void fetchMerchantInfoV2() throws Exception {
        String merchantInfoRequest = "{\"head\":{\"ssoToken\":\"ssoToken\",\"txnToken\":\"txnToken\",\"tokenType\":\"SSO\",\"token\":\"token\",\"requestTimestamp\":\"\",\"workFlow\":\"\",\"version\":\"v1\",\"channelId\":\"APP\",\"requestId\":\"request\"},\"body\":{\"mid\":\"mid\",\"orderId\":\"orderId\"}}";
        assertNotNull(mvc
                .perform(
                        MockMvcRequestBuilders.post("/api/v2/fetchMerchantInfo").accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON).content(merchantInfoRequest))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn());

    }
}