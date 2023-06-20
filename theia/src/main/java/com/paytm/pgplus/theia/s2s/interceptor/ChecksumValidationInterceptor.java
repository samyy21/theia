package com.paytm.pgplus.theia.s2s.interceptor;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.s2s.enums.ResponseCode;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SResponseUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kartik
 * @date 06-Apr-2018
 */
@Component
public class ChecksumValidationInterceptor extends HandlerInterceptorAdapter {

    private static final Pattern bodyStartPatternForJson = Pattern.compile("\"body\"[ \n\r]*:[ \n\r]*\\{");
    private static final Pattern headStartPatternForJson = Pattern.compile("\"header\"[ \n\r]*:[ \n\r]*\\{");

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumValidationInterceptor.class);

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private PaymentS2SResponseUtil responseUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String mid = getMid(request);
        if (StringUtils.isBlank(mid)) {
            LOGGER.error("MID not received in request parameters");
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        JsonMapper.mapObjectToJson(responseUtil.generateResponse(ResponseCode.INVALID_PARAM)));
            } catch (Exception e) {
                LOGGER.error("Exception occurred while sending response ", e);
            }
            return false;
        }
        boolean isChecksumEnabled = merchantPreferenceService.isChecksumEnabled(mid);
        if (isChecksumEnabled) {
            // LOGGER.info("Checksum validation is enabled on merchant : {}",
            // mid);
            if (!validateSignature(request, mid)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        JsonMapper.mapObjectToJson(responseUtil.generateResponse(ResponseCode.INVALID_CHECKSUM)));
                return false;
            }
        } else {
            LOGGER.info("Checksum validation is disabled on merchant : {}", mid);
        }
        return super.preHandle(request, response, handler);
    }

    private boolean validateSignature(HttpServletRequest request, String mid) throws Exception {
        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        String body = validateJsonAndGetBodyText(content);
        JSONObject json = new JSONObject(content);
        JSONObject head = json.getJSONObject("header");
        String merchantSignature = head.getString("signature");
        String clientId = head.optString("clientId");
        return validateSignature(body, mid, merchantSignature, clientId);
    }

    private String getMid(HttpServletRequest request) {
        return request.getParameter("MID");
    }

    private boolean validateSignature(String body, String mid, String merchantSignature, String clientId)
            throws Exception {
        if (StringUtils.isBlank(merchantSignature)) {
            return false;
        }
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
            LOGGER.error(" Pattern validation failed suspect [pattern : {} : input received :{}]",
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
        return content.substring(start, end);
    }

    private String getMerchantKey(String mid, String clientId) {
        return merchantExtendInfoUtils.getMerchantKey(mid, clientId);
    }
}
