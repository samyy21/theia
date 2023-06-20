package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.paymentoffer.model.request.ApplyPromoRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.KYC_FLOW;

/**
 * Shadow traffic phase 2 interceptor for Native flow
 */
public class NativeShadowTrafficPhaseTwoInterceptor extends ShadowTrafficPhaseTwoInterceptor {

    /**
     * @see ShadowTrafficPhaseTwoInterceptor#preHandle(HttpServletRequest,
     *      HttpServletResponse, Object)
     */
    @Autowired
    private NativeSessionUtil nativeSessionUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeShadowTrafficPhaseTwoInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        if ("YES".equalsIgnoreCase(request.getParameter(KYC_FLOW))) {
            return true;
        }
        if (Boolean.TRUE.toString().equals(request.getParameter(TheiaConstant.GvConsent.GV_CONSENT_FLOW))) {
            return true;
        }

        if (RiskConstants.TRUE.equals(request.getParameter(RiskConstants.RISK_VERIFIER_UI_KEY))) {
            return true;
        }
        String uri = request.getRequestURI();
        String txnToken = "";
        String accessToken = "";
        if (uri.equals(TheiaConstant.ExtraConstants.NATIV_FETCH_PAYMENTOPTIONS_URL)
                || TheiaConstant.ExtraConstants.NATIV_FETCH_PAYMENTOPTIONS_URL_V2.equals(uri)) {
            String requestBody = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            NativeCashierInfoRequest nativeCashierInfoRequest = JsonMapper.mapJsonToObject(requestBody,
                    NativeCashierInfoRequest.class);
            txnToken = nativeCashierInfoRequest.getHead().getTxnToken();
            if (StringUtils.isBlank(txnToken)) {
                accessToken = nativeCashierInfoRequest.getHead().getToken();
            }
        } else if (uri.equals(TheiaConstant.ExtraConstants.NATIV_PROCESS_TRANSACTION_URL)) {
            txnToken = request.getParameter("txnToken");
            if (txnToken == null) {
                String requestMessage = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
                // For enhanced nativ flow. Request comes as MultiRead in
                // enhanced native!
                try {
                    JsonNode headNode = ((JsonNode) JsonMapper.getParamFromJson(requestMessage, "head"));
                    JsonNode txnTokenNode = headNode != null ? headNode.findValue("txnToken") : null;
                    if (txnTokenNode != null) {
                        txnToken = txnTokenNode.asText();
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    LOGGER.info("Exception occurred while fetching required details from request : {}", requestMessage);
                    throw e;
                }
            }
        } else if (TheiaConstant.ExtraConstants.NATIVE_APP_INVOKE_URL.equals(uri)) {
            txnToken = request.getParameter("txnToken");
            if (StringUtils.isBlank(txnToken)) {
                return true;
            }
        } else if (TheiaConstant.ExtraConstants.APPLY_PROMO_URL_V2.equals(uri)) {
            String requestBody = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
            ApplyPromoRequest applyPromoRequest = JsonMapper.mapJsonToObject(requestBody, ApplyPromoRequest.class);
            accessToken = Optional.ofNullable(applyPromoRequest).map(ApplyPromoRequest::getHead)
                    .map(TokenRequestHeader::getToken).orElse(null);
        }
        if (isValueForMockRequestSetInSession(txnToken, accessToken)) {
            ShadowTrafficUtil.setAttributesForShadowContext();
        }
        return true;
    }

    private boolean isValueForMockRequestSetInSession(String txnToken, String accessToken) {
        String token = StringUtils.isNotBlank(txnToken) ? txnToken : accessToken;
        if (StringUtils.isBlank(token)) {
            LOGGER.info("txnToken/accessToken received as blank!");
            return false;
        }
        String isMockRequest = nativeSessionUtil.isMockRequest(token);
        return Boolean.TRUE.toString().equals(isMockRequest);
    }
}
