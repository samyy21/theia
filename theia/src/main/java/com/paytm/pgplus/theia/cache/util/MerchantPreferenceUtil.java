package com.paytm.pgplus.theia.cache.util;

import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.LINK_PAYMENT_ONLY_MERCHANT;

public class MerchantPreferenceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantPreferenceUtil.class);
    private static Map<String, String> merchantPreferenceImmutableMap;

    static {
        init();
    }

    private static void init() {
        Map<String, String> merchantPreferenceMap = new HashMap<>();
        merchantPreferenceMap.put(PreferenceKeys.IS_MOCK_MERCHANT, PreferenceValues.IS_MOCK_MERCHANT);
        merchantPreferenceMap.put(PreferenceKeys.CHECKSUM_ENABLED, PreferenceValues.CHECKSUM_ENABLED_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.BIN_IN_RESPONSE, PreferenceValues.BIN_IN_RESPONSE_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.WITHOUT_CONVENIENCE_FEES,
                PreferenceValues.WITHOUT_CONVENIENCE_FEES_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.AUTO_CREATE_USER, PreferenceValues.AUTO_CREATE_USER_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.ENC_PARAMS_ENABLED, PreferenceValues.ENC_PARAMS_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.ADD_MONEY_ENABLED, PreferenceValues.ADD_MONEY_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.HYBRID_ALLOWED, PreferenceValues.HYBRID_ALLOWED);
        merchantPreferenceMap.put(PreferenceKeys.SLAB_BASED_MDR, PreferenceValues.SLAB_BASED_ALLOWED);
        merchantPreferenceMap.put(PreferenceKeys.STORE_CARD_DETAILS, PreferenceValues.STORE_CARD_DETAILS);
        merchantPreferenceMap.put(PreferenceKeys.QR_PAY_ENABLED, PreferenceValues.QR_PAY_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.ENHANCED_CASHIER_PAGE_ENABLED,
                PreferenceValues.ENHANCED_CASHIER_PAGE_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.PRN_ENABLED, PreferenceValues.PRN_ENABLED);
        merchantPreferenceMap.put(PreferenceKeys.LOGIN_DISABLED, PreferenceValues.LOGIN_DISABLED);
        merchantPreferenceMap.put(PreferenceKeys.IS_AOA_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DEFAULT_FF_WEBSITE_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.LOGO_AVAILABLE_ON_NEW_LOCATION, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.IS_API_DISABLED, PreferenceValues.API_DISABLED);
        merchantPreferenceMap.put(PreferenceKeys.DYNAMIC_QR_2FA, PreferenceValues.API_DISABLED);
        merchantPreferenceMap.put(PreferenceKeys.AES256_ENC_PARAMS_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PG_AUTOLOGIN_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PG_AUTOLOGIN_DISABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.NATIVE_JSON_REQUEST, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.NATIVE_OTP_SUPPORTED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.WEB_ENHANCED_CASHIER_PAGE_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PG_MOBILE_NON_EDITABLE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PCF_FEE_INFO, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.APP_INVOKE_ALLOWED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DISABLED_LOGIN_STRIP, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.THEMATIC_FEATURE_USED, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.SUBSCRIPTION_WALLET_LIMIT_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DISABLED_LOGIN_STRIP, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.SAVED_CARD_ID_SUPPORTED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.RETURN_USER_VPA_IN_RESPONSE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PAY_WITH_PAYTM, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.RETURN_PREPAID_IN_RESPONSE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.OFFLINE_SNP_DESC_FLAG, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DYNAMIC_CHARGE_TARGET, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.RENT_PAYMENT_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.SEND_CARD_SCHEME_ENC_PARAMS_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.SEND_PCF_DETAILS_ENC_PARAMS_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.SEND_COUNTRY_CODE_PARAM_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENHANCE_QR_DISABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.AUTO_APP_INVOKE_ALLOWED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.CHECK_UPI_ACCOUNT_EXISTS, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ADDANDPAY_WITH_UPI_COLLECT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.UPI_COLLECT_BOX_ON_ADDANDPAY, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.COST_BASED_PREFERENCE_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.BLOCK_CHECKOUT_JS, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.LOCALE_PREF, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.CHECKOUT_JS_ON_ENHANCED_FLOW, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.UPI_APPS_PAY_MODE_DISABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ALLOW_REGISTERED_USER_ONLY, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PENNY_DROP_AUTO_REFUND_FLOW, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.FLEXI_SUBSCRIPTION_ENABLED, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.CUSTOMER_FEEDBACK_ENABLED, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.BLOCK_BULK_APPLY_PROMO, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.DCC_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.BRAND_EMI, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.LOCATION_PERMISSION, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.MINIMAL_PROMO_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.MINIMAL_SUBVENTION_MERCHANT, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.SHOW_PAYTM_PAYMODE_OFFERS, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.RETURN_TXN_PAID_TIME_IN_RESPONSE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.OFF_US_ORDER_NOT_FOUND_FAIL, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENCRYPTED_CARD_PAYMENT, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.BLOCK_LINK_FPO_DISABLE_PAYMODE, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.WalletOnlyMerchant, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.SUBWALLET_SEGREGATION_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_IDEMPOTENCY_ON_UI, PreferenceValues.Y);
        merchantPreferenceMap.put(LINK_PAYMENT_ONLY_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DISABLE_UPI_COLLECT_FOR_NON_LOGGEDIN, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.CONVERT_TO_ADDANDPAY_TXN, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.GROUPED_PAYMODES_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_UPI_COLLECT_FROM_ADDNPAY, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.POSTPAID_ONLY_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.BIN_ELIGIBLE_COFT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_ACCOUNT_RANGE_CARD_BIN, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.OVERRIDE_ADDNPAY_BEHAVIOUR, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.POSTPAID_ENABLED_ON_MERCHANT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.AOA_PAYTM_PG_MID, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.UPI_COLLECT_WHITELISTED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.CHECK_USER_POSTPAID_STATUS_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ALLOW_LOGIN_ON_DESKTOP, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.FETCH_POSTPAID_BALANCE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_COFT_PROMO_PAR_CONFIG, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.GLOBAL_VAULT_COFT, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.PP_2FA, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_SUBSCRIPTION_QR_FLOW, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.POSTPAID_ONBOARDING_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DISABLE_LIMIT_CC_ADD_N_PAY, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.OFFLINE_DEALS, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.BLACKLIST_SUPERCASH_INLINE_OFFERS, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.HIDE_NET_BANKING, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.POPULATE_BANK_RESULT_INFO, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ENABLE_BIN_IDENTIFIER_IN_RESPONSE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.BYPASS_CALLBACKURL_IN_REPEAT_REQUEST, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.CUSTOM_TRANSACTION_WEBHOOK_URL_ENABLED, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.CC_ON_UPI_RAILS_ENABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.P2PM_MERCHANT, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.UPI_LITE_DISABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.IS_3P_ADD_MONEY, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.P2P_DISABLED, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ALLOW_TXN_DECLINE_RESPONSE, PreferenceValues.Y);

        merchantPreferenceMap.put(PreferenceKeys.ADD_MONEY_SURCHARGE_EXEMPTION, PreferenceValues.Y);
        merchantPreferenceMap.put(PreferenceKeys.ALLOW_PP_LIMIT_CHECK_IN_ELIGIBILITY_API, PreferenceValues.Y);
        merchantPreferenceImmutableMap = Collections.unmodifiableMap(merchantPreferenceMap);

    }

    public static Map<String, String> getMerchantPreferenceMap() {
        if (merchantPreferenceImmutableMap == null) {
            LOGGER.info("merchantPreference map is null. Initializing..");
            init();
        }
        return merchantPreferenceImmutableMap;
    }
}
