package com.paytm.pgplus.theia.csrf.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.csrf.CSRFConfiguration;
import com.paytm.pgplus.theia.csrf.CSRFTokenManager;

/**
 * Created by ankitsinghal on 17/05/17.
 */
public class CSRFGeneratorInterceptor extends HandlerInterceptorAdapter {

    private CSRFTokenManager csrfTokenManager;

    @Autowired
    public CSRFGeneratorInterceptor(CSRFTokenManager csrfTokenManager) {
        this.csrfTokenManager = csrfTokenManager;
    }

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        // Enable generating CSRF only if the property is enabled/only logging.

        CSRFConfiguration configuration = CSRFConfiguration.getConfigurationByValue(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.CSRF_VALIDATION_ENABLED_FLAG, CSRFConfiguration.DISABLED.getValue()));

        if (configuration == CSRFConfiguration.ENABLED || configuration == CSRFConfiguration.ONLY_LOGGING) {
            // Add CSRF token to the session.
            csrfTokenManager.generateTokenForSession(request);
        }

        return super.preHandle(request, response, handler);
    }
}
