package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantInfo;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MerchantMappingServiceImplTest {

    @InjectMocks
    MerchantMappingServiceImpl merchantMappingService;

    @Mock
    private IMerchantDataService merchantDataService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMappingMerchantData() throws MappingServiceClientException {
        String id = "id";
        when(merchantDataService.getMerchantMappingData(id)).thenThrow(new PaymentRequestValidationException())
                .thenReturn(null, new MerchantInfo());
        try {
            merchantMappingService.getMappingMerchantData(id);
            fail();
        } catch (PaymentRequestValidationException e) {

        }
        assertNull(merchantMappingService.getMappingMerchantData(id));
        assertNotNull(merchantMappingService.getMappingMerchantData(id));
    }

    @Test
    public void fetchMerchanData() throws MappingServiceClientException {
        String id = "id";
        when(merchantDataService.getMerchantMappingData(id)).thenThrow(new PaymentRequestValidationException())
                .thenReturn(null, new MerchantInfo());
        assertNotNull(merchantMappingService.fetchMerchanData(id));
        assertNotNull(merchantMappingService.fetchMerchanData(id));
        assertNotNull(merchantMappingService.fetchMerchanData(id));

    }
}