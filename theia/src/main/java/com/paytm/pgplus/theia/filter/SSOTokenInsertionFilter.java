package com.paytm.pgplus.theia.filter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.util.AllowedMidCustidPropertyUtil;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
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

import static com.paytm.pgplus.common.util.ClientConstants.KEY_HEAD;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.SSO_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.X_PGP_UNIQUE_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TOKEN_TYPE;

public class SSOTokenInsertionFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSOTokenInsertionFilter.class);

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
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            FilterChain filterChain) throws ServletException, IOException {
        init();
        if (StringUtils.contains(httpServletRequest.getContentType(), ContentType.APPLICATION_JSON.getMimeType())) {

            boolean isMidEligibleForLogging = false;
            String mid = httpServletRequest.getParameter(TheiaConstant.RequestParams.Native.MID);
            ResettableStreamHttpServletRequest wrappedRequest = ResettableStreamHttpServletRequest
                    .resettableStreamHttpServletRequest(httpServletRequest);
            LOGGER.info("Inside SSOTokenInsertionFilter for PGP_ID : {} ",
                    httpServletRequest.getHeader(X_PGP_UNIQUE_ID));
            String requestBody = IOUtils.toString(wrappedRequest.getReader());
            JsonObject json = new Gson().fromJson(requestBody, JsonObject.class);
            try {
                isMidEligibleForLogging = AllowedMidCustidPropertyUtil.isMidCustIdEligible(mid, CommonConstants.ALL,
                        "eligible.mids.for.logging.at.filter", CommonConstants.NONE, Boolean.FALSE);
            } catch (Exception e) {
                LOGGER.info("Exception occured while checking isMidEligibleForLogging, e : {}", e);
            }
            if (isMidEligibleForLogging) {
                LOGGER.info("Inside SSOTokenInsertionFilter, requestBody is : {}, json is : {} ", requestBody, json);
            }
            try {
                JsonObject head = json.getAsJsonObject(KEY_HEAD);
                if (head.has(TOKEN_TYPE)
                        && (head.get(TOKEN_TYPE).getAsString().equals(TokenType.SSO.getType()))
                        && (!head.has(TOKEN) || (head.has(TOKEN) && StringUtils.isBlank(head.get(TOKEN).getAsString())))) {
                    head.addProperty(TOKEN, httpServletRequest.getHeader(SSO_TOKEN));
                    json.add(KEY_HEAD, head);
                }
            } catch (Exception e) {
                LOGGER.error("Exception occurred while inserting SSO Token for PGP_ID : {} ",
                        httpServletRequest.getHeader(X_PGP_UNIQUE_ID));
            }
            wrappedRequest.resetRequestBody(json.toString());

            filterChain.doFilter(wrappedRequest, httpServletResponse);
        }

        else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }

    }
}
