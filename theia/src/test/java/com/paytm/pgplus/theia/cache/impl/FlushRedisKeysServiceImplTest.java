package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class FlushRedisKeysServiceImplTest {

    @InjectMocks
    FlushRedisKeysServiceImpl flushRedisKeysService;

    @Mock
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void flushRedisKeys() {

        flushRedisKeysService.flushRedisKeys(new MockHttpServletRequest());
    }
}