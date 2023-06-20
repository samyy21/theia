package com.paytm.pgplus.theia.paymentoffer.helper;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.common.signature.wrapper.SignatureUtilWrapper;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.nativ.IOAuthHelper;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class TokenValidationHelper {

    private static final Pattern bodyStartPatternForJson = Pattern.compile("\"body\"[ \n\r]*:[ \n\r]*\\{");
    private static final Pattern headStartPatternForJson = Pattern.compile("\"head\"[ \n\r]*:[ \n\r]*\\{");

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidationHelper.class);

    @Autowired
    private IOAuthHelper authHelper;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    public <Res, Req> Res validateToken(String token, TokenType tokenType, Req reqBody, String mid) {
        if (tokenType == TokenType.SSO) {
            return (Res) validateSSOToken(token, mid);

        } else if (tokenType == TokenType.CHECKSUM) {
            return validateChecksum(token, reqBody, mid);
        } else {
            LOGGER.info("tokenType = {} is not supported", tokenType);
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    public <Res, Req> Res validateChecksum(String token, Req reqBody, String mid) {
        String reqBodyStr = null;
        try {
            reqBodyStr = JsonMapper.mapObjectToJson(reqBody);
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        if (reqBodyStr == null) {
            throw BaseException.getException();
        } else {
            try {
                if (validateSignature(reqBodyStr, mid, token)) {
                    LOGGER.info("Checksum validated successfully");
                    return null;
                }
            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            throw RequestValidationException.getException(ResultCode.INVALID_CHECKSUM);
        }
    }

    public UserDetailsBiz validateSSOToken(String ssoToken, String mid) {
        UserDetailsBiz userDetailsBiz = authHelper.validateSSOToken(ssoToken, mid);
        if ((userDetailsBiz == null || StringUtils.isBlank(userDetailsBiz.getUserId()))) {
            throw BaseException.getException();
        }
        return userDetailsBiz;
    }

    public boolean validateSignature(String body, String mid, String merchantSignature) throws Exception {
        if (StringUtils.isBlank(merchantSignature)) {
            return false;
        }
        CheckSumInput checkSumInput = new CheckSumInput();
        checkSumInput.setMerchantChecksumHash(merchantSignature);
        checkSumInput.setPaytmChecksumHash(body.concat("|"));
        checkSumInput.setMerchantKey(getMerchantKey(mid));
        return ValidateChecksum.getInstance().verifyCheckSum(checkSumInput);
    }

    private String getMerchantKey(String mid) {
        String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid);
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }
        return merchantKey;
    }

    public String validateJsonAndGetBodyText(String content) {
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
