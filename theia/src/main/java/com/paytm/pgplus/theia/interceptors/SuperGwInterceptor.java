package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.SUPERGW_VERSION;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REFERENCE_ID;

public class SuperGwInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwInterceptor.class);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        validateMandatoryParams(request);
        setMDC(request);
        return super.preHandle(request, response, handler);
    }

    private void validateMandatoryParams(HttpServletRequest request) throws Exception {
        String referenceId = request.getParameter(TheiaConstant.RequestParams.Native.REFERENCE_ID);
        if (StringUtils.isBlank(referenceId)) {
            LOGGER.error("Error - Mandatory parameters missing for super gateway api : {}", request.getRequestURI());
            throw new Exception("referenceId missing in query params");
        }
    }

    private void setMDC(HttpServletRequest request) {
        setMDCDefault(request);
    }

    private void setMDCDefault(HttpServletRequest request) {
        String mid = getMid(request);
        String referenceId = request.getParameter(TheiaConstant.RequestParams.Native.REFERENCE_ID);
        setMDCDefault(mid, referenceId);
    }

    private String getMid(HttpServletRequest request) {
        String mid = request.getParameter(MID);
        /** Hack to Support all merchant */
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID1);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.MID2);
        }
        if (StringUtils.isBlank(mid)) {
            mid = request.getParameter(TheiaConstant.RequestParams.Native.MID);
        }
        return mid;
    }

    private void setMDCDefault(final String mid, final String referenceId) {
        if (StringUtils.isNotBlank(mid)) {
            MDC.put(MID, mid);
        }
        if (StringUtils.isNotBlank(referenceId)) {
            MDC.put(REFERENCE_ID, referenceId);
        }
        MDC.put(VERSION, SUPERGW_VERSION);
    }

}
