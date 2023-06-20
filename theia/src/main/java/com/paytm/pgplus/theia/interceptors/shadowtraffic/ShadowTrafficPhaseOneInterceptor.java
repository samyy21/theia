package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.cache.impl.MerchantPreferenceServiceImpl;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.StagingRequestException;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequest;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.StagingParamValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.FAST_FORWARD_URL;

/**
 * An interceptor to distinguish between the real and mock requests (basically
 * context setting)
 */
@Component
public class ShadowTrafficPhaseOneInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShadowTrafficPhaseOneInterceptor.class);

    /**
     * Used to access the functions of class MerchantPreferenceServiceImpl
     */
    @Autowired
    protected MerchantPreferenceServiceImpl merchantPreferenceService;

    /**
     * Used for OOPS page in case of configuration mismatch
     */
    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    protected ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private StagingParamValidator stagingParamValidator;

    /**
     * @see HandlerInterceptorAdapter#preHandle(HttpServletRequest,
     *      HttpServletResponse, Object)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }

        ShadowTrafficUtil.clearMDC();

        String mid = request.getParameter(TheiaConstant.RequestParams.MID);
        final boolean isMockRequest = ShadowTrafficUtil.isShadowRequest(request);

        if (StringUtils.isNotBlank(request.getRequestURI())
                && !request.getRequestURI().equalsIgnoreCase(FAST_FORWARD_URL)
                && stagingParamValidator.isCustomPageEnabledForURL(request.getServerName())
                && !stagingParamValidator.midOrderIDCheck(request)) {
            throw new StagingRequestException("mid/orderid wrong");
        }

        // Check if mid passed in query param and in body is same for mock
        // request to prevent fund-loss
        if (isMockRequest) {
            String uri = request.getRequestURI();
            if (uri.equalsIgnoreCase(FAST_FORWARD_URL)) {
                String requestBody = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
                FastForwardRequest fastForwardRequest = JsonMapper.mapJsonToObject(requestBody,
                        FastForwardRequest.class);
                // mid in request body
                mid = Optional.ofNullable(fastForwardRequest).map(FastForwardRequest::getHead).map(BaseHeader::getMid)
                        .orElse(null);
            }
        }

        // If the MID is present in request with "MID" as key, try processing
        // our interceptor. Else, passthrough to lower interceptors.
        if (StringUtils.isNotBlank(mid)) {
            // the method to check whether a legitimate mock request or not
            if (!isRequestAllowed(request, mid)) {
                response.sendRedirect(theiaViewResolverService.returnOOPSPage(request));
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    /**
     * @see HandlerInterceptorAdapter#afterCompletion(HttpServletRequest,
     *      HttpServletResponse, Object, Exception)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ShadowTrafficUtil.unsetShadowContext();
    }

    /**
     * Checks if the request received is a legitimate request based on MID
     * configuration and Request parameter. If its not legitimate, either due to
     * configuration mismatch or global switch, fail it.
     *
     * @param request
     *            Request from merchant
     * @param mid
     *            MerchantId
     * @return True if the request can be processed properly
     */
    boolean isRequestAllowed(HttpServletRequest request, String mid) {

        // Get the merchant preference for Mock
        boolean isMockMerchant = merchantPreferenceService.isMockMerchant(mid);

        // Switch for performance traffic support
        boolean isGlobalShadowSwitchEnabled = ShadowTrafficUtil.isGlobalShadowSwitchEnabled();

        // Get the request parameter, if its mock or not
        boolean isMockRequest = ShadowTrafficUtil.isShadowRequest(request);

        // If the global shadow switch is enabled, then only process the shadow
        // traffic.
        if (isGlobalShadowSwitchEnabled) {
            if (isMockRequest && isMockMerchant) {
                // Legitimate "Mock" request
                LOGGER.info("Received legitimate mock traffic. Setting appropriate context!");
                ShadowTrafficUtil.setSessionAndAttributesForShadowContext(request);
            } else if (!isMockMerchant && !isMockRequest) {
                // Legitimate "Real" request. Do nothing
                LOGGER.debug("Received real traffic! Nothing to do special!");
            } else {
                LOGGER.error(
                        "There is a mismatch in configuration for shadow traffic. Mid: {}, isMockRequest: {}, isMockMerchant: {}",
                        mid, isMockRequest, isMockMerchant);
                return false;
            }
        } else {
            /*
             * If the global shadow switch is false, following two conditions
             * needs to be taken care of: 1. Fail "all" the shadow traffic, but
             * continue to allow "Real" traffic. 2. Fail if any request is
             * received from mock merchant.
             */
            if (isMockRequest || isMockMerchant) {
                LOGGER.error(
                        "Received unexpected request, but the global switch is off. Failing request. isMockRequest: {}, isMockMerchant: {}",
                        isMockRequest, isMockMerchant);
                return false;
            }
        }
        // Validations passed. Pass through the request to process forward.
        return true;
    }

}