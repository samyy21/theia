package com.paytm.pgplus.theia.nativ.filter;

import com.google.common.io.ByteSource;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.*;
import static com.paytm.pgplus.stats.constant.StatsDConstants.*;

@Provider
public class ApiPerformanceLogger extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPerformanceLogger.class);

    private AWSStatsDUtils statsDUtils;

    private static Ff4jUtils ff4jUtils;

    private TheiaResponseGenerator theiaResponseGenerator;

    public void init() {
        try {
            if (statsDUtils == null || ff4jUtils == null || theiaResponseGenerator == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                statsDUtils = webApplicationContext.getBean(AWSStatsDUtils.class);
                ff4jUtils = webApplicationContext.getBean(Ff4jUtils.class);
                theiaResponseGenerator = webApplicationContext.getBean(TheiaResponseGenerator.class);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while initializing ", ex);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        init();

        if (ff4jUtils.isFeatureEnabled(ENABLE_API_PERFORMANCE_LOGGER, false)) {
            long startTime = System.currentTimeMillis();
            String contentType = request.getContentType();
            try {
                if (isFormPost(contentType)) {
                    filterChain.doFilter(request, response);
                    long executionTime = System.currentTimeMillis() - startTime;
                    logFormPostApiDetails(request, executionTime);
                } else {
                    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
                    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
                    filterChain.doFilter(requestWrapper, responseWrapper);
                    long executionTime = System.currentTimeMillis() - startTime;
                    logJsonPostApiDetails(request, response, executionTime, requestWrapper, responseWrapper);
                }
            } catch (Exception ex) {
                LOGGER.error("Error in ApiPerformanceLogger ", ex);
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private void logFormPostApiDetails(HttpServletRequest requestContext, long executionTime) {
        Map<String, String> statsDInputMap = new LinkedHashMap<>();
        statsDInputMap.put(EXECUTION_TIME, String.valueOf(executionTime));
        statsDInputMap.put(API_NAME, requestContext.getRequestURI());
        if (ObjectUtils.notEqual(ff4jUtils, null)) {
            String paymentMode = StringUtils.isNotBlank(requestContext
                    .getParameter(TheiaConstant.ExtraConstants.PAYMENT_MODE)) ? requestContext
                    .getParameter(TheiaConstant.ExtraConstants.PAYMENT_MODE) : requestContext
                    .getParameter(TheiaConstant.EventLogConstants.PAYMENT_MODE);
            theiaResponseGenerator.pushDataInStatsDMap(statsDInputMap, PAYMENT_MODE, paymentMode,
                    ENABLE_PUSH_PAYMENT_MODE_TO_STATSD);
        }
        if (ObjectUtils.notEqual(ff4jUtils, null)) {
            String requestType = StringUtils.isNotBlank(requestContext
                    .getParameter(TheiaConstant.EventLogConstants.REQUEST_TYPE)) ? requestContext
                    .getParameter(TheiaConstant.EventLogConstants.REQUEST_TYPE) : requestContext
                    .getParameter(TheiaConstant.ExtraConstants.REQUEST_TYPE);
            theiaResponseGenerator.pushDataInStatsDMap(statsDInputMap, REQUEST_TYPE, requestType,
                    ENABLE_PUSH_REQUEST_TYPE_TO_STATSD);
        }
        theiaResponseGenerator.pushMidToStatsDMap(requestContext, statsDInputMap);
        statsDUtils.pushFormPtcLatencyDetails(statsDInputMap);
    }

    private void logJsonPostApiDetails(HttpServletRequest requestContext, HttpServletResponse responseContext,
            long executionTime, ContentCachingRequestWrapper requestWrapper,
            ContentCachingResponseWrapper responseWrapper) throws IOException {
        Map<String, String> statsDInputMap = new LinkedHashMap<>();
        statsDInputMap.put(EXECUTION_TIME, String.valueOf(executionTime));
        statsDInputMap.put(API_NAME, requestContext.getRequestURI());
        String requestData = ByteSource.wrap(requestWrapper.getContentAsByteArray())
                .asCharSource(StandardCharsets.UTF_8).read();
        if (ObjectUtils.notEqual(ff4jUtils, null)) {
            JSONObject requestBody;
            String paymentMode = null;
            String requestType = null;
            try {
                JSONObject json = new JSONObject(requestData);
                requestBody = json.getJSONObject(TheiaConstant.DccConstants.BODY);
                paymentMode = requestBody.has(TheiaConstant.ExtraConstants.PAYMENT_MODE) ? requestBody
                        .getString(TheiaConstant.ExtraConstants.PAYMENT_MODE) : requestBody
                        .getString(TheiaConstant.EventLogConstants.PAYMENT_MODE);
                requestType = requestBody.has(TheiaConstant.ExtraConstants.REQUEST_TYPE) ? requestBody
                        .getString(TheiaConstant.ExtraConstants.REQUEST_TYPE) : requestBody
                        .getString(TheiaConstant.EventLogConstants.REQUEST_TYPE);
            } catch (JSONException e) {
                LOGGER.error("Invalid Json Request: {}", e.getMessage());
            }
            theiaResponseGenerator.pushDataInStatsDMap(statsDInputMap, PAYMENT_MODE, paymentMode,
                    ENABLE_PUSH_PAYMENT_MODE_TO_STATSD);
            theiaResponseGenerator.pushDataInStatsDMap(statsDInputMap, REQUEST_TYPE, requestType,
                    ENABLE_PUSH_REQUEST_TYPE_TO_STATSD);
        }
        theiaResponseGenerator.pushMidToStatsDMap(requestContext, statsDInputMap);
        String responseBody = getResponseBody(responseContext, responseWrapper);
        if (StringUtils.isNotBlank(responseBody)) {
            pushResultStatusInStatsDMap(responseBody, statsDInputMap);
        }
        statsDUtils.pushJsonPtcApiDetails(statsDInputMap);
    }

    private String getResponseBody(HttpServletResponse response, ContentCachingResponseWrapper responseWrapper) {
        String responseBody = StringUtils.EMPTY;
        try {
            if ((ObjectUtils.notEqual(response, null)) && (ObjectUtils.notEqual(responseWrapper, null))) {
                if ((response instanceof ResponseFacade)) {
                    responseBody = getResponseBody(responseWrapper.getContentAsByteArray(),
                            response.getCharacterEncoding());
                    if (StringUtils.isNotBlank(responseBody)) {
                        responseBody = responseBody.trim().replaceAll("\\s+", " ");
                    }
                } else {
                    responseBody = getResponseBody(responseWrapper.getContentAsByteArray(),
                            response.getCharacterEncoding());
                }
                responseWrapper.copyBodyToResponse();
            } else {
                LOGGER.error("Response is null");
            }
        } catch (Exception ex) {
            LOGGER.error("Error in logging Response Payload ", ex);
        }
        return responseBody;
    }

    private String getResponseBody(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (Exception e) {
            LOGGER.error("Exception while creating response body ", e);
        }
        return StringUtils.EMPTY;
    }

    private boolean isFormPost(String contentType) {
        return StringUtils.isNotBlank(contentType) && contentType.contains("application/x-www-form-urlencoded");
    }

    private void pushResultStatusInStatsDMap(String responseBody, Map<String, String> statsDInputMap) {
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject body = json.getJSONObject(TheiaConstant.DccConstants.BODY);
            JSONObject resultInfo = body.getJSONObject(TheiaConstant.ExtraConstants.RESULT_INFO);
            String resultStatus = resultInfo.has(TheiaConstant.ExtraConstants.RESULT_STATUS) ? resultInfo
                    .getString(TheiaConstant.ExtraConstants.RESULT_STATUS) : null;
            if (StringUtils.isNotBlank(resultStatus)) {
                statsDInputMap.put(RESULT_STATUS, resultStatus);
            }
        } catch (JSONException e) {
            LOGGER.error("Invalid Json Request: {}", e.getMessage());
        }
    }
}
