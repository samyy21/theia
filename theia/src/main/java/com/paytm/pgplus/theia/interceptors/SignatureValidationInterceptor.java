package com.paytm.pgplus.theia.interceptors;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.JWTValidationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class SignatureValidationInterceptor extends HandlerInterceptorAdapter {

    private static final Pattern bodyStartPatternForJson = Pattern.compile("\"body\"[ \n\r]*:[ \n\r]*\\{");
    private static final Pattern headStartPatternForJson = Pattern.compile("\"head\"[ \n\r]*:[ \n\r]*\\{");

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureValidationInterceptor.class);

    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private JWTValidationUtil jwtValidationUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Handling CORS preflight request
        if (StringUtils.equalsIgnoreCase(TheiaConstant.ExtraConstants.REQUEST_METHOD_OPTIONS, request.getMethod())) {
            return true;
        }
        String mid = getMid(request);

        boolean isChecksumEnabled = merchantPreferenceService.isChecksumEnabled(mid);

        if (TokenType.JWT.getType().equals(getTokenType(request))) {
            validateJWT(request);
        } else if (isChecksumEnabled) {
            LOGGER.debug("Checksum validation is enabled");
            validateSignature(request, mid);
        } else {
            LOGGER.debug("Checksum validation is disabled");
        }
        return super.preHandle(request, response, handler);
    }

    private void validateJWT(HttpServletRequest request) {
        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        JSONObject json = new JSONObject(content);
        LOGGER.info("Token Type JWT Recieved, skipping Checksum Validation");
        if (!jwtValidationUtil.validateJWT(json)) {
            LOGGER.info("JWT Token Validation Failed");
            throw RequestValidationException.getException(ResultCode.TOKEN_VALIDATION_EXCEPTION);
        }
    }

    private String getTokenType(HttpServletRequest request) {
        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        JSONObject json = new JSONObject(content);
        JSONObject head = json.getJSONObject("head");
        return head.optString("tokenType");
    }

    private void validateSignature(HttpServletRequest request, String mid) throws Exception {
        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        String body = validateJsonAndGetBodyText(content);
        JSONObject json = new JSONObject(content);
        JSONObject head = json.getJSONObject("head");
        String clientId = head.optString("clientId");
        String merchantSignature = head.getString("signature");
        String requestType = json.getJSONObject("body").optString(TheiaConstant.ExtraConstants.REQUEST_TYPE);
        if (StringUtils.isNotBlank(requestType) && TheiaConstant.RequestTypes.NATIVE_MF.equals(requestType)) {
            if (!json.getJSONObject("body").has(TheiaConstant.ExtraConstants.AggMID)) {
                LOGGER.error("Request Type is NATIVE_MF and aggMid is Blank For Mutual Fund");
                throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
            }
        }
        if (json.getJSONObject("body").has(TheiaConstant.ExtraConstants.AggMID)) {
            String aggMid = getAggMid(json);
            if (StringUtils.isNotBlank(aggMid)) {
                mid = aggMid;
            }
        }
        boolean isValidated = validateSignature(body, mid, merchantSignature, clientId);
        if (!isValidated) {
            throw RequestValidationException.getException(ResultCode.INVALID_CHECKSUM);
        }
    }

    private String getAggMid(JSONObject json) {
        Object aggMidVal = json.getJSONObject("body").get(TheiaConstant.ExtraConstants.AggMID);
        if (aggMidVal instanceof String) {
            return (String) aggMidVal;
        }
        return null;
    }

    private String getMid(HttpServletRequest request) {
        return request.getParameter("mid");
    }

    private boolean validateSignature(String body, String mid, String merchantSignature, String clientId)
            throws Exception {
        CheckSumInput checkSumInput = new CheckSumInput();
        checkSumInput.setMerchantChecksumHash(merchantSignature);
        checkSumInput.setPaytmChecksumHash(body.concat("|"));
        checkSumInput.setMerchantKey(getMerchantKey(mid, clientId));
        LOGGER.debug("CheckSumInput body:{}, merchantSignature:{}", checkSumInput.getPaytmChecksumHash(),
                checkSumInput.getMerchantChecksumHash());
        return ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
    }

    private String validateJsonAndGetBodyText(String content) {
        Matcher headMatcher = headStartPatternForJson.matcher(content);
        if (!headMatcher.find()) {
            LOGGER.error(" Pattern validation failed suspect [pattern : {} : input received :{}]",
                    headStartPatternForJson, content);
            throw new RuntimeException("Pattern not found :" + headStartPatternForJson);
        }
        Matcher bodyMatcher = bodyStartPatternForJson.matcher(content);
        if (!bodyMatcher.find()) {
            LOGGER.error(" Pattern validation failed suspect [matcher : {} : input received :{}]",
                    bodyStartPatternForJson, content);
            throw new RuntimeException("Pattern not found : " + bodyStartPatternForJson);
        }
        int start = bodyMatcher.end() - 1;
        int end = 0;
        if (bodyMatcher.start() < headMatcher.start()) { // body is after head
            for (int i = headMatcher.start() - 1; i >= 0; i--) {
                if (content.charAt(i) == '}') {
                    end = i + 1;
                    break;
                }
            }
        } else { // body is after head
            int times = 0;
            for (int i = content.length() - 1; i > 0; i--) {
                if (content.charAt(i) == '}') {
                    times++;
                }
                if (times == 2) {
                    end = i + 1;
                    break;
                }
            }
        }
        String body = content.substring(start, end);
        return body;
    }

    private String getMerchantKey(String mid, String clientId) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }
}
