package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.dynamicwrapper.core.config.impl.CacheService;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DynamicWrapperUtilTest extends AOAUtilsTest {

    @InjectMocks
    DynamicWrapperUtil dynamicWrapperUtil;

    @Mock
    CacheService cacheService;

    @Test
    public void testIsDynamicWrapperEnabled() {
        Assert.assertFalse(dynamicWrapperUtil.isDynamicWrapperEnabled());
    }

    @Test
    public void testIsDynamicWrapperConfigPresent() {
        when(cacheService.isDynamicWrapperConfigPresent(anyString())).thenReturn(true);
        Assert.assertTrue(dynamicWrapperUtil.isDynamicWrapperConfigPresent("test", API.CHECK_BALANCE,
                PayloadType.REQUEST));
    }

}