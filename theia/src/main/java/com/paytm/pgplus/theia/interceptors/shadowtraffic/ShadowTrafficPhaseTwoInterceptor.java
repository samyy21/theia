package com.paytm.pgplus.theia.interceptors.shadowtraffic;

import com.paytm.pgplus.cache.model.TransactionInfo;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.IS_MOCK_REQUEST;

/**
 * Phase 2 interceptor for 2nd phase payment processing. Get the previous set
 * contexts in session
 */
@Component
public class ShadowTrafficPhaseTwoInterceptor extends HandlerInterceptorAdapter {

    /**
     * @see HandlerInterceptorAdapter#preHandle(HttpServletRequest,
     *      HttpServletResponse, Object)
     */
    public static final String TRANSACTION_STATUS = "transactionStatus";

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String isMockRequest = null;
        if (uri.contains(TRANSACTION_STATUS)) {
            String transId = request.getParameter(TheiaConstant.ExtraConstants.TRANS_ID);
            TransactionInfo transInfo = transactionCacheUtils.getTransInfoFromCache(transId);
            if (transInfo != null) {
                isMockRequest = transInfo.getIsMockRequest();
            }
        } else if (request.getSession() != null) {
            isMockRequest = (String) request.getSession().getAttribute(IS_MOCK_REQUEST);
        }
        if (Boolean.TRUE.toString().equals(isMockRequest)) {
            ShadowTrafficUtil.setAttributesForShadowContext();
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

}
