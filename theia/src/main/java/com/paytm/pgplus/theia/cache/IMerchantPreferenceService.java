/**
 *
 */
package com.paytm.pgplus.theia.cache;

import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.nativ.enums.PayModeGroupSequenceEnum;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;

/**
 * @author amitdubey
 * @date Jan 13, 2017
 */
public interface IMerchantPreferenceService {

    MerchantPreferenceStore getMerchantPreferenceStore(String id);

    boolean isAutoLoginEnable(String merchantId);

    boolean isChecksumEnabled(String merchantId);

    boolean isAddMoneyEnabled(String merchantId);

    boolean isPostConvenienceFeesEnabled(String merchantId);

    boolean isBinInResponseEnabled(String merchantId);

    boolean isAllowTxnDeclineResponseEnabled(String merchantId);

    boolean isEncRequestEnabled(String merchantId);

    String getLinkedPlatform(String merchantId);

    boolean isMerchantLinkedToFacebook(String merchantId);

    boolean isSlabBasedMDREnabled(String merchantId);

    boolean isOffusOrderNotFound(String merchantId);

    boolean isQRCodePaymentEnabled(String merchantId);

    boolean isEnhancedCashierPageEnabled(String merchantId);

    boolean isEnhancedCashierPageEnabled(String merchantId, boolean defaultValue);

    boolean isStoreCardEnabledForMerchant(String merchantId);

    boolean isPRNEnabled(String merchantId);

    boolean isLocalePrefEnabled(String mid);

    boolean isCheckoutJsOnEnhancedFlowEnabled(String mid);

    boolean isLoginDisabled(String merchantId);

    boolean isAOAMerchant(String merchantId);

    boolean isDefaultFFWebsiteEnabled(String merchantId);

    boolean isMockMerchant(String merchantID);

    boolean isDynamicQR2FAEnabled(String merchantId);

    boolean isDynamicQR2FAEnabledWithPCF(String merchantId);

    boolean isLogoAvailableOnNewLocation(String merchantId);

    boolean isAPIDisabled(String merchantId);

    boolean isPgAutoLoginEnabled(String merchantId);

    boolean isPgAutoLoginEnabled(String merchantId, boolean defaultValue);

    boolean isPgAutoLoginDisabled(String merchantId, boolean defaultValue);

    boolean isAES256EncRequestEnabled(String merchantId);

    String getOrderExpiryTimeForMerchant(String mid);

    String getMerchantPaymodeSequence(String mid, PaymodeSequenceEnum paymodeSequenceEnum);

    String getMerchantPaymodeGrpSequence(String mid, PayModeGroupSequenceEnum payModeGroupSequenceEnum);

    boolean isNativeOtpSupported(String mid);

    boolean isNativeJsonRequestSupported(String mid);

    boolean isWebEnhancedCashierPageEnabled(String merchantId);

    boolean isWebEnhancedCashierPageEnabled(String merchantId, boolean defaultValue);

    boolean isMobileNumberNonEditable(String merchantId, boolean defaultValue);

    boolean isPcfFeeInfoEnabled(String merchantId);

    boolean isAppInvokeAllowed(String merchantId, boolean defaultValue);

    boolean isThemeticCustomizationEnabled(String merchantId, boolean defaultValue);

    public boolean isSubscriptionLimitOnWalletEnabled(String merchantId);

    boolean isDisabledLoginStrip(String merchantId, boolean defaultValue);

    boolean isSavedCardIdSupported(String merchantId);

    boolean isReturnVPAEnabled(String merchantId);

    boolean isPwpEnabled(String merchantId);

    boolean isReturnPrepaidEnabled(String merchantId);

    boolean isAddDescriptionMandatorySupported(String merchantId);

    String getDescTextFormat(String merchantId);

    boolean isDynamicFeeMerchant(String merchantId);

    boolean isOnusRentPaymentMerchant(String merchantId);

    boolean isSendCardSchemeEncryptedParamEnabled(String merchantId);

    boolean isEnhanceQrCodeDisabled(String merchantId);

    boolean isAutoAppInvokeAllowed(String merchantId);

    boolean isCheckUPIAccountSupported(String mid);

    boolean isAddNPayWithUPICollectSupported(String mid);

    boolean isCollectBoxEnabledForAddNPay(String mid);

    boolean isCostBasedPreferenceEnabled(String merchantId);

    boolean isCobrandedCardBinPreferenceEnabled(String merchantId, String cardBin);

