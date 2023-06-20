package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

public class TokenTypeChecksumValidationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenTypeChecksumValidationInterceptor.class);

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS,
                request.getMethod())) {
            return true;
        }

        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        JSONObject json = new JSONObject(content);
        JSONObject head = json.getJSONObject("head");
        String tokenType = head.optString("tokenType");

        if (TokenType.CHECKSUM.getType().equals(tokenType)) {
            String body = tokenValidationHelper.validateJsonAndGetBodyText(content);
            String mid = getMid(request, body);
            String token = head.getString("token");
            boolean isValidated = tokenValidationHelper.validateSignature(body, mid, token);
            if (!isValidated) {
                throw RequestValidationException.getException(ResultCode.INVALID_CHECKSUM);
            }
        }

        return super.preHandle(request, response, handler);

    }

    private String getMid(HttpServletRequest request, String body) {
        String mid = request.getParameter("mid");
        if (StringUtils.isBlank(mid)) {
            JSONObject bodyJson = new JSONObject(body);
            mid = bodyJson.optString("mid");
        }
        return mid;
    }

}
