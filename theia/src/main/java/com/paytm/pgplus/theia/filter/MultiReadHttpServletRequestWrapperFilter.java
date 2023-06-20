package com.paytm.pgplus.theia.filter;

import com.paytm.pgplus.biz.utils.Ff4jUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MultiReadHttpServletRequestWrapperFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiReadHttpServletRequestWrapperFilter.class);

    private static Ff4jUtils ff4jUtils;

    public void init() {
        try {
            if (ff4jUtils == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while initializing ", ex);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        init();
        MultiReadHttpServletRequestWrapper multiReadRequest = new MultiReadHttpServletRequestWrapper(request);
        filterChain.doFilter(multiReadRequest, response);
    }
}
