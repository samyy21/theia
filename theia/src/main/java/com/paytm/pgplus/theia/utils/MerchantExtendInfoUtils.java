package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.theia.cache.IMerchantExtendedInfoDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MerchantExtendInfoUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantExtendInfoUtils.class);

    @Autowired
    @Qualifier("merchantExtendedInfoDataService")
    private IMerchantExtendedInfoDataService merchantExtendedInfoDataService;

    public String getKeyFromExtendInfo(String mid, String key) {

        try {
            TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = merchantExtendedInfoDataService
                    .getMerchantExtendedInfoData(mid);
            if (theiaMerchantExtendedDataResponse != null
                    && theiaMerchantExtendedDataResponse.getExtendedInfo() != null) {
                String value = theiaMerchantExtendedDataResponse.getExtendedInfo().get(key);
                return value;
            }
        } catch (Exception e) {
            // LOGGER.error("Exception ", e);
            LOGGER.error("Exception ", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        return TheiaConstant.ExtraConstants.EMPTY_STRING;
    }

    public String getKeyFromExtendInfoFromClientId(String mid, String clientId, String key) {
        try {
            TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = merchantExtendedInfoDataService
                    .getMerchantExtendedInfoDataFromClientId(mid, clientId);
            if (theiaMerchantExtendedDataResponse != null
                    && theiaMerchantExtendedDataResponse.getExtendedInfo() != null) {
                String value = theiaMerchantExtendedDataResponse.getExtendedInfo().get(key);
                return value;
            }
        } catch (Exception e) {
            // LOGGER.error("Exception ", e);
            LOGGER.error("Exception ", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
        return TheiaConstant.ExtraConstants.EMPTY_STRING;
    }

    public boolean isCallbackEnabled(String mid) {
        String isCallbackEnabled = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.S2S_CALLBACK_ENABLED);
        if (TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.S2S_CALLBACK_ENABLED.equals(isCallbackEnabled)) {
            return true;
        }
        return false;
    }

    public boolean isPeonEnabled(String mid) {
        String isPeonable = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.PEON_ENABLE);
        if (TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.PEON_ENABLE.equals(isPeonable)) {
            return true;
        }
        return false;
    }

    public String getMerchantKey(String mid) {
        return getKeyFromExtendInfo(mid, TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_KEY);
    }

    public String getMerchantKey(String mid, String clientId) {

        if (StringUtils.isNotBlank(clientId))
            return getKeyFromExtendInfoFromClientId(mid, clientId,
                    TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_KEY);
        else
            return getMerchantKey(mid);
    }

    public String getMerchantName(String mid) {
        return getKeyFromExtendInfo(mid, TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_NAME);
    }

    public int getNumberOfRetries(String mid) {
        String value = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.NUMBER_OF_RETRY);
        if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    public String getEntityIDCorrespodingToMerchant(String mid) {
        return getKeyFromExtendInfo(mid, TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ENTITY_ID);
    }

    public boolean isLoginViaOtpEnabled(String mid) {
        String value = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.OTP_LOGIN_ENABLED);
        return TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.OTP_LOGIN_ENABLED.equals(value);
    }

    public String getMerchantPrivateKey(String mid) {
        return getKeyFromExtendInfo(mid, TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_PRIVATE_KEY);
    }

    public boolean isMerchantOnPaytm(String mid) {
        String value = getKeyFromExtendInfo(mid, TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ON_PAYTM);
        if (StringUtils.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return false;
    }

    public boolean isMerchantActiveOrBlocked(String mid) {
        String activeValue = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_INACTIVE);
        String blockedValue = getKeyFromExtendInfo(mid,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_BLOCKED);
        return TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.MERCHANT_INACTIVE.equals(activeValue)
                || TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.MERCHANT_BLOCKED.equals(blockedValue);
    }

}
