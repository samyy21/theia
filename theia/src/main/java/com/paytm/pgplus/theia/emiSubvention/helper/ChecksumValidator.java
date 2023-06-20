package com.paytm.pgplus.theia.emiSubvention.helper;

import com.google.gson.JsonObject;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.filter.MultiReadHttpServletRequestWrapper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component("checksumValidator")
public class ChecksumValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumValidator.class);

    private static final Pattern bodyStartPatternForJson = Pattern.compile("\"body\"[ \n\r]*:[ \n\r]*\\{");
    private static final Pattern headStartPatternForJson = Pattern.compile("\"head\"[ \n\r]*:[ \n\r]*\\{");

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;
    @Autowired
    private IMerchantPreferenceService merchantPreferenceService;

    public boolean validateChecksum(String body, String mid, String merchantSignature) {
        boolean isChecksumEnabled = merchantPreferenceService.isChecksumEnabled(mid);
        if (isChecksumEnabled) {
            LOGGER.debug("Checksum validation is enabled");
            return validateSignature(body, mid, merchantSignature);
        }
        return true;
    }

    public boolean validateSignature(String body, String mid, String merchantSignature) {
        if (StringUtils.isBlank(merchantSignature)) {
            return false;
        }
        boolean isValidChecksum = false;
        CheckSumInput checkSumInput = new CheckSumInput();
        checkSumInput.setMerchantChecksumHash(merchantSignature);
        checkSumInput.setPaytmChecksumHash(body.concat("|"));
        checkSumInput.setMerchantKey(getMerchantKey(mid));

        JSONObject jsonObject = new JSONObject(checkSumInput.getPaytmChecksumHash());
        if (jsonObject.has("paymentDetails")) {
            jsonObject.remove("paymentDetails");
        }
        LOGGER.info("CheckSumInput body:{}, merchantSignature:{}", jsonObject.toString(),
                checkSumInput.getMerchantChecksumHash());
        try {
            isValidChecksum = ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
        } catch (Exception e) {
            LOGGER.error("Error in validating checksum {}", e);
        }
        return isValidChecksum;

    }

    private String getMerchantKey(String mid) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }

    public String getBodyString() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String content = ((MultiReadHttpServletRequestWrapper) request).getMessageBody();
        String body = validateJsonAndGetBodyText(content);
        return body;
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
}
