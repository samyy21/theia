package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.theia.cache.impl.ConfigurationDataServiceImpl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class NativeCODUtilsTest extends AOAUtilsTest {

    @InjectMocks
    NativeCODUtils nativeCODUtils = new NativeCODUtils();

    @Mock
    ConfigurationDataServiceImpl configurationDataService;

    @Mock
    PaytmProperty paytmProperty;

    @Test
    public void testGetMinimumCodAMount() {
        when(configurationDataService.getPaytmProperty(anyString())).thenReturn(paytmProperty);
        when(paytmProperty.getValue()).thenReturn("123");
        Assert.assertEquals("123", nativeCODUtils.getMinimumCodAmount());
    }

    @Test
    public void testGetMinimumCodAMountWhenAmountNotANumber() {
        when(configurationDataService.getPaytmProperty(anyString())).thenReturn(paytmProperty);
        when(paytmProperty.getValue()).thenReturn("abc");
        Assert.assertNull(nativeCODUtils.getMinimumCodAmount());
    }

}