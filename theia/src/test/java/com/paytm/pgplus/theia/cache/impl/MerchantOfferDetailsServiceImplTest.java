package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.cache.model.MerchantOfferDetails;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.merchant.models.MappingMerchantOfferDetails;
import com.paytm.pgplus.theia.merchant.models.MerchantOfferDetailsInput;
import org.junit.Before;
import org.mockito.Mock;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MerchantOfferDetailsServiceImplTest {

    @InjectMocks
    private MerchantOfferDetailsServiceImpl merchantOfferDetailsService;

    @Mock
    private IMerchantDataService merchantDataService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMerchantOfferDetails() throws MappingServiceClientException {

        MerchantOfferDetailsInput merchantUrlInput = new MerchantOfferDetailsInput("id",
                MappingMerchantOfferDetails.Channel.WEB, "localhost");
        when(
                merchantDataService.getMerchantOfferDetails(merchantUrlInput.getMerchantId(), merchantUrlInput
                        .getChannel().name(), merchantUrlInput.getWebsite())).thenThrow(
                new PaymentRequestValidationException()).thenReturn(null, new MerchantOfferDetails());
        assertNull(merchantOfferDetailsService.getMerchantOfferDetails(merchantUrlInput));
        assertNull(merchantOfferDetailsService.getMerchantOfferDetails(merchantUrlInput));
        assertNotNull(merchantOfferDetailsService.getMerchantOfferDetails(merchantUrlInput));

    }
}