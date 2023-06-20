package com.paytm.pgplus.theia.promo.service;

import com.paytm.pgplus.cache.enums.MappingServiceResultCode;
import com.paytm.pgplus.cache.model.MappingServiceResultInfo;
import com.paytm.pgplus.cache.model.MerchantInfoResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.theia.promo.model.FetchMerchantInfoRequest;
import com.paytm.pgplus.theia.promo.model.FetchMerchantInfoRequestBody;
import com.paytm.pgplus.theia.promo.model.RequestHeader;
import com.paytm.pgplus.cache.model.MerchantInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import java.util.Collections;

public class MerchantInfoServicePromoTest {

    @InjectMocks
    private MerchantInfoService merchantInfoService = new MerchantInfoService();

    @Mock
    private IMerchantDataService merchantDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantInfoService.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetMerchantInfoResponse() throws MappingServiceClientException {
        FetchMerchantInfoRequest request = new FetchMerchantInfoRequest();
        getFetchMerchantInfoRequestSetHead(request);
        getFetchMerchantInfoRequestSetBody(request);
        MerchantInfoResponse merchantInfoResponse = new MerchantInfoResponse();
        merchantInfoResponse.setResponse(new MappingServiceResultInfo());
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setAlipayId("alipayId");
        merchantInfo.setOfficialName("officialName");
        merchantInfo.setPaytmId("PaytmId");
        merchantInfoResponse.getResponse().setResultCode(MappingServiceResultCode.RESPONSE_BLANK);
        merchantInfoResponse.getResponse().setMessaage("message");
        merchantInfoResponse.setMerchantInfoList(Collections.singletonList(merchantInfo));
        when(merchantDataService.getMerchantInfoList(any())).thenReturn(merchantInfoResponse);
        merchantInfoService.getMerchantInfoResponse(request);

        when(merchantDataService.getMerchantInfoList(any())).thenThrow(
                new MappingServiceClientException("errorMessage"));
        merchantInfoService.getMerchantInfoResponse(request);

    }

    @Test
    public void testGetMerchantInfoResponseWhenMerchantInfoResponseIsNull() {
        FetchMerchantInfoRequest request = new FetchMerchantInfoRequest();
        getFetchMerchantInfoRequestSetHead(request);
        getFetchMerchantInfoRequestSetBody(request);
        merchantInfoService.getMerchantInfoResponse(request);
    }

    @Test
    public void testIsValidRequest() {
        FetchMerchantInfoRequest request = new FetchMerchantInfoRequest();
        getFetchMerchantInfoRequestSetBody(request);
        getFetchMerchantInfoRequestSetHead(request);
        merchantInfoService.isValidRequest(request);
    }

    private void getFetchMerchantInfoRequestSetHead(FetchMerchantInfoRequest request) {
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("requestId");
        requestHeader.setClientId("clientId");
        requestHeader.setToken("token");
        requestHeader.setTokenType("tokenType");
        requestHeader.setVersion("version");
        requestHeader.setRequestTimeStamp("requestTimeStamp");
        request.setHead(requestHeader);
    }

    private void getFetchMerchantInfoRequestSetBody(FetchMerchantInfoRequest request) {
        FetchMerchantInfoRequestBody fetchMerchantInfoRequestBody = new FetchMerchantInfoRequestBody();
        fetchMerchantInfoRequestBody.setMid(Collections.singletonList("mid,123456"));
        request.setBody(fetchMerchantInfoRequestBody);
    }
}