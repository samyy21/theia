package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.EagerLoadable;
import com.paytm.pgplus.common.util.ReloadablePropertyUtil;
import com.paytm.pgplus.common.util.ThreadLocalUtil;

@EagerLoadable
public class ConfigurationUtil {

    private static final ReloadablePropertyUtil reloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-biz.properties");
    private static final ReloadablePropertyUtil mockReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "project-theia-biz.properties");

    private static final ReloadablePropertyUtil riskRejectReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-risk-reject.properties");

    private static final ReloadablePropertyUtil reloadableTheiaPropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia.properties");

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

    public static String getPropertyFromRiskRejectProperties(String key) {
        return riskRejectReloadablePropertyUtil.getProperty(key);
    }

    public static String getPropertyFromRiskRejectProperties(String key, String defaultValue) {
        return riskRejectReloadablePropertyUtil.getProperty(key, defaultValue);
    }

    public static String getTheiaProperty(String key) {
        return reloadableTheiaPropertyUtil.getProperty(key);
    }

    public static String getTheiaProperty(String key, String defaultValue) {
        return reloadableTheiaPropertyUtil.getProperty(key, defaultValue);
    }
}
