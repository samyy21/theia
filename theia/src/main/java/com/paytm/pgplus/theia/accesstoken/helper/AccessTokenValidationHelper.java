package com.paytm.pgplus.theia.accesstoken.helper;

import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.checksum.model.CheckSumInput;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.exception.BaseException;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequestBody;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class AccessTokenValidationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenValidationHelper.class);

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    public <Res> Res validateChecksum(String token, CreateAccessTokenRequestBody body, String reqStr, String mid) {
        String reqBodyStr = null;
        try {
            reqBodyStr = JsonMapper.mapObjectToJson(body);
        } catch (FacadeCheckedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        if (StringUtils.isBlank(reqStr) || StringUtils.isBlank(reqBodyStr)) {
            throw BaseException.getException();
        } else {
            String reqBody = tokenValidationHelper.validateJsonAndGetBodyText(reqStr);
            ResultCode resultCode = ResultCode.INVALID_CHECKSUM;
            String resultCodeString = null;
            try {
                if (validateSignature(reqBodyStr, mid, token)) {
                    LOGGER.info("Checksum validated successfully");
                    return null;
                }
                if (validateSignature(reqBody, mid, token)) {
                    LOGGER.info("Checksum validated successfully");
                    return null;
                }
            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                if (e instanceof BaseException && null != ((RequestValidationException) e).getResultInfo()
                        && StringUtils.isNotBlank(((RequestValidationException) e).getResultInfo().getResultCode())) {

                    resultCodeString = ((RequestValidationException) e).getResultInfo().getResultCode();
                }
            }

            try {
                if (StringUtils.isNotBlank(resultCodeString)) {
                    resultCode = ResultCode.valueOf(resultCodeString);
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Unknown ResultCode encounterd: " + e.getMessage());
            }

            throw RequestValidationException.getException(resultCode);
        }
    }

    private boolean validateSignature(String body, String mid, String merchantSignature) throws Exception {
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
}
