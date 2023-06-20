package com.paytm.pgplus.theia.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.xss.XSSRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XSSFilter implements Filter {
    private static final Logger XSS_LOGGER = LoggerFactory.getLogger("XSS_LOGGER");

    private static final String XSS_VALIDATION = "XSS_VALIDATION";
    private static final String XSS_VALIDATION_N = "N";
    private static final String XSS_VALIDATION_Y = "Y";
    private static final String XSS_VALIDATION_THROW_EXCEPTION = "XSS_VALIDATION_THROW_EXCEPTION";
    private static final String HEALTH_CHECK = "healthcheck";

    private static Ff4jUtils ff4jUtils;

    /**
     * Default constructor.
     */
    public XSSFilter() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        XSSRequestWrapper reqWrapper = new XSSRequestWrapper((HttpServletRequest) request);
        if (!reqWrapper.getRequestURI().contains(HEALTH_CHECK)) {
            String xssValidation = ConfigurationUtil.getProperty(XSS_VALIDATION, XSS_VALIDATION_N);
            if (XSS_VALIDATION_Y.equals(xssValidation)) {
                try {
                    if (reqWrapper.isMatch()) {
                        XSS_LOGGER.warn("XSS_BAD_REQUEST_URL : {}", reqWrapper.getRequestURI());
                        StatisticsLogger.logForXflush(reqWrapper.getParameter("MID"), "Thiea", null, "request",
                                "XSS_BAD_REQUEST", reqWrapper.getRequestURI());

                        String xssThrowException = ConfigurationUtil.getProperty(XSS_VALIDATION_THROW_EXCEPTION,
                                XSS_VALIDATION_N);
                        if (XSS_VALIDATION_Y.equals(xssThrowException)) {
                            res.reset();
                            res.sendError(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }
                    }
                } catch (Exception ex) {
                    XSS_LOGGER.error("XSS Exception catched {} for Request{}", ex, reqWrapper.getRequestURI());
                } catch (Throwable t) {
                    XSS_LOGGER.error("XSS Error catched {} for Request{}", t, reqWrapper.getRequestURI());
                    if (!(t instanceof StackOverflowError)) {
                        throw t;
                    }
                }
            }
        }

        chain.doFilter(request, res);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
        try {
            if (ff4jUtils == null) {
                ServletContext servletContext = fConfig.getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            XSS_LOGGER.error("Error while initializing ", ex);
        }
    }

}