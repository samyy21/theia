package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantUserInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantUserInfoServiceRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class MerchantUserInfoServiceTest {

    @InjectMocks
    MerchantUserInfoService merchantUserInfoService;

    @Mock
    private IConfigurationService configurationServiceImpl;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Mock
    private Ff4jUtils ff4jUtils;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Test
    public void fetchMerchantUserInfo() throws MappingServiceClientException {
        MockitoAnnotations.initMocks(this);
        MerchantUserInfoServiceRequest request = new MerchantUserInfoServiceRequest();
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setResponse(new MappingServiceResultInfo());
        merchantBussinessLogoInfo.getResponse().setResultStatus(ResultCode.SUCCESS.getResultStatus());
        request.setTokenType(TokenType.TXN_TOKEN.name());
        request.setTxnToken("txnToken");
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        nativeInitiateRequest.setInitiateTxnReq(new InitiateTransactionRequest());
        nativeInitiateRequest.getInitiateTxnReq().setBody(new InitiateTransactionRequestBody());
        when(theiaSessionRedisUtil.hget(request.getTxnToken(), "orderDetail")).thenReturn(null);
        when(theiaTransactionalRedisUtil.hget(request.getTxnToken(), "orderDetail")).thenReturn(nativeInitiateRequest);
        when(
                ff4jUtils.isFeatureEnabledOnMid(MERCHANT_USER_INFO_INITIATE_ORDER_DETAIL,
                        THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)).thenReturn(false);
        when(configurationServiceImpl.getMerchantlogoInfoFromMid(any())).thenReturn(merchantBussinessLogoInfo);
        when(configurationServiceImpl.getMerchantlogoInfoFromMidV2(any())).thenReturn(merchantBussinessLogoInfo);
        MerchantUserInfoResponse response = merchantUserInfoService.fetchMerchantUserInfo(request);
        assertNull(response.getBody().getUserInfoResp().getUserInfo());
        verify(configurationServiceImpl, atMost(1)).getMerchantlogoInfoFromMid(any());
        verify(configurationServiceImpl, atMost(1)).getMerchantlogoInfoFromMidV2(any());

    }
}