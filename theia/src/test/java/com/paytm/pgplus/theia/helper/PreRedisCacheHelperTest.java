package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PreRedisCacheHelperTest {

    @InjectMocks
    PreRedisCacheHelper preRedisCacheHelper;

    @Mock
    private IPreRedisCacheService preRedisCacheServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMerchantPreferenceStore() {

        when(preRedisCacheServiceImpl.getMerchantPreferenceStoreWithoutCache("mid")).thenReturn(
                new MerchantPreferenceStore());
        assertNotNull(preRedisCacheHelper.getMerchantPreferenceStore("mid"));
    }

    @Test
    public void getMerchantExtendedData() {

        when(preRedisCacheServiceImpl.getMerchantExtendedDataWithoutCache("mid")).thenReturn(
                new MerchantExtendedInfoResponse());
        assertNotNull(preRedisCacheHelper.getMerchantExtendedData("mid"));
    }

    @Test
    public void getMerchantExtendedDataFromClientId() {

        when(preRedisCacheServiceImpl.getMerchantExtendedDataFromClientIdWithoutCache("mid", "client")).thenReturn(
                new MerchantExtendedInfoResponse());
        assertNotNull(preRedisCacheHelper.getMerchantExtendedDataFromClientId("mid", "client"));
    }
}