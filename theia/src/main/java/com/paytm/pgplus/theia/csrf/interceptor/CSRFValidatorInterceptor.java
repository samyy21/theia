package com.paytm.pgplus.theia.csrf.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.paytm.pgplus.common.statistics.StatisticsLogger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.csrf.CSRFConfiguration;
import com.paytm.pgplus.theia.csrf.CSRFInvalidException;
import com.paytm.pgplus.theia.csrf.CSRFToken;
import com.paytm.pgplus.theia.csrf.CSRFTokenManager;
import com.paytm.pgplus.theia.csrf.RequestMatcher;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.ThemeInfo;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;

/**
 * Created by ankitsinghal, AmitDubey on 17/05/17.
 */
@Component
public class CSRFValidatorInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSRFValidatorInterceptor.class);

    private CSRFTokenManager csrfTokenManager;

    private RequestMatcher requestMatcher;

    private ITheiaSessionDataService sessionDataService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    public CSRFValidatorInterceptor(CSRFTokenManager csrfTokenManager, RequestMatcher requestMatcher,
            @Qualifier("theiaSessionDataService") ITheiaSessionDataService sessionDataService) {
        this.csrfTokenManager = csrfTokenManager;
        this.requestMatcher = requestMatcher;
        this.sessionDataService = sessionDataService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        // Enable validating CSRF only if the property is enabled.

        CSRFConfiguration configuration = CSRFConfiguration.getConfigurationByValue(ConfigurationUtil.getProperty(
                TheiaConstant.ExtraConstants.CSRF_VALIDATION_ENABLED_FLAG, CSRFConfiguration.DISABLED.getValue()));

        if (configuration == CSRFConfiguration.ENABLED) {
            CSRFToken tokenFromSession = null;
            CSRFToken tokenFromRequest = null;
            try {
                if (requestMatcher.matches(request)) {
                    if (!sessionDataService.isSessionExists(request)) {
                        throw new TheiaControllerException("Session Does not exist");
                    }
                    tokenFromSession = csrfTokenManager.loadTokenFromSession(request);
                    tokenFromRequest = csrfTokenManager.getTokenFromRequest(request);
                    if (null == tokenFromRequest || null == tokenFromSession
                            || !tokenFromSession.equals(tokenFromRequest)) {
                        throw new CSRFInvalidException(tokenFromRequest, tokenFromSession);
                    }
                } else {
                    LOGGER.debug("Not validating request for method:{}, URL: {} for CSRF.", request.getMethod(),
                            request.getRequestURL());
                }
            } catch (CSRFInvalidException cie) {
                // If CSRF validation is enabled, then only throw exception else
                // eat the exception silently in case of ONLY_LOGGING.
                if (configuration == CSRFConfiguration.ENABLED) {
                    StatisticsLogger.logForXflush(MDC.get("MID"), "Theia", null, "request", "Invalid CSRF token",
                            request.getRequestURL().toString());
                    LOGGER.error(
                            "Invalid CSRF Token detected. Token in request: {}, token in session: {} for request: {}",
                            tokenFromRequest, tokenFromSession, request.getRequestURL());
                    throw new CSRFInvalidException("Invalid CSRF Token detected");
                } else {
                    ThemeInfo themeInfo = sessionDataService.getThemeInfoFromSession(request);
                    LOGGER.error(
                            "Invalid CSRF Token detected. Token in request: {}, token in session: {} for request: {} with theme: {}",
                            tokenFromRequest, tokenFromSession, request.getRequestURL(), themeInfo);
                }
            }
        }
        return super.preHandle(request, response, handler);
    }

}
