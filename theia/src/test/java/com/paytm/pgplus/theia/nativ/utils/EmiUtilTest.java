package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.cache.model.EmiOnDcResponse;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.impl.EmiOnDcDetails;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class EmiUtilTest extends AOAUtilsTest {

    @InjectMocks
    EmiUtil emiUtil;

    @Mock
    EmiOnDcDetails emiOnDcDetails;

    @Mock
    EmiOnDcResponse emiOnDcResponse;

    @Mock
    Environment env;

    @Test
    public void testIsUserEligibleForEmiOnDc() throws MappingServiceClientException {
        when(emiOnDcDetails.getEmiOnDcEligibilityDetails(anyString(), anyString())).thenReturn(emiOnDcResponse);
        when(emiOnDcResponse.isEmiOnDcEnable()).thenReturn(true);
        when(env.getProperty(anyString())).thenReturn("abc");

        Assert.assertTrue(emiUtil.isUserEligibleforEmiOnDc("1234567890", "HDFC"));

    }

    @Test
    public void testIsUserEligibleForEmiOnDcWhenThrowsException() throws MappingServiceClientException {

        when(emiOnDcDetails.getEmiOnDcEligibilityDetails(anyString(), anyString())).thenThrow(
                MappingServiceClientException.class);
        Assert.assertFalse(emiUtil.isUserEligibleforEmiOnDc("123456789", "HDFC"));
    }

}