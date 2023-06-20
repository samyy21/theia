package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.cache.model.MerchantPreferenceInfoResponse;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.model.IPreRedisCacheService;
import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.*;

@Service("merchantPreferenceProvider")
public class MerchantPreferenceProvider {

    @Autowired
    @Qualifier("preRedisCacheServiceImpl")
    private IPreRedisCacheService preRedisCacheServiceImpl;

    final static Logger LOGGER = LoggerFactory.getLogger(MerchantPreferenceProvider.class);

    public MerchantPreferenceStore parseResponse(MerchantPreferenceInfoResponse merchantPreferenceResponse) {
        return preRedisCacheServiceImpl.parseResponse(merchantPreferenceResponse);
    }

    private boolean isPreferenceEnabled(MerchantPreferenceStore preferenceStore, String preferenceName) {
        LOGGER.debug("Trying to get the preference {} from MerchantPreference {}.", preferenceName, preferenceStore);
        if (StringUtils.isNotBlank(preferenceName)) {
            if (preferenceStore != null) {
                MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                if (preference != null) {
                    return preference.isEnabled();
                }
            }
        }
        return false;
    }

    private String getPreferenceValue(MerchantPreferenceStore preferenceStore, final String preferenceName) {
        if (StringUtils.isNotBlank(preferenceName)) {
            if (preferenceStore != null) {
                MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                if (preference != null) {
                    return preference.getPreferenceValue();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public boolean isAutoLoginEnable(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, AUTO_CREATE_USER);
    }

    public boolean isChecksumEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, CHECKSUM_ENABLED);
    }

    public boolean isAddMoneyEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, ADD_MONEY_ENABLED);
    }

    public boolean isPostConvenienceFeesEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, WITHOUT_CONVENIENCE_FEES);
    }

    public boolean isBinInResponseEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, BIN_IN_RESPONSE);
    }

    public boolean isEncRequestEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, ENC_PARAMS_ENABLED);
    }

    public String getLinkedPlatform(MerchantPreferenceStore preferenceStore) {
        return getPreferenceValue(preferenceStore, LINKED_PLATFORM);
    }

    public String getPWPCategory(MerchantPreferenceStore preferenceStore) {
        return getPreferenceValue(preferenceStore, PWP_CATEGORY);
    }

    public boolean isMerchantLinkedToFacebook(MerchantPreferenceStore preferenceStore) {
        return TheiaConstant.MerchantPreference.PreferenceValues.LINKED_PLATFORM_FACEBOOK.equals(getPreferenceValue(
                preferenceStore, LINKED_PLATFORM));
    }

    public boolean isStoreCardEnabledForMerchant(MerchantPreferenceStore preferenceStore) {
        return TheiaConstant.MerchantPreference.PreferenceValues.STORE_CARD_DETAILS.equals(getPreferenceValue(
                preferenceStore, STORE_CARD_DETAILS));
    }

    public boolean isSlabBasedMDREnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, SLAB_BASED_MDR);
    }

    public boolean isDynamicFeeMerchant(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, DYNAMIC_CHARGE_TARGET);
    }

    public boolean isQRCodePaymentEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, QR_PAY_ENABLED);
    }

    public boolean isPRNEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, PRN_ENABLED);
    }

    public boolean isEnhancedCashierPageEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore,
                TheiaConstant.MerchantPreference.PreferenceKeys.ENHANCED_CASHIER_PAGE_ENABLED);
    }

    public boolean isLoginDisabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, LOGIN_DISABLED);
    }

    public boolean isAOAMerchant(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, IS_AOA_MERCHANT);
    }

    public boolean isDefaultFFWebsiteEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, DEFAULT_FF_WEBSITE_ENABLED);
    }

    public boolean isDynamicQR2FAEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, DYNAMIC_QR_2FA);
    }

    public String getPWPCategory(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return getPWPCategory(merchantPreferenceStore);
    }

    public boolean isDynamicQR2FAEnabledWithPCF(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, DYNAMIC_QR_2FA) && isPostConvenienceFeesEnabled(preferenceStore);
    }

    public boolean isLogoAvailableOnNewLocation(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, LOGO_AVAILABLE_ON_NEW_LOCATION);
    }

    public boolean isAPIDisabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, TheiaConstant.MerchantPreference.PreferenceKeys.IS_API_DISABLED);
    }

    public boolean isMockMerchant(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, TheiaConstant.MerchantPreference.PreferenceKeys.IS_MOCK_MERCHANT);
    }

    public boolean isPgAutoLoginEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore,
                TheiaConstant.MerchantPreference.PreferenceKeys.PG_AUTOLOGIN_ENABLED);
    }

    public boolean isAES256EncRequestEnabled(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, AES256_ENC_PARAMS_ENABLED);
    }

    public boolean isHybridAllowed(MerchantPreferenceStore preferenceStore) {
        return isPreferenceEnabled(preferenceStore, HYBRID_ALLOWED);
    }

    public String getOrderExpiryTimeForMerchant(MerchantPreferenceStore preferenceStore) {
        return getPreferenceValue(preferenceStore, TheiaConstant.MerchantPreference.PreferenceKeys.ORDER_EXPIRY);
    }

    public MerchantPreferenceStore getMerchantPreferenceStore(PaymentRequestBean paymentRequestBean) {
        if (paymentRequestBean == null) {
            LOGGER.error("Payment Request Bean is null.");
            return null;
        }
        return preRedisCacheServiceImpl.getMerchantPreferenceStoreByCache(paymentRequestBean.getMid());
    }

    public boolean isAutoLoginEnable(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isAutoLoginEnable(merchantPreferenceStore);
    }

    public boolean isChecksumEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isChecksumEnabled(merchantPreferenceStore);
    }

    public boolean isAddMoneyEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isAddMoneyEnabled(merchantPreferenceStore);
    }

    public boolean isPostConvenienceFeesEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return ((!StringUtils.equals(paymentRequestBean.getMid(),
                ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))) && isPostConvenienceFeesEnabled(merchantPreferenceStore));
    }

    public boolean isBinInResponseEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isBinInResponseEnabled(merchantPreferenceStore);
    }

    public boolean isEncRequestEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isEncRequestEnabled(merchantPreferenceStore);
    }

    public String getLinkedPlatform(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return getLinkedPlatform(merchantPreferenceStore);
    }

    public boolean isMerchantLinkedToFacebook(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isMerchantLinkedToFacebook(merchantPreferenceStore);
    }

    public boolean isStoreCardEnabledForMerchant(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isStoreCardEnabledForMerchant(merchantPreferenceStore);
    }

    public boolean isSlabBasedMDREnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isSlabBasedMDREnabled(merchantPreferenceStore);
    }

    public boolean isQRCodePaymentEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isQRCodePaymentEnabled(merchantPreferenceStore);
    }

    public boolean isPRNEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isPRNEnabled(merchantPreferenceStore);
    }

    public boolean isEnhancedCashierPageEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isEnhancedCashierPageEnabled(merchantPreferenceStore);
    }

    public boolean isLoginDisabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isLoginDisabled(merchantPreferenceStore);
    }

    public boolean isAOAMerchant(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isAOAMerchant(merchantPreferenceStore);
    }

    public boolean isDefaultFFWebsiteEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isDefaultFFWebsiteEnabled(merchantPreferenceStore);
    }

    public boolean isDynamicQR2FAEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isDynamicQR2FAEnabled(merchantPreferenceStore);
    }

    public boolean isDynamicQR2FAEnabledWithPCF(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isDynamicQR2FAEnabledWithPCF(merchantPreferenceStore);
    }

    public boolean isLogoAvailableOnNewLocation(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isLogoAvailableOnNewLocation(merchantPreferenceStore);
    }

    public boolean isAPIDisabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isAPIDisabled(merchantPreferenceStore);
    }

    public boolean isMockMerchant(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isMockMerchant(merchantPreferenceStore);
    }

    public boolean isPgAutoLoginEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isPgAutoLoginEnabled(merchantPreferenceStore);
    }

    public boolean isAES256EncRequestEnabled(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isAES256EncRequestEnabled(merchantPreferenceStore);
    }

    public boolean isHybridAllowed(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isHybridAllowed(merchantPreferenceStore);
    }

    public String getOrderExpiryTimeForMerchant(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return getOrderExpiryTimeForMerchant(merchantPreferenceStore);
    }

    public MerchantPreferenceStore getMerchantPreferenceStore(WorkFlowRequestBean workFlowRequestBean) {
        if (workFlowRequestBean == null) {
            LOGGER.error("WorkFlowRequestBean is null.");
            return null;
        }
        return preRedisCacheServiceImpl.getMerchantPreferenceStoreByCache(workFlowRequestBean.getPaytmMID());
    }

    private MerchantPreferenceStore getMerchantPreferenceStore(String mid) {
        if (StringUtils.isBlank(mid)) {
            LOGGER.error("mid is blank.");
            return null;
        }
        return preRedisCacheServiceImpl.getMerchantPreferenceStoreByCache(mid);
    }

    public boolean isAutoLoginEnable(WorkFlowRequestBean workFlowRequestBean) {
        return isAutoLoginEnable(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isChecksumEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isChecksumEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isAddMoneyEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isAddMoneyEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isPostConvenienceFeesEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isPostConvenienceFeesEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isBinInResponseEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isBinInResponseEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isEncRequestEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isEncRequestEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public String getLinkedPlatform(WorkFlowRequestBean workFlowRequestBean) {
        return getLinkedPlatform(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isMerchantLinkedToFacebook(WorkFlowRequestBean workFlowRequestBean) {
        return isMerchantLinkedToFacebook(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isStoreCardEnabledForMerchant(WorkFlowRequestBean workFlowRequestBean) {
        return isStoreCardEnabledForMerchant(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isSlabBasedMDREnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isSlabBasedMDREnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isQRCodePaymentEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isQRCodePaymentEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isPRNEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isPRNEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isEnhancedCashierPageEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isEnhancedCashierPageEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isLoginDisabled(WorkFlowRequestBean workFlowRequestBean) {
        return isLoginDisabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isAOAMerchant(WorkFlowRequestBean workFlowRequestBean) {
        return isLoginDisabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isDefaultFFWebsiteEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isDefaultFFWebsiteEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isDynamicQR2FAEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isDynamicQR2FAEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isDynamicQR2FAEnabledWithPCF(WorkFlowRequestBean workFlowRequestBean) {
        return isDynamicQR2FAEnabledWithPCF(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isLogoAvailableOnNewLocation(WorkFlowRequestBean workFlowRequestBean) {
        return isLogoAvailableOnNewLocation(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isAPIDisabled(WorkFlowRequestBean workFlowRequestBean) {
        return isAPIDisabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isMockMerchant(WorkFlowRequestBean workFlowRequestBean) {
        return isMockMerchant(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isPgAutoLoginEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isPgAutoLoginEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isAES256EncRequestEnabled(WorkFlowRequestBean workFlowRequestBean) {
        return isAES256EncRequestEnabled(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isHybridAllowed(WorkFlowRequestBean workFlowRequestBean) {
        return isHybridAllowed(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public String getOrderExpiryTimeForMerchant(WorkFlowRequestBean workFlowRequestBean) {
        return getOrderExpiryTimeForMerchant(getMerchantPreferenceStore(workFlowRequestBean));
    }

    public boolean isDynamicFeeMerchant(PaymentRequestBean paymentRequestBean) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(paymentRequestBean);
        return isDynamicFeeMerchant(merchantPreferenceStore);
    }

    public boolean isBrandEMIEnabled(String mid) {
        return isPreferenceEnabled(getMerchantPreferenceStore(mid), BRAND_EMI);
    }

    public boolean isBinEligibleCoft(String mid) {
        return isPreferenceEnabled(getMerchantPreferenceStore(mid), BIN_ELIGIBLE_COFT);
    }

    public boolean isAccountRangeCardBinSharingEnable(String mid) {
        return isPreferenceEnabled(getMerchantPreferenceStore(mid), ENABLE_ACCOUNT_RANGE_CARD_BIN);
    }

    public boolean isAddMoneyEnabled(String mid) {
        MerchantPreferenceStore merchantPreferenceStore = getMerchantPreferenceStore(mid);
        return isAddMoneyEnabled(merchantPreferenceStore);
    }
}