package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.impl.MerchantBankInfoDataServiceImpl;
import com.paytm.pgplus.theia.cache.impl.MerchantExtendedInfoDataServiceImpl;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:servlet-context-test.xml" })
@WebAppConfiguration
@EnableWebMvc
public class AOAUtilsTest {
    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("catalina.base", "");
    }

    @InjectMocks
    AOAUtils aoaUtils;

    @Mock
    MerchantExtendedInfoDataServiceImpl merchantExtendedInfoDataService;

    @Mock
    TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse;

    @Test
    public void testIsAOAMerchantWHenMidBlank() {
        Assert.assertFalse(aoaUtils.isAOAMerchant(""));
    }

    @Test
    public void testIsAOAMerchantWHenMidNotBlank() {
        Map<String, String> extendedInfo = new HashMap<>();
        extendedInfo.put("platformType", "yes");

        when(theiaMerchantExtendedDataResponse.getExtendedInfo()).thenReturn(extendedInfo);

        when(merchantExtendedInfoDataService.getMerchantExtendedInfoData(anyString())).thenReturn(
                theiaMerchantExtendedDataResponse);

        Assert.assertFalse(aoaUtils.isAOAMerchant("abc"));
    }

    @Test
    public void testIsAOAMerchantWhenPaymentRequestBeanNull() {

        PaymentRequestBean paymentRequestBean = null;
        Assert.assertFalse(aoaUtils.isAOAMerchant(paymentRequestBean));
    }

    @Test
    public void testIsAOAMerchantWhenPaymentRequestBeanNotNull() {
        Map<String, String> extendedInfo = new HashMap<>();
        extendedInfo.put("platformType", "yes");

        when(theiaMerchantExtendedDataResponse.getExtendedInfo()).thenReturn(extendedInfo);

        when(merchantExtendedInfoDataService.getMerchantExtendedInfoData(anyString())).thenReturn(
                theiaMerchantExtendedDataResponse);
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("abc");
        Assert.assertFalse(aoaUtils.isAOAMerchant(paymentRequestBean));
    }

    @Test
    public void testIsAOAMerchantWhenExtendedInfoNull() {
        Map<String, String> extendedInfo = new HashMap<>();
        extendedInfo.put("platformType", "yes");

        when(theiaMerchantExtendedDataResponse.getExtendedInfo()).thenReturn(null);

        when(merchantExtendedInfoDataService.getMerchantExtendedInfoData(anyString())).thenReturn(
                theiaMerchantExtendedDataResponse);
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("abc");
        Assert.assertFalse(aoaUtils.isAOAMerchant(paymentRequestBean));
    }

}