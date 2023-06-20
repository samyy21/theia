package com.paytm.pgplus.theia.nativ.filter;

import com.google.common.io.ByteSource;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_ADDITIONAL_LOGGER_IN_FILTER;
import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_THEIA_REQUEST_RESPONSE_FILTER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.PARAMETER_TO_BE_MASKED_IN_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_PARAMETER_MASKING_IN_REQUEST;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.CARD_INFO;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.PAYTM_SSO_TOKEN;

public class LoggingFilter extends OncePerRequestFilter {

    private static Ff4jUtils ff4JUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    private static void loggingRequestPayload(String mid, String orderId, String pgpId, long time, String url,
            Type type, String payload) {
        LOGGER.info(
                "{ \"MID\": \"{}\", \"ORDER_ID\": \"{}\", \"PGP_ID\": \"{}\",  \"EVENT_TIME\" : \"{}\", \"FUNCTION\" : \"{}\", \"TYPE\" : \"{}\", \"PAYLOAD\" : {}}",
                mid, orderId, pgpId, time, url, type, payload);
    }

    private static void loggingResponsePayload(String mid, String orderId, String pgpId, long responseTime,
            long totalTimeTaken, String url, Type type, String payload) {
        LOGGER.info(
                "{ \"MID\": \"{}\", \"ORDER_ID\": \"{}\", \"PGP_ID\": \"{}\",  \"EVENT_TIME\" : \"{}\", \"TOTAL_TIME_TAKEN\" : \"{}\", \"FUNCTION\" : \"{}\", \"TYPE\" : \"{}\", \"PAYLOAD\" : {}}",
                mid, orderId, pgpId, responseTime, totalTimeTaken, url, type, payload);
    }

