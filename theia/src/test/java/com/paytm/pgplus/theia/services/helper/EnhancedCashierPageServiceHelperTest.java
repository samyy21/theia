package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import mockit.MockUp;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class EnhancedCashierPageServiceHelperTest {

    @InjectMocks
    private EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper = new EnhancedCashierPageServiceHelper();

    @Mock
    private NativeSessionUtil nativeSessionUtil;

    @Mock
    private Ff4jUtils ff4jUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedCashierPageServiceHelper.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testFetchRedisKey() {
        enhancedCashierPageServiceHelper.fetchRedisKey("mid", "orderId");
    }

    @Test
    public void testGetTokenExpiryTime() {
        enhancedCashierPageServiceHelper.getTokenExpiryTime();

        new MockUp<ConfigurationUtil>() {
            @mockit.Mock
            public String getProperty(String key, String defaultValue) {
                return null;
            }

        };
        enhancedCashierPageServiceHelper.getTokenExpiryTime();
    }

    @Test
    public void testGetEnhancedCashierTheme() {
        enhancedCashierPageServiceHelper.getEnhancedCashierTheme(null);
        enhancedCashierPageServiceHelper.getEnhancedCashierTheme(EChannelId.WEB.getValue());
        enhancedCashierPageServiceHelper.getEnhancedCashierTheme(EChannelId.WAP.getValue());
        enhancedCashierPageServiceHelper.getEnhancedCashierTheme(EChannelId.APP.getValue());
    }

    @Test
    public void testGetEnhancedCheckoutJSTheme() {
        enhancedCashierPageServiceHelper.getEnhancedCheckoutJSTheme();
    }

    @Test
    public void testIsMidCustIdAllowedForWEBEnhanced() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("mid");
        paymentRequestBean.setCustId("custId");
        enhancedCashierPageServiceHelper.isMidCustIdAllowedForWEBEnhanced(paymentRequestBean);
    }

    @Test
    public void testIsMidCustIdBlockedForWEBEnhanced() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("mid");
        paymentRequestBean.setCustId("custId");
        enhancedCashierPageServiceHelper.isMidCustIdBlockedForWEBEnhanced(paymentRequestBean);
    }

    @Test
    public void testIsMidCustIdAllowedForWAPEnhanced() {
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setMid("mid");
        paymentRequestBean.setCustId("custId");
        enhancedCashierPageServiceHelper.isMidCustIdAllowedForWAPEnhanced(paymentRequestBean);
    }

    @Test
    public void testInvalidateEnhancedNativeData() {
        when(ff4jUtils.isFeatureEnabledOnMid(any(), any(), anyBoolean())).thenReturn(false);
        enhancedCashierPageServiceHelper.invalidateEnhancedNativeData("txnToken", "mid", "orderId");

        when(ff4jUtils.isFeatureEnabledOnMid(any(), any(), anyBoolean())).thenReturn(true);
        enhancedCashierPageServiceHelper.invalidateEnhancedNativeData("txnToken", "mid", "orderId");

        enhancedCashierPageServiceHelper.invalidateEnhancedNativeData("", "mid", "orderId");

    }
}