package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.EagerLoadable;
import com.paytm.pgplus.common.util.ReloadablePropertyUtil;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.facade.enums.ClientName;
import com.paytm.pgplus.facade.utils.paymentAdapterUtils.ClientNameUtil;

import java.util.Iterator;

@EagerLoadable
public class ConfigurationUtil {

    private static final ClientNameUtil clientNameUtil = ClientNameUtil.getInstance(ClientName.THEIA);
    private static final ReloadablePropertyUtil reloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia.properties");
    private static final ReloadablePropertyUtil mockReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "project-theia.properties");
    private static final ReloadablePropertyUtil successRateReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-successrate.properties");
    private static final ReloadablePropertyUtil messageReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-message.properties");
    private static final ReloadablePropertyUtil upiReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "upi-config.properties");
    private static final ReloadablePropertyUtil mockUpiPropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "upi-config.properties");
    private static final ReloadablePropertyUtil upiMccReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-upi-mcc-mapping.properties");
    private static final ReloadablePropertyUtil mockUpiMccPropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "project-theia-upi-mcc-mapping.properties");

    public static String getProperty(String key) {

        if (ThreadLocalUtil.getForMockRequest()) {
            return mockReloadablePropertyUtil.getProperty(key);
        }
        return reloadablePropertyUtil.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (ThreadLocalUtil.getForMockRequest()) {
            return mockReloadablePropertyUtil.getProperty(key, defaultValue);
        }
        return reloadablePropertyUtil.getProperty(key, defaultValue);
    }

    public static String getSuccessRateProperty(String key) {
        return successRateReloadablePropertyUtil.getProperty(key);
    }

    public static String getSuccessRateProperty(String key, String defaultValue) {
        return successRateReloadablePropertyUtil.getProperty(key, defaultValue);
    }

    public static String getMessageProperty(String key) {
        return messageReloadablePropertyUtil.getProperty(key);
    }

    public static String getMessageProperty(String key, String defaultValue) {
        return messageReloadablePropertyUtil.getProperty(key, defaultValue);
    }

    public static Iterator getUpiProperties() {
        if (ThreadLocalUtil.getForMockRequest()) {
            return mockUpiPropertyUtil.getProperties();
        }
        return upiReloadablePropertyUtil.getProperties();
    }

    public static String getUpiProperty(String key) {
        return upiReloadablePropertyUtil.getProperty(key);
    }

    public static String getUpiMccProperty(String key) {

        if (ThreadLocalUtil.getForMockRequest()) {
            return mockUpiMccPropertyUtil.getProperty(key);
        }
        return upiMccReloadablePropertyUtil.getProperty(key);
    }

    public static String getUpiMccProperty(String key, String defaultValue) {
        if (ThreadLocalUtil.getForMockRequest()) {
            return mockUpiMccPropertyUtil.getProperty(key, defaultValue);
        }
        return upiMccReloadablePropertyUtil.getProperty(key, defaultValue);
    }

    public static boolean isRedisOPtimizedFlow() {
        // short-circuiting redis optimised flow
        return false;
        // return
        // Boolean.valueOf(ConfigurationUtil.getProperty(REDIS_OPTIMIZED_FLOW,
        // "false"));
    }

}
