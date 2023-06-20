package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.merchant.models.TheiaMerchantExtendedDataResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/*
 Helper class for caching Extend info inside payment request bean.
 */

@Service("merchantExtendInfoProvider")
public class MerchantExtendInfoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantExtendInfoProvider.class);

    @Autowired
    @Qualifier("preRedisCacheServiceImpl")
    private IPreRedisCacheService preRedisCacheServiceImpl;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    private String getKeyFromExtendInfo(PaymentRequestBean paymentRequestBean, String key) {
        if (paymentRequestBean == null) {
            LOGGER.error("Payment Request Bean is null.");
            return null;
        }
        try {
            TheiaMerchantExtendedDataResponse theiaMerchantExtendedDataResponse = JsonMapper.convertValue(
                    preRedisCacheServiceImpl.getMerchantExtendedDataByCache(paymentRequestBean.getMid()),
                    TheiaMerchantExtendedDataResponse.class);
            if (theiaMerchantExtendedDataResponse != null
                    && theiaMerchantExtendedDataResponse.getExtendedInfo() != null) {
                String value = theiaMerchantExtendedDataResponse.getExtendedInfo().get(key);
                LOGGER.debug("Value for key {} is {}.", key, value);
                return value;
            }
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        }
        return TheiaConstant.ExtraConstants.EMPTY_STRING;
    }

    public boolean isCallbackEnabled(PaymentRequestBean paymentRequestBean) {
        String isCallbackEnabled = getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.S2S_CALLBACK_ENABLED);
        if (TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.S2S_CALLBACK_ENABLED.equals(isCallbackEnabled)) {
            return true;
        }
        return false;
    }

    public boolean isPeonEnabled(PaymentRequestBean paymentRequestBean) {
        String isPeonable = getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.PEON_ENABLE);
        if (TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.PEON_ENABLE.equals(isPeonable)) {
            return true;
        }
        return false;
    }

    public String getMerchantKey(PaymentRequestBean paymentRequestBean) {
        return getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_KEY);
    }

    public String getMerchantKey(PaymentRequestBean paymentRequestBean, String clientId) {
        if (StringUtils.isNotBlank(clientId)) {
            return merchantExtendInfoUtils.getKeyFromExtendInfoFromClientId(paymentRequestBean.getMid(), clientId,
                    TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_KEY);
        } else {
            return getMerchantKey(paymentRequestBean);
        }
    }

    public String getMerchantName(PaymentRequestBean paymentRequestBean) {
        return getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_NAME);
    }

    public int getNumberOfRetries(PaymentRequestBean paymentRequestBean) {
        String value = getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.NUMBER_OF_RETRY);
        if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    public String getEntityIDCorrespodingToMerchant(PaymentRequestBean paymentRequestBean) {
        return getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ENTITY_ID);
    }

    public boolean isLoginViaOtpEnabled(PaymentRequestBean paymentRequestBean) {
        String value = getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.OTP_LOGIN_ENABLED);
        return TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoValues.OTP_LOGIN_ENABLED.equals(value);
    }

    public String getMerchantPrivateKey(PaymentRequestBean paymentRequestBean) {
        return getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.MERCHANT_PRIVATE_KEY);
    }

    public boolean isMerchantOnPaytm(PaymentRequestBean paymentRequestBean) {
        String value = getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.ON_PAYTM);
        if (StringUtils.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return false;
    }

    public void checkPaymentRequest(PaymentRequestBean paymentRequestBean) {
        if (paymentRequestBean.getMerchantExtendedInfoResponse() == null) {
            // LOGGER.info("MerchantExtendInfoResponse is Null inside PaymentRequestBean.");
            MerchantExtendedInfoResponse merchantExtendedInfoResponse = preRedisCacheServiceImpl
                    .getMerchantExtendedDataWithoutCache(paymentRequestBean.getMid());
            LOGGER.info("Setting MerchantExtendInfoResponse to PaymentRequestBean.");
            paymentRequestBean.setMerchantExtendedInfoResponse(merchantExtendedInfoResponse);
        }
    }

    public String getMerchantKybId(PaymentRequestBean paymentRequestBean) {
        return getKeyFromExtendInfo(paymentRequestBean,
                TheiaConstant.MerchantExtendedInfo.MerchantExtendInfoKeys.KYB_ID);
    }
}
