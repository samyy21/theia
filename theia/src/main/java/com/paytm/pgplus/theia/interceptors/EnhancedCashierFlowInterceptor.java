package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.EnhancedCashierFlow;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EnhancedCashierFlowInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedCashierFlowInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        if (request instanceof MultiReadHttpServletRequestWrapper) {
            String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();

            if (content.contains(TheiaConstant.DccConstants.PAYMENT_CALL_DCC)) {
                request.setAttribute(TheiaConstant.DccConstants.PAYMENT_CALL_DCC, true);
            }

            if (content.contains(EnhancedCashierFlow.WORKFLOW)) {
                try {
                    JSONObject json = new JSONObject(content);
                    JSONObject head = json.getJSONObject("head");

                    String workFlow = (String) head.get(EnhancedCashierFlow.WORKFLOW);
                    if (StringUtils.equals(workFlow, EnhancedCashierFlow.ENHANCED_CASHIER_FLOW)) {
                        request.setAttribute(EnhancedCashierFlow.WORKFLOW, EnhancedCashierFlow.ENHANCED_CASHIER_FLOW);
                    }
                } catch (JSONException e) {
                    LOGGER.info("inside EnhancedCashierFlowInterceptor not enhanced cashier request");
                }
            }
        }

        return super.preHandle(request, response, handler);
    }

}
