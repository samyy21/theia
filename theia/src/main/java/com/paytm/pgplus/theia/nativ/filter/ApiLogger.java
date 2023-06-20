package com.paytm.pgplus.theia.nativ.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.paytm.pgplus.stats.constant.StatsDConstants.*;

@Provider
public class ApiLogger implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLogger.class);

    private static final String HEALTH_CHECK = "healthcheck";

    private AWSStatsDUtils statsDUtils;

    private static Ff4jUtils ff4jUtils;

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            if (statsDUtils == null) {
                ServletContext servletContext = filterConfig.getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                statsDUtils = webApplicationContext.getBean(AWSStatsDUtils.class);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Throwable ex) {
            LOGGER.error("Error in ApiLogger ", ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        try {
            long startTime = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            long executionTime = System.currentTimeMillis() - startTime;

            if (request instanceof HttpServletRequest) {
                String path = ((HttpServletRequest) request).getRequestURI();
                if (!StringUtils.startsWith(path, HEALTH_CHECK)) {
                    logResponseTimeEvent((HttpServletRequest) request, (HttpServletResponse) response, executionTime);
                }
            }
        } catch (Throwable ex) {
            LOGGER.error("Error in ApiLogger ", ex);
        }
    }

    @Override
    public void destroy() {
    }

    private void logResponseTimeEvent(HttpServletRequest requestContext, HttpServletResponse responseContext,
            long executionTime) {
        Map<String, String> statsDInputMap = new LinkedHashMap<>(4);
        statsDInputMap.put(EXECUTION_TIME, String.valueOf(executionTime));
        statsDInputMap.put(HTTP_METHOD, requestContext.getMethod());
        statsDInputMap.put(API_NAME, requestContext.getRequestURI());
        statsDInputMap.put(HTTP_RESPONSE_CODES, String.valueOf(responseContext.getStatus()));

        statsDUtils.pushApiResponseTimeEventLog(statsDInputMap);
    }
}
