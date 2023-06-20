/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.IS_MOCK_REQUEST;

/**
 * @author ankit.singhal
 * @version $Id: ShadowTrafficUtil.java, v 0.1 2018-09-08 6:56 PM ankit.singhal
 *          Exp $$
 */
public class ShadowTrafficUtil {

    /**
     * Added private constructor so that class can't be instantiated
     */
    private ShadowTrafficUtil() {

    }

    /**
     * Utility method to clean the context from thread local/MDC if any
     */
    public static void unsetShadowContext() {
        ThreadLocalUtil.unsetForMockRequest();
        MDC.clear();
    }

    /**
     * Get the global shadow traffic preference from configuration
     *
     * @return True if the shadow configuration is enabled
     */
    static boolean isGlobalShadowSwitchEnabled() {
        String globalShadowSwitchEnabled = ConfigurationUtil.getProperty("shadow.traffic.supported", "false");
        return Boolean.TRUE.toString().equals(globalShadowSwitchEnabled);
    }

    /**
     * Get the shadow request attribute from request
     *
     * @param request
     *            Servlet request
     * @return True if the shadow attribute IS_MOCK_REQUEST = "true"
     */
    static boolean isShadowRequest(HttpServletRequest request) {
        String isMockRequest = request.getParameter(IS_MOCK_REQUEST);

        // If the parameter is present, then get its value as true/false.
        if (StringUtils.isNotBlank(isMockRequest)) {
            return Boolean.TRUE.toString().equals(isMockRequest);
        } else {
            // No parameter present, definitely actual request
            return false;
        }
    }

    static void clearMDC() {
        MDC.clear();
    }

    /**
     * Set the shadow context in both session and other contextual attributes
     *
     * @param request
     *            Incoming request
     */
    static void setSessionAndAttributesForShadowContext(HttpServletRequest request) {
        // setting the global session with a parameter
        // Adding null check since native doesn't use global tomcat session
        if (request.getSession() != null) {
            request.getSession().setAttribute(IS_MOCK_REQUEST, Boolean.TRUE.toString());
        }

        // set in MDC, and threadLocal
        setAttributesForShadowContext();
    }

    /**
     * Set the shadow context only in contextual attributes
     */
    public static void setAttributesForShadowContext() {
        // setting the MDC with a legitimate mock request
        MDC.put(IS_MOCK_REQUEST, Boolean.TRUE.toString());

        // setting the flag for mock request in thread local
        ThreadLocalUtil.setForMockRequest(Boolean.TRUE);
    }
}
