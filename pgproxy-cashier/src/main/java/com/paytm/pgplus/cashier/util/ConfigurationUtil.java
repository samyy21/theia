package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.common.util.EagerLoadable;
import com.paytm.pgplus.common.util.ReloadablePropertyUtil;
import com.paytm.pgplus.common.util.ThreadLocalUtil;

@EagerLoadable
public class ConfigurationUtil {

    private static final ReloadablePropertyUtil reloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia-cashier.properties");

    private static final ReloadablePropertyUtil mockReloadablePropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "project-theia-cashier.properties");

    private static final ReloadablePropertyUtil reloadableTheiaPropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.PROPERTY_LOCATION + "project-theia.properties");

    private static final ReloadablePropertyUtil mockReloadableTheiaPropertyUtil = new ReloadablePropertyUtil(
            CommonConstants.MOCK_PROPERTY_LOCATION + "project-theia.properties");

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

    public static String getTheiaProperty(String key) {
        if (ThreadLocalUtil.getForMockRequest()) {
            return mockReloadableTheiaPropertyUtil.getProperty(key);
        }
        return reloadableTheiaPropertyUtil.getProperty(key);
    }

    public static String getTheiaProperty(String key, String defaultValue) {
        if (ThreadLocalUtil.getForMockRequest()) {
            return mockReloadableTheiaPropertyUtil.getProperty(key);
        }
        return reloadableTheiaPropertyUtil.getProperty(key, defaultValue);
    }
}
