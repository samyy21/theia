package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MerchantExtendedInfoDataServiceImplTest {

    @InjectMocks
    private MerchantExtendedInfoDataServiceImpl merchantExtendedInfoDataService;

    @Mock
    private PreRedisCacheHelper preRedisCacheHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMerchantExtendedInfoData() {
        String merchantid = "id";
        when(preRedisCacheHelper.getMerchantExtendedData(merchantid))
                .thenThrow(new PaymentRequestValidationException())
                .thenReturn(null, new MerchantExtendedInfoResponse());
        try {
            merchantExtendedInfoDataService.getMerchantExtendedInfoData(merchantid);
            fail();
        } catch (PaymentRequestValidationException e) {

        }
        assertNull(merchantExtendedInfoDataService.getMerchantExtendedInfoData(merchantid));
        assertNotNull(merchantExtendedInfoDataService.getMerchantExtendedInfoData(merchantid));

    }

    @Test
    public void getMerchantExtendedInfoDataFromClientId() {
        String merchantid = "id", clientId = "id";
        when(preRedisCacheHelper.getMerchantExtendedDataFromClientId(merchantid, clientId)).thenThrow(
                new PaymentRequestValidationException()).thenReturn(null, new MerchantExtendedInfoResponse());
        try {
            merchantExtendedInfoDataService.getMerchantExtendedInfoDataFromClientId(merchantid, clientId);
            fail();
        } catch (PaymentRequestValidationException e) {

        }
        assertNull(merchantExtendedInfoDataService.getMerchantExtendedInfoDataFromClientId(merchantid, clientId));
        assertNotNull(merchantExtendedInfoDataService.getMerchantExtendedInfoDataFromClientId(merchantid, clientId));
    }

    @Test
    public void getMerchanExtendedDataFromCache() {
        String merchantid = "id";
        when(preRedisCacheHelper.getMerchantExtendedData(merchantid))
                .thenThrow(new PaymentRequestValidationException()).thenReturn(new MerchantExtendedInfoResponse());
        assertNotNull(merchantExtendedInfoDataService.getMerchanExtendedDataFromCache(merchantid));
        assertNotNull(merchantExtendedInfoDataService.getMerchanExtendedDataFromCache(merchantid));

    }
}