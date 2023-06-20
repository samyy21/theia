package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class PGPreferenceHelperTest {

    @InjectMocks
    PGPreferenceHelper pgPreferenceHelper;

    @Mock
    private Ff4jUtils ff4jUtils;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Test
    public void checkPgAutologinEnabledFlag() {
        MockitoAnnotations.initMocks(this);
        when(merchantPreferenceService.isPgAutoLoginDisabled("mid", false)).thenReturn(false);
        when(ff4jUtils.isFeatureEnabledOnMid(anyString(), anyObject(), anyBoolean())).thenReturn(false);
        when(merchantPreferenceService.isPgAutoLoginEnabled("mid", false)).thenReturn(true);
        assertFalse(pgPreferenceHelper.checkPgAutologinEnabledFlag("mid"));
    }
}