    public void init() {
        try {
            if (ff4JUtil == null) {
                ServletContext servletContext = getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils
                        .getWebApplicationContext(servletContext);
                ff4JUtil = webApplicationContext.getBean(Ff4jUtils.class);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while initializing ", ex);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        init();

        if (ObjectUtils.notEqual(ff4JUtil, null)) {
            String mid = StringUtils.isNotBlank(request.getParameter(MID)) ? request.getParameter(MID) : request
                    .getParameter(TheiaConstant.RequestParams.Native.MID);

            if (ff4JUtil.isFeatureEnabled(ENABLE_THEIA_REQUEST_RESPONSE_FILTER, false)) {
                String orderId = StringUtils.isNotBlank(request.getParameter(ORDER_ID)) ? request
                        .getParameter(ORDER_ID) : request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);
                String pgpId = request.getHeader(X_PGP_UNIQUE_ID);

                if (ff4JUtil.isFeatureEnabled(ENABLE_ADDITIONAL_LOGGER_IN_FILTER, false) && StringUtils.isBlank(mid)) {
                    LOGGER.info("PGP_ID {}  parameter map in logging filter {}", pgpId, request.getParameterMap());
                    LOGGER.info("PGP_ID {} query string in logging filter {} ", pgpId, request.getQueryString());
                }
                if (StringUtils.isBlank(mid)) {
                    mid = FilterUtils.getMidFromQueryParam(ff4JUtil, request);
                }
                if (StringUtils.isBlank(orderId)) {
                    orderId = FilterUtils.getOrderIdFromQueryParam(ff4JUtil, request);
                }

                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
                long requestTime = System.currentTimeMillis();
                String contentType = request.getContentType();
                try {
                    CustomContentCachingRequestWrapper requestWrapper = new CustomContentCachingRequestWrapper(request);
                    logRequestPayload(request, requestWrapper, mid, orderId, pgpId, requestTime);
                    if (isFormPost(contentType)) {
                        filterChain.doFilter(request, responseWrapper);
                    } else {
                        filterChain.doFilter(requestWrapper, responseWrapper);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error in LoggingFilter ", ex);
                    filterChain.doFilter(request, response);
                }
                long responseTime = System.currentTimeMillis();
                long totalTimeTaken = responseTime - requestTime;
                logResponsePayload(request, response, mid, orderId, pgpId, responseWrapper, responseTime,
                        totalTimeTaken);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void logResponsePayload(HttpServletRequest request, HttpServletResponse response, String mid,
            String orderId, String pgpId, ContentCachingResponseWrapper responseWrapper, long responseTime,
            long totalTimeTaken) {
        String responseBody = StringUtils.EMPTY;
        try {
            if ((ObjectUtils.notEqual(response, null)) && (ObjectUtils.notEqual(responseWrapper, null))) {
                if ((response instanceof ResponseFacade)) {
                    responseBody = getStringValue(responseWrapper.getContentAsByteArray(),
                            response.getCharacterEncoding());
                    if (StringUtils.isNotBlank(responseBody)) {
                        responseBody = responseBody.trim().replaceAll("\\s+", " ");
                    }
                } else {
                    responseBody = getStringValue(responseWrapper.getContentAsByteArray(),
                            response.getCharacterEncoding());
                }
                responseWrapper.copyBodyToResponse();
            } else {
                LOGGER.error("Response is null");
            }
        } catch (Exception ex) {
            LOGGER.error("Error in logging Response Payload ", ex);
        } finally {
            if (StringUtils.isNotBlank(responseBody)) {
                if (responseBody.contains(CARD_INFO)) {
                    responseBody = MaskingUtil.maskingStringExactKeys(responseBody, CARD_INFO, 0, 0);
                }
            }
            loggingResponsePayload(mid, orderId, pgpId, responseTime, totalTimeTaken, request.getRequestURI(),
                    Type.RESPONSE, responseBody);
        }
    }

    private void logRequestPayload(HttpServletRequest request, CustomContentCachingRequestWrapper requestWrapper,
            String mid, String orderId, String pgpId, long requestTime) {
        String requestBody = StringUtils.EMPTY;
        try {
            if (request instanceof RequestFacade) {
                requestBody = getStringValue(requestWrapper.getBody(), requestWrapper.getCharacterEncoding());
            } else if (request instanceof MultiReadHttpServletRequestWrapper) {
                requestBody = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            } else if (request instanceof ContentCachingRequestWrapper) {
                requestBody = ByteSource.wrap(requestWrapper.getContentAsByteArray())
                        .asCharSource(StandardCharsets.UTF_8).read();
            } else {
                LOGGER.info("Request Type is: {}", request.getClass());
            }
        } catch (Exception ex) {
            LOGGER.error("Error in logging Request Payload: ", ex);
        } finally {
            if (StringUtils.isNotBlank(requestBody)) {
                requestBody = removeSpecialCharacters(requestBody);
                requestBody = maskingSensitiveInformation(requestBody);
            }
            loggingRequestPayload(mid, orderId, pgpId, requestTime, request.getRequestURI(), Type.REQUEST, requestBody);
        }
    }

    private String removeSpecialCharacters(String requestBody) {
        requestBody = requestBody.trim().replaceAll("\\s+", " ");
        requestBody = requestBody.replaceAll("\\+", "");
        requestBody = requestBody.replaceAll("=", "");
        requestBody = requestBody.replaceAll("\\\\/", "");
        return requestBody;
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        }
        return StringUtils.EMPTY;
    }

    private String maskingSensitiveInformation(String requestBody) {

        requestBody = maskRequestBody(requestBody);
        if (requestBody.contains(PAYTM_SSO_TOKEN)) {
            requestBody = MaskingUtil.maskingStringExactKeys(requestBody, PAYTM_SSO_TOKEN, 6, 4);
        }
        if (requestBody.contains(SSO_TOKEN)) {
            requestBody = MaskingUtil.maskingStringExactKeys(requestBody, SSO_TOKEN, 6, 4);
        }
        return requestBody;
    }

    private boolean isFormPost(String contentType) {
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }

    private String maskRequestBody(String requestBody) {
        String paramaterToBeMasked = ConfigurationUtil.getProperty(PARAMETER_TO_BE_MASKED_IN_REQUEST, "");
        String parameterToBeMaskedInFf4jProperty = ff4JUtil.getPropertyAsStringWithDefault(
                THEIA_PARAMETER_MASKING_IN_REQUEST, "");
        if (StringUtils.isNotBlank(parameterToBeMaskedInFf4jProperty)) {
            paramaterToBeMasked = paramaterToBeMasked + "," + parameterToBeMaskedInFf4jProperty;
        }
        if (StringUtils.isNotBlank(paramaterToBeMasked)) {
            List<String> parameterListToBeMasked = new ArrayList<>(Arrays.asList(paramaterToBeMasked.split(",")));
            for (String maskingParameter : parameterListToBeMasked) {
                if (requestBody.contains(maskingParameter)) {
                    requestBody = MaskingUtil.maskingStringExactKeys(requestBody, maskingParameter, 0, 0);
                }
            }

        }
        return requestBody;
    }
}
