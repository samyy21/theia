package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantApiUrlInfo;
import com.paytm.pgplus.cache.model.MerchantApiUrlInfoResponse;
import com.paytm.pgplus.cache.model.MerchantUrlInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MerchantUrlServiceImplTest {

    @InjectMocks
    MerchantUrlServiceImpl merchantUrlService;

    @Mock
    private IMerchantDataService merchantDataService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMerchantUrlInfo() throws MappingServiceClientException {
        MerchantUrlInput merchantUrlInput = new MerchantUrlInput("id", MappingMerchantUrlInfo.UrlTypeId.RESPONSE,
                "localhost");
        MerchantUrlInfo merchantUrlInfo = new MerchantUrlInfo();
        when(
                merchantDataService.getMerchantUrlInfo(merchantUrlInput.getMerchantId(), merchantUrlInput
                        .getUrlTypeId().name(), merchantUrlInput.getWebsite())).thenThrow(
                new MappingServiceClientException("")).thenReturn(null, merchantUrlInfo);
        try {
            merchantUrlService.getMerchantUrlInfo(merchantUrlInput);
            fail();
        } catch (PaymentRequestValidationException e) {

        }
        assertNull(merchantUrlService.getMerchantUrlInfo(merchantUrlInput));
        assertNotNull(merchantUrlService.getMerchantUrlInfo(merchantUrlInput));
    }

    @Test
    public void getMerchantUrlInfoV2() throws MappingServiceClientException {
        MerchantUrlInput merchantUrlInput = new MerchantUrlInput("id", MappingMerchantUrlInfo.UrlTypeId.RESPONSE,
                "localhost");
        MerchantUrlInfo merchantUrlInfo = new MerchantUrlInfo();
        when(
                merchantDataService.getMerchantUrlInfoV2(merchantUrlInput.getMerchantId(), merchantUrlInput
                        .getUrlTypeId().name(), merchantUrlInput.getWebsite())).thenThrow(
                new MappingServiceClientException("")).thenReturn(null, merchantUrlInfo);
        try {
            merchantUrlService.getMerchantUrlInfoV2(merchantUrlInput);
            fail();
        } catch (PaymentRequestValidationException e) {

        }
        assertNull(merchantUrlService.getMerchantUrlInfoV2(merchantUrlInput));
        assertNotNull(merchantUrlService.getMerchantUrlInfoV2(merchantUrlInput));

    }

    @Test
    public void getMerchantApiUrlInfo() throws MappingServiceClientException {
        String midType = "midType", mid = "mid";
        MerchantApiUrlInfoResponse merchantApiUrlInfoResponse = new MerchantApiUrlInfoResponse();
        merchantApiUrlInfoResponse.setMerchantId("");
        when(merchantDataService.getMerchantApiUrlInfo(midType, mid)).thenThrow(new MappingServiceClientException(""))
                .thenReturn(null, merchantApiUrlInfoResponse);
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        merchantApiUrlInfoResponse.setMerchantId(mid);
        when(merchantDataService.getMerchantApiUrlInfo(midType, mid)).thenReturn(merchantApiUrlInfoResponse);
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        merchantApiUrlInfoResponse.setMerchantApiUrlInfoList(null);
        when(merchantDataService.getMerchantApiUrlInfo(midType, mid)).thenReturn(merchantApiUrlInfoResponse);
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        merchantApiUrlInfoResponse.setMerchantApiUrlInfoList(Collections.emptyList());
        when(merchantDataService.getMerchantApiUrlInfo(midType, mid)).thenReturn(merchantApiUrlInfoResponse);
        assertNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
        merchantApiUrlInfoResponse.setMerchantApiUrlInfoList(Collections.singletonList(new MerchantApiUrlInfo()));
        when(merchantDataService.getMerchantApiUrlInfo(midType, mid)).thenReturn(merchantApiUrlInfoResponse);
        assertNotNull(merchantUrlService.getMerchantApiUrlInfo(midType, mid));
    }
}