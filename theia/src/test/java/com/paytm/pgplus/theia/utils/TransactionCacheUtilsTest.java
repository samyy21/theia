package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class TransactionCacheUtilsTest extends AOAUtilsTest {
    @InjectMocks
    TransactionCacheUtils transactionCacheUtils;

    @Mock
    ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Test
    public void testPutTransInfoInCache() {
        transactionCacheUtils.putTransInfoInCache("test", "test", "test", true);
        verify(theiaTransactionalRedisUtil, atMost(1)).set(anyString(), any(), anyLong());
    }

    @Test
    public void testPutTransInfoInCacheWhenIsFundOrderFalse() {
        transactionCacheUtils.putTransInfoInCache("test", "test", "test", false);
        verify(theiaTransactionalRedisUtil, atMost(1)).set(anyString(), any(), anyLong());
    }

    @Test
    public void testPutTransInfoInCacheWhenArgsFiveWhenIsFundOrderTrue() {
        List<String> testList = new ArrayList<>();
        transactionCacheUtils.putTransInfoInCache("test", "test", "test", true, "test");
        verify(theiaTransactionalRedisUtil, atMost(1)).set(anyString(), any(), anyLong());

    }

    @Test
    public void testPutTransInfoInCacheWhenArgsFiveWhenIsFundOrderFalse() {

        transactionCacheUtils.putTransInfoInCache("test", "test", "test", false, "test");
        verify(theiaTransactionalRedisUtil, atMost(1)).set(anyString(), any(), anyLong());
    }

    @Test
    public void testPutTransInfoInCacheWrapper() {
        transactionCacheUtils.putTransInfoInCacheWrapper("test", "test", "test", true, ERequestType.DEFAULT);
        verify(theiaTransactionalRedisUtil, atMost(1)).set(anyString(), any(), anyLong());
    }

}