    boolean isThresholdAmount2FAPreferenceEnabled(String merchantId);

    boolean isUpiAppsPayModeDisabled(String merchantId, boolean defaultValue);

    boolean isBlockCheckoutJS(String mid);

    boolean isAllowRegisteredUserOnlyLogin(String mid);

    boolean isAutoRefundPreferenceEnabled(String mid);

    boolean isCustomerFeedbackEnabled(String mid, boolean defaultValue);

    boolean isBlockBulkApplyPromo(String mid);

    boolean isFlexiSubscriptionEnabled(String mid, boolean defaultValue);

    boolean isDccEnabledMerchant(String mid);

    boolean isLocationPermission(String mid);

    boolean isMinimalPromoMerchant(String mid);

    boolean isMinimalSubventionMerchant(String mid);

    boolean showOnlyPaytmPaymodePaymentOffers(String mid);

    boolean isLinkPaymentOnlyMerchant(String mid);

    String getBankTransferCheckoutFlow(String mid);

    boolean isTxnPaidTimePreferenceEnabled(String mid, boolean defaultValue);

    boolean isEncryptedCardMerchant(String merchantId);

    boolean isBlockLinkFPODisablePaymode(String merchantId, boolean defaultValue);

    boolean isWalletOnlyMerchant(String merchantId);

    boolean isSubWalletSegregationEnabled(String mid);

    boolean isIdempotencyEnabledOnUi(String mid, boolean defaultValue);

    boolean isUpiCollectDisabledForNonLoggedIn(String mid);

    boolean isGroupedPayModesEnabled(String mid, boolean defaultValue);

    boolean convertTxnToAddNPayEnabled(String mid, boolean defaultValue);

    boolean enableUPICollectFromADDNPAY(String merchantId);

    boolean isPostpaidOnlyMerchant(String merchantId, boolean defaultValue);

    boolean isPG2Enabled(String merchantId, boolean defaultValue);

    boolean isOverrideAddNPayBehaviourEnabled(String mid);

    boolean isPostpaidEnabledOnMerchant(String merchantId, boolean defaultValue);

    boolean fetchPostpaidBalance(String merchantId);

    boolean isUpiCollectWhitelisted(String merchantId, boolean defaultValue);

    boolean isUserPostpaidStatusCheckEnabled(String mid);

    public boolean isAOAsPaytmPgMID(String merchantId, boolean defaultValue);

    boolean allowLoginOnDesktop(String merchantId, boolean defaultValue);

    boolean isFullPg2TrafficEnabled(String mid);

    boolean isSendPCFDetailsEncryptedParamEnabled(String merchantId);

    boolean isEligibleForMultipleMBIDFlow(String merchantId);

    boolean isInstantSettlementEnabled(String merchantId);

    boolean isCoftPromoParConfigEnabled(String merchantId);

    boolean isGlobalVaultCoft(String merchantId);

    boolean isSendCountryCode(String merchantId);

    boolean isPostpaidOnbordingEnable(String merchantId, boolean defaultValue);

    boolean isDisableLimitCCAddNPay(String merchantId);

    boolean isSubscriptionQrEnabled(String merchantId);

    boolean isDealsEnabled(String merchantId, boolean defaultValue);

    boolean isBlackListSupercashInlineOffers(String merchantId);

    boolean hideNetBankingBlank(String merchantId, boolean defaultValue);

    boolean isPopulateBankResultInfoEnabled(String merchantId);

    boolean enableBinIdentifierInResponse(String merchantId);

    boolean byPassCallBackUrlInRepeatRequest(String merchantId, boolean defaultValue);

    boolean isUpiDirectSettlementEnabled(String merchantId);

    boolean isCCOnUPIRailsEnabled(String merchantId);

    boolean isP2PMMerchantEnabled(String merchantId);

    boolean isCustomTransactionWebhookUrlEnabled(String merchantId);

    boolean isUpiLiteDisabled(String merchantId, boolean defaultValue);

    boolean isAddnPayDisabledForZeroBalance(String merchantId, boolean defaultValue);

    boolean is3pAddMoney(String merchantId, boolean defaultValue);

    boolean isUpiLiteEnabled(String merchantId, boolean defaultValue);

    boolean isP2pDisabled(String merchantId, boolean defaultValue);

    boolean isAddMoneyPcfDisabled(String merchantId, boolean defaultValue);

    boolean isAllowPPLimitCheckInEligiblityApi(String merchantId, boolean defaultValue);

}
