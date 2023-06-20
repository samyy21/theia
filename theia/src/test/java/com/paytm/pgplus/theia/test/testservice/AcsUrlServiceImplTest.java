package com.paytm.pgplus.theia.test.testservice;

import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
//import org.junit.Ignore;
import org.junit.Before;
import org.junit.Test;

import com.paytm.pgplus.theia.acs.service.IAcsUrlService;
import com.paytm.pgplus.theia.acs.service.impl.AcsUrlServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AcsUrlServiceImplTest {

    @Mock
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @InjectMocks
    private IAcsUrlService acsUrlServiceImpl = new AcsUrlServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcsUrlSuccessResolving() {
        String acsUrl = acsUrlServiceImpl.generateACSUrl("mid", "orderId", "webForm");
        Assert.assertTrue("ACS Url should not be empty", StringUtils.isNotBlank(acsUrl));
        String[] split1 = acsUrl.split("resolveACS");
        Assert.assertTrue(split1.length == 2);
        String[] queryParams = split1[1].split("&");
        Assert.assertTrue(queryParams.length == 3);
        String mid = queryParams[0].split("=")[1];
        String orderId = queryParams[1].split("=")[1];
        String uniqueId = queryParams[2].split("=")[1];
        Assert.assertTrue(StringUtils.isNotBlank(uniqueId));
        Assert.assertTrue(StringUtils.isNotBlank(orderId));
        Assert.assertTrue(StringUtils.isNotBlank(mid));
        String webForm = acsUrlServiceImpl.resolveACSUrl(mid, orderId, uniqueId);
        Assert.assertTrue("webForm".equals(webForm));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcsUrlInvalidId() {
        String acsUrl = acsUrlServiceImpl.generateACSUrl("mid", "orderId", "webForm");
        Assert.assertTrue("ACS Url should not be empty", StringUtils.isNotBlank(acsUrl));
        String[] split1 = acsUrl.split("resolveACS");
        Assert.assertTrue(split1.length == 2);
        String[] queryParams = split1[1].split("&");
        Assert.assertTrue(queryParams.length == 3);
        String mid = queryParams[0].split("=")[1];
        String orderId = queryParams[1].split("=")[1];
        String uniqueId = queryParams[2].split("=")[1];
        Assert.assertTrue(StringUtils.isNotBlank(uniqueId));
        Assert.assertTrue(StringUtils.isNotBlank(orderId));
        Assert.assertTrue(StringUtils.isNotBlank(mid));
        acsUrlServiceImpl.resolveACSUrl(mid, orderId, uniqueId + "random");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcsUrlInvalidMid() {
        String acsUrl = acsUrlServiceImpl.generateACSUrl("mid", "orderId", "webForm");
        Assert.assertTrue("ACS Url should not be empty", StringUtils.isNotBlank(acsUrl));
        String[] split1 = acsUrl.split("resolveACS");
        Assert.assertTrue(split1.length == 2);
        String[] queryParams = split1[1].split("&");
        Assert.assertTrue(queryParams.length == 3);
        String mid = queryParams[0].split("=")[1];
        String orderId = queryParams[1].split("=")[1];
        String uniqueId = queryParams[2].split("=")[1];
        Assert.assertTrue(StringUtils.isNotBlank(uniqueId));
        Assert.assertTrue(StringUtils.isNotBlank(orderId));
        Assert.assertTrue(StringUtils.isNotBlank(mid));
        acsUrlServiceImpl.resolveACSUrl(mid, orderId + "random", uniqueId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcsUrlInvalidOrderId() {
        String acsUrl = acsUrlServiceImpl.generateACSUrl("mid", "orderId", "webForm");
        Assert.assertTrue("ACS Url should not be empty", StringUtils.isNotBlank(acsUrl));
        String[] split1 = acsUrl.split("resolveACS");
        Assert.assertTrue(split1.length == 2);
        String[] queryParams = split1[1].split("&");
        Assert.assertTrue(queryParams.length == 3);
        String mid = queryParams[0].split("=")[1];
        String orderId = queryParams[1].split("=")[1];
        String uniqueId = queryParams[2].split("=")[1];
        Assert.assertTrue(StringUtils.isNotBlank(uniqueId));
        Assert.assertTrue(StringUtils.isNotBlank(orderId));
        Assert.assertTrue(StringUtils.isNotBlank(mid));
        acsUrlServiceImpl.resolveACSUrl(mid, orderId + "random", uniqueId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcsUrlDuplicateHit() {
        String acsUrl = acsUrlServiceImpl.generateACSUrl("mid", "orderId", "webForm");
        Assert.assertTrue("ACS Url should not be empty", StringUtils.isNotBlank(acsUrl));
        String[] split1 = acsUrl.split("resolveACS");
        Assert.assertTrue(split1.length == 2);
        String[] queryParams = split1[1].split("&");
        Assert.assertTrue(queryParams.length == 3);
        String mid = queryParams[0].split("=")[1];
        String orderId = queryParams[1].split("=")[1];
        String uniqueId = queryParams[2].split("=")[1];
        Assert.assertTrue(StringUtils.isNotBlank(uniqueId));
        Assert.assertTrue(StringUtils.isNotBlank(orderId));
        Assert.assertTrue(StringUtils.isNotBlank(mid));
        String webForm = acsUrlServiceImpl.resolveACSUrl(mid, orderId, uniqueId);
        acsUrlServiceImpl.purgeAcsUrl(mid, orderId);
        Assert.assertTrue("webForm".equals(webForm));
        acsUrlServiceImpl.resolveACSUrl(mid, orderId, uniqueId);
    }

}
