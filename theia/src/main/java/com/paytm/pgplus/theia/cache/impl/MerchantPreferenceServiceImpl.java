package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceValues;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import com.paytm.pgplus.theia.nativ.enums.PayModeGroupSequenceEnum;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.LINK_PAYMENT_ONLY_MERCHANT;
import static com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceKeys.*;

/**
 * @author manojpal
 *
 */
@Service("merchantPreferenceService")
public class MerchantPreferenceServiceImpl implements IMerchantPreferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantPreferenceServiceImpl.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(MerchantPreferenceServiceImpl.class);

    @Autowired
    private PreRedisCacheHelper preRedisCacheHelper;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Override
    public MerchantPreferenceStore getMerchantPreferenceStore(String id) {
        return preRedisCacheHelper.getMerchantPreferenceStore(id);
    }

    private boolean isPreferenceEnabled(final String merchantId, String preferenceName) {
        if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(preferenceName)) {

            MerchantPreferenceStore preferenceStore = getMerchantPreferenceStore(merchantId);
            if (preferenceStore != null) {
                MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                if (preference != null) {
                    // LOGGER.info("MerchantPreference {} for mid {} is {}",
                    // preference, merchantId,
                    // preference.isEnabled() ? "enabled" : "disabled");
                    EXT_LOGGER.customInfo("MerchantPreference {} for mid {} is {}", preference, merchantId,
                            preference.isEnabled() ? "enabled" : "disabled");
                    return preference.isEnabled();
                }
                // LOGGER.info("MerchantPreference {} for mid {} is null",
                // preferenceName, merchantId);
                EXT_LOGGER.customInfo("MerchantPreference {} for mid {} is null", preferenceName, merchantId);
            }
        }
        return false;
    }

    private boolean isCardBinPrefernceEnabled(final String merchantId, String cardBin, String preferenceName) {
        try {
            if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(preferenceName)) {

                MerchantPreferenceStore preferenceStore = getMerchantPreferenceStore(merchantId);
                if (preferenceStore != null) {
                    MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                    String prefernceValues = " ";
                    if (preference != null) {
                        prefernceValues = preference.getPreferenceValue();
                        List<String> binList = Arrays.asList(prefernceValues.split(","));
                        EXT_LOGGER.customInfo("MerchantPreference {} for mid {} is {}", preference, merchantId,
                                preference.isEnabled() ? "enabled" : "disabled");
                        LOGGER.info("bin list is {}", binList);
                        return binList.contains(cardBin);
                    }
                    EXT_LOGGER.customInfo("MerchantPreference {} for mid {} is null", preferenceName, merchantId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in fetching prefernce {}", e);
        }

        return false;
    }

    private String getPreferenceValue(final String merchantId, final String preferenceName) {
        if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(preferenceName)) {

            MerchantPreferenceStore preferenceStore = getMerchantPreferenceStore(merchantId);
            if (preferenceStore != null) {
                MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                if (preference != null) {
                    return preference.getPreferenceValue();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private boolean isPreferenceEnabled(final String merchantId, String preferenceName, boolean defaultValue) {
        if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(preferenceName)) {
            MerchantPreferenceStore preferenceStore = getMerchantPreferenceStore(merchantId);
            if (preferenceStore != null) {
                MerchantPreference preference = preferenceStore.getPreferences().get(preferenceName);
                if (preference == null || StringUtils.isBlank(preference.getPreferenceValue())) {
                    return defaultValue;
                } else {
                    return preference.isEnabled();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAutoLoginEnable(String merchantId) {
        return isPreferenceEnabled(merchantId, AUTO_CREATE_USER);
    }

    @Override
    public boolean isChecksumEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, CHECKSUM_ENABLED);
    }

    @Override
    public boolean isAddMoneyEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, ADD_MONEY_ENABLED);
    }

    @Override
    public boolean isPostConvenienceFeesEnabled(String merchantId) {
        return ((!StringUtils.equals(merchantId, ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID))) && isPreferenceEnabled(
                merchantId, WITHOUT_CONVENIENCE_FEES));
    }

    @Override
    public boolean isBinInResponseEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, BIN_IN_RESPONSE);
    }

    @Override
    public boolean isAllowTxnDeclineResponseEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, CommonConstants.ALLOW_TXN_DECLINE_RESPONSE);
    }

    @Override
    public boolean isEncRequestEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, ENC_PARAMS_ENABLED);
    }

    @Override
    public String getLinkedPlatform(String merchantId) {
        return getPreferenceValue(merchantId, LINKED_PLATFORM);
    }

    @Override
    public boolean isMerchantLinkedToFacebook(String merchantId) {
        return PreferenceValues.LINKED_PLATFORM_FACEBOOK.equals(getPreferenceValue(merchantId, LINKED_PLATFORM));
    }

    @Override
    public boolean isStoreCardEnabledForMerchant(String merchantId) {
        return PreferenceValues.STORE_CARD_DETAILS.equalsIgnoreCase(getPreferenceValue(merchantId, STORE_CARD_DETAILS));
    }

    @Override
    public boolean isSlabBasedMDREnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, SLAB_BASED_MDR);
    }

    @Override
    public boolean isOffusOrderNotFound(String merchantId) {
        return isPreferenceEnabled(merchantId, OFF_US_ORDER_NOT_FOUND_FAIL);
    }

    @Override
    public boolean isQRCodePaymentEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, QR_PAY_ENABLED);
    }

    @Override
    public boolean isPRNEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, PRN_ENABLED);
    }

    @Override
    public boolean isEnhancedCashierPageEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.ENHANCED_CASHIER_PAGE_ENABLED);
    }

    @Override
    public boolean isEnhancedCashierPageEnabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.ENHANCED_CASHIER_PAGE_ENABLED, defaultValue);
    }

    @Override
    public boolean isLocalePrefEnabled(String mid) {
        return isPreferenceEnabled(mid, LOCALE_PREF, false);
    }

    @Override
    public boolean isCheckoutJsOnEnhancedFlowEnabled(String mid) {
        return isPreferenceEnabled(mid, CHECKOUT_JS_ON_ENHANCED_FLOW, false);
    }

    @Override
    public boolean isLoginDisabled(String merchantId) {
        return isPreferenceEnabled(merchantId, LOGIN_DISABLED);
    }

    @Override
    public boolean isAOAMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, IS_AOA_MERCHANT);
    }

    @Override
    public boolean isDefaultFFWebsiteEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, DEFAULT_FF_WEBSITE_ENABLED);
    }

    @Override
    public boolean isDynamicQR2FAEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, DYNAMIC_QR_2FA);
    }

    @Override
    public boolean isDynamicQR2FAEnabledWithPCF(String merchantId) {
        return isPreferenceEnabled(merchantId, DYNAMIC_QR_2FA) && isPostConvenienceFeesEnabled(merchantId);
    }

    @Override
    public boolean isLogoAvailableOnNewLocation(String merchantId) {
        return isPreferenceEnabled(merchantId, LOGO_AVAILABLE_ON_NEW_LOCATION);
    }

    @Override
    public boolean isAPIDisabled(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.IS_API_DISABLED);
    }

    @Override
    public boolean isMockMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.IS_MOCK_MERCHANT);
    }

    @Override
    public boolean isPgAutoLoginEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.PG_AUTOLOGIN_ENABLED);
    }

    @Override
    public boolean isPgAutoLoginEnabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.PG_AUTOLOGIN_ENABLED,
                defaultValue);
    }

    @Override
    public boolean isPgAutoLoginDisabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.PG_AUTOLOGIN_DISABLED,
                defaultValue);
    }

    @Override
    public boolean isMobileNumberNonEditable(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, PreferenceKeys.PG_MOBILE_NON_EDITABLE, defaultValue);
    }

    @Override
    public boolean isAppInvokeAllowed(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, PreferenceKeys.APP_INVOKE_ALLOWED, defaultValue);
    }

    @Override
    public boolean isThemeticCustomizationEnabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, THEMATIC_FEATURE_USED, defaultValue);
    }

    @Override
    public boolean isAES256EncRequestEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, AES256_ENC_PARAMS_ENABLED);
    }

    @Override
    public String getOrderExpiryTimeForMerchant(String mid) {
        return getPreferenceValue(mid, TheiaConstant.MerchantPreference.PreferenceKeys.ORDER_EXPIRY);
    }

    @Override
    public String getMerchantPaymodeSequence(String mid, PaymodeSequenceEnum paymodeSequenceEnum) {
        String response = null;
        if (paymodeSequenceEnum != null) {
            response = getPreferenceValue(mid, paymodeSequenceEnum.getPreferenceName());
        }
        if (StringUtils.isEmpty(response)) {
            response = getPreferenceValue(mid, PaymodeSequenceEnum.DEFAULT.getPreferenceName());
        }
        return StringUtils.isNotEmpty(response) ? response : null;
    }

    @Override
    public String getMerchantPaymodeGrpSequence(String mid, PayModeGroupSequenceEnum payModeGroupSequenceEnum) {
        String response = null;
        if (payModeGroupSequenceEnum != null) {
            response = getPreferenceValue(mid, payModeGroupSequenceEnum.getPreferenceName());
        }
        if (StringUtils.isEmpty(response)) {
            response = getPreferenceValue(mid, PayModeGroupSequenceEnum.DEFAULT.getPreferenceName());
        }
        return StringUtils.isNotEmpty(response) ? response : null;
    }

    @Override
    public boolean isNativeOtpSupported(String merchantId) {
        return isPreferenceEnabled(merchantId, NATIVE_OTP_SUPPORTED);
    }

    @Override
    public boolean isNativeJsonRequestSupported(String merchantId) {
        return isPreferenceEnabled(merchantId, NATIVE_JSON_REQUEST);
    }

    @Override
    public boolean isWebEnhancedCashierPageEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.WEB_ENHANCED_CASHIER_PAGE_ENABLED);
    }

    @Override
    public boolean isWebEnhancedCashierPageEnabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.WEB_ENHANCED_CASHIER_PAGE_ENABLED, defaultValue);
    }

    public boolean isPcfFeeInfoEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.PCF_FEE_INFO);
    }

    @Override
    public boolean isSubscriptionLimitOnWalletEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, SUBSCRIPTION_WALLET_LIMIT_ENABLED);
    }

    @Override
    public boolean isDisabledLoginStrip(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.DISABLED_LOGIN_STRIP,
                defaultValue);
    }

    @Override
    public boolean isSavedCardIdSupported(String merchantId) {
        return isPreferenceEnabled(merchantId, SAVED_CARD_ID_SUPPORTED);
    }

    @Override
    public boolean isReturnVPAEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, RETURN_USER_VPA_IN_RESPONSE);
    }

    @Override
    public boolean isPwpEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, PAY_WITH_PAYTM);
    }

    @Override
    public boolean isReturnPrepaidEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, RETURN_PREPAID_IN_RESPONSE);
    }

    @Override
    public boolean isDynamicFeeMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, DYNAMIC_CHARGE_TARGET);
    }

    @Override
    public boolean isOnusRentPaymentMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, RENT_PAYMENT_MERCHANT);
    }

    @Override
    public boolean isMinimalPromoMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, MINIMAL_PROMO_MERCHANT);
    }

    @Override
    public boolean isMinimalSubventionMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, MINIMAL_SUBVENTION_MERCHANT);
    }

    @Override
    public boolean showOnlyPaytmPaymodePaymentOffers(String merchantId) {
        return isPreferenceEnabled(merchantId, SHOW_PAYTM_PAYMODE_OFFERS);
    }

    @Override
    public boolean isLinkPaymentOnlyMerchant(String mid) {
        return isPreferenceEnabled(mid, LINK_PAYMENT_ONLY_MERCHANT);
    }

    @Override
    public boolean isAddDescriptionMandatorySupported(String merchantId) {
        return isPreferenceEnabled(merchantId, OFFLINE_SNP_DESC_FLAG);
    }

    @Override
    public String getDescTextFormat(String merchantId) {
        String descText = getPreferenceValue(merchantId, OFFLINE_SNP_DESC_TXT);
        if (StringUtils.isNotEmpty(descText)) {
            return descText;
        }
        return null;
    }

    public boolean isSendCardSchemeEncryptedParamEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, SEND_CARD_SCHEME_ENC_PARAMS_ENABLED);
    }

    @Override
    public boolean isEnhanceQrCodeDisabled(String merchantId) {
        return isPreferenceEnabled(merchantId, ENHANCE_QR_DISABLED);
    }

    @Override
    public boolean isAutoAppInvokeAllowed(String merchantId) {
        return isPreferenceEnabled(merchantId, AUTO_APP_INVOKE_ALLOWED);
    }

    @Override
    public boolean isCheckUPIAccountSupported(String mid) {
        return isPreferenceEnabled(mid, CHECK_UPI_ACCOUNT_EXISTS);
    }

    @Override
    public boolean isAddNPayWithUPICollectSupported(String mid) {
        return isPreferenceEnabled(mid, ADDANDPAY_WITH_UPI_COLLECT);
    }

    @Override
    public boolean isCollectBoxEnabledForAddNPay(String mid) {
        return isPreferenceEnabled(mid, UPI_COLLECT_BOX_ON_ADDANDPAY);
    }

    @Override
    public boolean isCostBasedPreferenceEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, COST_BASED_PREFERENCE_ENABLED);
    }

    @Override
    public boolean isCobrandedCardBinPreferenceEnabled(String merchantId, String cardBin) {
        return isCardBinPrefernceEnabled(merchantId, cardBin, CO_BRANDED_CARD_IDENTIFIER);
    }

    @Override
    public boolean isThresholdAmount2FAPreferenceEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, PP_2FA);
    }

    @Override
    public boolean isSubscriptionQrEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, ENABLE_SUBSCRIPTION_QR_FLOW);
    }

    @Override
    public boolean isUpiAppsPayModeDisabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, UPI_APPS_PAY_MODE_DISABLED, defaultValue);
    }

    @Override
    public boolean isBlackListSupercashInlineOffers(String merchantId) {
        return isPreferenceEnabled(merchantId, BLACKLIST_SUPERCASH_INLINE_OFFERS);
    }

    // this method will work when native json preference is disabled
    @Override
    public boolean isBlockCheckoutJS(String mid) {
        return isPreferenceEnabled(mid, BLOCK_CHECKOUT_JS);
    }

    @Override
    public boolean isAllowRegisteredUserOnlyLogin(String mid) {
        return isPreferenceEnabled(mid, ALLOW_REGISTERED_USER_ONLY);
    }

    @Override
    public boolean isAutoRefundPreferenceEnabled(String mid) {
        return PreferenceValues.REFUND.equals(getPreferenceValue(mid, PENNY_DROP_AUTO_REFUND_FLOW));
    }

    // PGP-24609 changes
    @Override
    public boolean isCustomerFeedbackEnabled(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, CUSTOMER_FEEDBACK_ENABLED, defaultValue);
    }

    @Override
    public boolean isBlockBulkApplyPromo(String mid) {
        return isPreferenceEnabled(mid, BLOCK_BULK_APPLY_PROMO);
    }

    @Override
    public boolean isFlexiSubscriptionEnabled(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, FLEXI_SUBSCRIPTION_ENABLED, defaultValue);
    }

    @Override
    public boolean isDccEnabledMerchant(String mid) {
        return isPreferenceEnabled(mid, DCC_ENABLED);
    }

    @Override
    public boolean isLocationPermission(String mid) {
        return isPreferenceEnabled(mid, LOCATION_PERMISSION);
    }

    @Override
    public String getBankTransferCheckoutFlow(String mid) {
        return getPreferenceValue(mid, BANK_TRANSFER_CHECKOUT_FLOW);
    }

    @Override
    public boolean isTxnPaidTimePreferenceEnabled(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, RETURN_TXN_PAID_TIME_IN_RESPONSE, defaultValue);
    }

    @Override
    public boolean isEncryptedCardMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, ENCRYPTED_CARD_PAYMENT);
    }

    @Override
    public boolean isBlockLinkFPODisablePaymode(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.BLOCK_LINK_FPO_DISABLE_PAYMODE, defaultValue);
    }

    @Override
    public boolean isWalletOnlyMerchant(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.WalletOnlyMerchant);
    }

    public boolean isSubWalletSegregationEnabled(String mid) {
        return isPreferenceEnabled(mid, TheiaConstant.MerchantPreference.PreferenceKeys.SUBWALLET_SEGREGATION_ENABLED);
    }

    @Override
    public boolean isIdempotencyEnabledOnUi(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, TheiaConstant.MerchantPreference.PreferenceKeys.ENABLE_IDEMPOTENCY_ON_UI,
                defaultValue);
    }

    @Override
    public boolean isUpiCollectDisabledForNonLoggedIn(String mid) {
        return isPreferenceEnabled(mid, DISABLE_UPI_COLLECT_FOR_NON_LOGGEDIN);
    }

    @Override
    public boolean isGroupedPayModesEnabled(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, TheiaConstant.MerchantPreference.PreferenceKeys.GROUPED_PAYMODES_ENABLED,
                defaultValue);
    }

    public boolean convertTxnToAddNPayEnabled(String mid, boolean defaultValue) {
        return isPreferenceEnabled(mid, CONVERT_TO_ADDANDPAY_TXN, defaultValue);
    }

    public boolean enableUPICollectFromADDNPAY(String merchantId) {
        return isPreferenceEnabled(merchantId, ENABLE_UPI_COLLECT_FROM_ADDNPAY);
    }

    @Override
    public boolean isPostpaidOnlyMerchant(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.POSTPAID_ONLY_MERCHANT,
                defaultValue);
    }

    @Override
    public boolean isPG2Enabled(String merchantId, boolean defaultValue) {
        return true;
    }

    @Override
    public boolean isOverrideAddNPayBehaviourEnabled(String mid) {
        return isPreferenceEnabled(mid, OVERRIDE_ADDNPAY_BEHAVIOUR);
    }

    @Override
    public boolean isPostpaidEnabledOnMerchant(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.POSTPAID_ENABLED_ON_MERCHANT, defaultValue);
    }

    public boolean isEligibleForMultipleMBIDFlow(String merchantId) {
        return "Y".equals(getPreferenceValue(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.TPV_MULTIPLE_MBID_ENABLED));
    }

    @Override
    public boolean isInstantSettlementEnabled(String merchantId) {
        return "Y".equals(getPreferenceValue(merchantId,
                TheiaConstant.MerchantPreference.PreferenceKeys.INSTANT_SETTLEMENT));
    }

    @Override
    public boolean isAOAsPaytmPgMID(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, AOA_PAYTM_PG_MID);
    }

    public boolean isUpiCollectWhitelisted(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.UPI_COLLECT_WHITELISTED,
                defaultValue);
    }

    @Override
    public boolean isUserPostpaidStatusCheckEnabled(String mid) {
        return isPreferenceEnabled(mid, CHECK_USER_POSTPAID_STATUS_ENABLED);
    }

    @Override
    public boolean fetchPostpaidBalance(String merchantId) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.FETCH_POSTPAID_BALANCE);
    }

    @Override
    public boolean allowLoginOnDesktop(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, TheiaConstant.MerchantPreference.PreferenceKeys.ALLOW_LOGIN_ON_DESKTOP,
                defaultValue);
    }

    @Override
    public boolean isFullPg2TrafficEnabled(String mid) {
        return true;
    }

    @Override
    public boolean isSendPCFDetailsEncryptedParamEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, SEND_PCF_DETAILS_ENC_PARAMS_ENABLED, true);
    }

    @Override
    public boolean isSendCountryCode(String merchantId) {
        return isPreferenceEnabled(merchantId, SEND_COUNTRY_CODE_PARAM_ENABLED, false);
    }

    @Override
    public boolean isCoftPromoParConfigEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, ENABLE_COFT_PROMO_PAR_CONFIG);
    }

    @Override
    public boolean isGlobalVaultCoft(String merchantId) {
        return isPreferenceEnabled(merchantId, GLOBAL_VAULT_COFT);
    }

    @Override
    public boolean isPostpaidOnbordingEnable(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, POSTPAID_ONBOARDING_ENABLED, defaultValue);
    }

    @Override
    public boolean isDisableLimitCCAddNPay(String merchantId) {
        return isPreferenceEnabled(merchantId, DISABLE_LIMIT_CC_ADD_N_PAY);
    }

    @Override
    public boolean isDealsEnabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, OFFLINE_DEALS, defaultValue);
    }

    @Override
    public boolean hideNetBankingBlank(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, HIDE_NET_BANKING, defaultValue);
    }

    public boolean isPopulateBankResultInfoEnabled(String merchantId) {

        return isPreferenceEnabled(merchantId, POPULATE_BANK_RESULT_INFO);
    }

    @Override
    public boolean enableBinIdentifierInResponse(String merchantId) {
        return isPreferenceEnabled(merchantId, ENABLE_BIN_IDENTIFIER_IN_RESPONSE);
    }

    @Override
    public boolean byPassCallBackUrlInRepeatRequest(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, BYPASS_CALLBACKURL_IN_REPEAT_REQUEST, defaultValue);
    }

    @Override
    public boolean isUpiDirectSettlementEnabled(String merchantId) {
        return "Y".equals(getPreferenceValue(merchantId, UPI_DIRECT_SETTLEMENT));
    }

    public boolean isCCOnUPIRailsEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, CC_ON_UPI_RAILS_ENABLED);
    }

    @Override
    public boolean isP2PMMerchantEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, P2PM_MERCHANT);
    }

    @Override
    public boolean isUpiLiteDisabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, UPI_LITE_DISABLED, defaultValue);
    }

    @Override
    public boolean isUpiLiteEnabled(String merchantId, boolean defaultValue) {
        return "Y".equals(getPreferenceValue(merchantId, "UPI_LITE_ENABLED"));
    }

    @Override
    public boolean isCustomTransactionWebhookUrlEnabled(String merchantId) {
        return isPreferenceEnabled(merchantId, CUSTOM_TRANSACTION_WEBHOOK_URL_ENABLED);
    }

    @Override
    public boolean isAddnPayDisabledForZeroBalance(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, DISABLE_ADD_N_PAY_FOR_WALLET_0_BALANCE, defaultValue);
    }

    @Override
    public boolean is3pAddMoney(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, IS_3P_ADD_MONEY, defaultValue);
    }

    @Override
    public boolean isP2pDisabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, P2P_DISABLED, defaultValue);
    }

    @Override
    public boolean isAddMoneyPcfDisabled(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, ADD_MONEY_SURCHARGE_EXEMPTION, defaultValue);
    }

    @Override
    public boolean isAllowPPLimitCheckInEligiblityApi(String merchantId, boolean defaultValue) {
        return isPreferenceEnabled(merchantId, ALLOW_PP_LIMIT_CHECK_IN_ELIGIBILITY_API, defaultValue);
    }
}
