package com.paytm.pgplus.theia.nativ.utils;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.testng.Assert;

import static org.junit.Assert.*;

public class CyberSourceUtilTest extends AOAUtilsTest {

    @InjectMocks
    CyberSourceUtil cyberSourceUtil;

    @Test
    public void testGetStatusQryProps() {
        Assert.assertNotNull(CyberSourceUtil.getStatusQryProps("a", "b", "c", "d"));
    }

}