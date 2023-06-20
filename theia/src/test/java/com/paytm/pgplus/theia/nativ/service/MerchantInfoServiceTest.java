package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponseBody;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class MerchantInfoServiceTest {

    @InjectMocks
    MerchantInfoService merchantInfoService;

    @Mock
    private IConfigurationService configurationServiceImpl;

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private MerchantDataServiceImpl merchantDataService;

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    private MerchantInfoServiceRequest request;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        request = new MerchantInfoServiceRequest();
        request.setNativeInitiateRequest(new NativeInitiateRequest(new InitiateTransactionRequestBody()));
        request.getNativeInitiateRequest().getInitiateTxnReq().getBody().setTxnAmount(new Money());
        request.getNativeInitiateRequest().getInitiateTxnReq().getBody().setPromoCode("promo");
        MerchantBussinessLogoInfo merchantBussinessLogoInfo = new MerchantBussinessLogoInfo();
        merchantBussinessLogoInfo.setResponse(new MappingServiceResultInfo());
        merchantBussinessLogoInfo.getResponse().setResultStatus(ResultCode.SUCCESS.getResultStatus());

        when(configurationServiceImpl.getMerchantlogoInfoFromMid(any())).thenReturn(merchantBussinessLogoInfo);
        when(configurationServiceImpl.getMerchantlogoInfoFromMidV2(any())).thenReturn(merchantBussinessLogoInfo);

    }

    @Test
    public void fetchMerchantInfoResponse() throws MappingServiceClientException, IOException, IllegalPayloadException,
            HttpCommunicationException {

        merchantInfoService.fetchMerchantInfoResponse(request);
        verify(configurationServiceImpl, atMost(1)).getMerchantlogoInfoFromMid(any());
        verify(configurationServiceImpl, atMost(1)).getMerchantlogoInfoFromMidV2(any());

    }

    @Test
    public void mapSsoTxnTokens() {
        MerchantInfoResponse response = new MerchantInfoResponse();
        response.setBody(new MerchantInfoResponseBody());
        response.getBody().setAppInvokeAllowed(true);
        InitiateTransactionRequestBody orderDetail = request.getNativeInitiateRequest().getInitiateTxnReq().getBody();
        orderDetail.setNeedAppIntentEndpoint(false);
        orderDetail.setOrderId("Order1");
        orderDetail.setMid("mid");
        doNothing().when(nativeSessionUtil).setField(any(), any(), any(), anyByte());

        request.setSsoToken("SsoToken");
        merchantInfoService.mapSsoTxnTokens(request, false, response);
        verify(nativeSessionUtil, times(1)).setField(any(), any(), any(), anyByte());

    }

    @Test
    public void getRedisObject() {

        when(theiaTransactionalRedisUtil.get(any())).thenReturn(null);
        assertNull(merchantInfoService.getRedisObject("redis"));
    }
}