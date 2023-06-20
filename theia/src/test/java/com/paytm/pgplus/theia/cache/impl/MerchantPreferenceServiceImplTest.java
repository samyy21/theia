package com.paytm.pgplus.theia.cache.impl;

import com.paytm.pgplus.theia.cache.model.MerchantPreference;
import com.paytm.pgplus.theia.cache.model.MerchantPreferenceStore;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.helper.PreRedisCacheHelper;
import com.paytm.pgplus.theia.nativ.model.common.PaymodeSequenceEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MerchantPreferenceServiceImplTest {

    @InjectMocks
    private MerchantPreferenceServiceImpl merchantPreferenceService;

    @Mock
    private PreRedisCacheHelper preRedisCacheHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MerchantPreferenceStore merchantPreferenceStore = mock(MerchantPreferenceStore.class);
        MerchantPreference merchantPreference = mock(MerchantPreference.class);
        when(merchantPreference.isEnabled()).thenReturn(true);
        when(merchantPreference.getPreferenceValue()).thenReturn("preferenceValue");
        Map<String, MerchantPreference> preferences = mock(Map.class);
        when(preferences.get(Mockito.any())).thenReturn(merchantPreference);
        when(merchantPreferenceStore.getPreferences()).thenReturn(preferences);
        when(preRedisCacheHelper.getMerchantPreferenceStore("id")).thenReturn(merchantPreferenceStore);
    }

    // true from store
    @Test
    public void isUpiCollectWhitelisted_preferanceFoundTrue_shouldReturnTrue() {
        String mid = "ankitPay";
        MerchantPreference merchantPreference = new MerchantPreference();
        merchantPreference.setPreferenceName(TheiaConstant.MerchantPreference.PreferenceKeys.UPI_COLLECT_WHITELISTED);
        merchantPreference.setPreferenceValue("Y");
        merchantPreference.setEnabled(true);
        Map<String, MerchantPreference> map = new HashMap<>();
        map.put(TheiaConstant.MerchantPreference.PreferenceKeys.UPI_COLLECT_WHITELISTED, merchantPreference);

        MerchantPreferenceStore merchantPreferenceStore = Mockito.mock(MerchantPreferenceStore.class);

        Mockito.when(merchantPreferenceStore.getPreferences()).thenReturn(map);
        Mockito.when(preRedisCacheHelper.getMerchantPreferenceStore(mid)).thenReturn(merchantPreferenceStore);
        Assert.assertTrue(merchantPreferenceService.isUpiCollectWhitelisted(mid, false));
    }

    // false from store
    @Test
    public void isUpiCollectWhitelisted_preferanceFoundFalse_shouldReturnFalse() {
        String mid = "ankitPay";
        MerchantPreference merchantPreference = new MerchantPreference();
        merchantPreference.setPreferenceName(TheiaConstant.MerchantPreference.PreferenceKeys.UPI_COLLECT_WHITELISTED);
        merchantPreference.setPreferenceValue("Y");
        merchantPreference.setEnabled(false);
        Map<String, MerchantPreference> map = new HashMap<>();
        map.put(TheiaConstant.MerchantPreference.PreferenceKeys.UPI_COLLECT_WHITELISTED, merchantPreference);

        MerchantPreferenceStore merchantPreferenceStore = Mockito.mock(MerchantPreferenceStore.class);

        Mockito.when(merchantPreferenceStore.getPreferences()).thenReturn(map);
        Mockito.when(preRedisCacheHelper.getMerchantPreferenceStore(mid)).thenReturn(merchantPreferenceStore);
        Assert.assertTrue(!merchantPreferenceService.isUpiCollectWhitelisted(mid, true));
    }

    // store = null default value
    @Test
    public void isUpiCollectWhitelisted_preferanceNotFound_shouldReturnDefaultTrue() {
        String mid = "ankitPay";
        MerchantPreferenceStore merchantPreferenceStore = Mockito.mock(MerchantPreferenceStore.class);
        Map<String, MerchantPreference> map = new HashMap<>();
        Mockito.when(merchantPreferenceStore.getPreferences()).thenReturn(map);
        Mockito.when(preRedisCacheHelper.getMerchantPreferenceStore(mid)).thenReturn(merchantPreferenceStore);
        Assert.assertTrue(merchantPreferenceService.isUpiCollectWhitelisted(mid, true));
    }

    // store = null default value
    @Test
    public void isUpiCollectWhitelisted_preferanceNotFound_shouldReturnDefaultFalse() {
        String mid = "ankitPay";
        MerchantPreferenceStore merchantPreferenceStore = Mockito.mock(MerchantPreferenceStore.class);
        Map<String, MerchantPreference> map = new HashMap<>();
        Mockito.when(merchantPreferenceStore.getPreferences()).thenReturn(map);
        Mockito.when(preRedisCacheHelper.getMerchantPreferenceStore(mid)).thenReturn(merchantPreferenceStore);
        Assert.assertTrue(!merchantPreferenceService.isUpiCollectWhitelisted(mid, false));
    }

    @Test
    public void isAutoLoginEnable() {
        merchantPreferenceService.isAutoLoginEnable("id");
    }

    @Test
    public void isChecksumEnabled() {
        merchantPreferenceService.isChecksumEnabled("id");
    }

    @Test
    public void isAddMoneyEnabled() {
        merchantPreferenceService.isAddMoneyEnabled("id");
    }

    @Test
    public void isPostConvenienceFeesEnabled() {
        merchantPreferenceService.isPostConvenienceFeesEnabled("id");
    }

    @Test
    public void isBinInResponseEnabled() {
        merchantPreferenceService.isBinInResponseEnabled("id");
    }

    @Test
    public void isEncRequestEnabled() {
        merchantPreferenceService.isEncRequestEnabled("id");
    }

    @Test
    public void getLinkedPlatform() {
        merchantPreferenceService.getLinkedPlatform("id");
    }

    @Test
    public void isMerchantLinkedToFacebook() {
        merchantPreferenceService.isMerchantLinkedToFacebook("id");
    }

    @Test
    public void isStoreCardEnabledForMerchant() {
        merchantPreferenceService.isStoreCardEnabledForMerchant("id");
    }

    @Test
    public void isSlabBasedMDREnabled() {
        merchantPreferenceService.isSlabBasedMDREnabled("id");
    }

    @Test
    public void isOffusOrderNotFound() {
        merchantPreferenceService.isOffusOrderNotFound("id");
    }

    @Test
    public void isQRCodePaymentEnabled() {
        merchantPreferenceService.isQRCodePaymentEnabled("id");
    }

    @Test
    public void isPRNEnabled() {
        merchantPreferenceService.isPRNEnabled("id");
    }

    @Test
    public void isEnhancedCashierPageEnabled() {
        merchantPreferenceService.isEnhancedCashierPageEnabled("id");
    }

    @Test
    public void testIsEnhancedCashierPageEnabled() {
        merchantPreferenceService.isEnhancedCashierPageEnabled("id", false);
    }

    @Test
    public void isLocalePrefEnabled() {
        merchantPreferenceService.isLocalePrefEnabled("id");
    }

    @Test
    public void isCheckoutJsOnEnhancedFlowEnabled() {
        merchantPreferenceService.isCheckoutJsOnEnhancedFlowEnabled("id");
    }

    @Test
    public void isLoginDisabled() {
        merchantPreferenceService.isLoginDisabled("id");
    }

    @Test
    public void isAOAMerchant() {
        merchantPreferenceService.isAOAMerchant("id");
    }

    @Test
    public void isDefaultFFWebsiteEnabled() {
        merchantPreferenceService.isDefaultFFWebsiteEnabled("id");
    }

    @Test
    public void isDynamicQR2FAEnabled() {
        merchantPreferenceService.isDynamicQR2FAEnabled("id");
    }

    @Test
    public void isDynamicQR2FAEnabledWithPCF() {
        merchantPreferenceService.isDynamicQR2FAEnabledWithPCF("id");
    }

    @Test
    public void isLogoAvailableOnNewLocation() {
        merchantPreferenceService.isLogoAvailableOnNewLocation("id");
    }

    @Test
    public void isAPIDisabled() {
        merchantPreferenceService.isAPIDisabled("id");
    }

    @Test
    public void isMockMerchant() {
        merchantPreferenceService.isMockMerchant("id");
    }

    @Test
    public void isPgAutoLoginEnabled() {
        merchantPreferenceService.isPgAutoLoginEnabled("id");
    }

    @Test
    public void testIsPgAutoLoginEnabled() {
        merchantPreferenceService.isPgAutoLoginEnabled("id", true);
    }

    @Test
    public void isPgAutoLoginDisabled() {
        merchantPreferenceService.isPgAutoLoginDisabled("id", true);
    }

    @Test
    public void isMobileNumberNonEditable() {
        merchantPreferenceService.isMobileNumberNonEditable("id", true);
    }

    @Test
    public void isAppInvokeAllowed() {
        merchantPreferenceService.isAppInvokeAllowed("id", true);
    }

    @Test
    public void isThemeticCustomizationEnabled() {
        merchantPreferenceService.isThemeticCustomizationEnabled("id", true);
    }

    @Test
    public void isAES256EncRequestEnabled() {
        merchantPreferenceService.isAES256EncRequestEnabled("id");
    }

    @Test
    public void getOrderExpiryTimeForMerchant() {
        merchantPreferenceService.getOrderExpiryTimeForMerchant("id");
    }

    @Test
    public void getMerchantPaymodeSequence() {
        merchantPreferenceService.getMerchantPaymodeSequence("id", PaymodeSequenceEnum.DEFAULT);
    }

    @Test
    public void isNativeOtpSupported() {
        merchantPreferenceService.isNativeOtpSupported("id");
    }

    @Test
    public void isNativeJsonRequestSupported() {
        merchantPreferenceService.isNativeJsonRequestSupported("id");
    }

    @Test
    public void isWebEnhancedCashierPageEnabled() {
        merchantPreferenceService.isWebEnhancedCashierPageEnabled("id");
    }

    @Test
    public void testIsWebEnhancedCashierPageEnabled() {
        merchantPreferenceService.isWebEnhancedCashierPageEnabled("id", true);
    }

    @Test
    public void isPcfFeeInfoEnabled() {
        merchantPreferenceService.isPcfFeeInfoEnabled("id");
    }

    @Test
    public void isSubscriptionLimitOnWalletEnabled() {
        merchantPreferenceService.isSubscriptionLimitOnWalletEnabled("id");
    }

    @Test
    public void isDisabledLoginStrip() {
        merchantPreferenceService.isDisabledLoginStrip("id", true);
    }

    @Test
    public void isSavedCardIdSupported() {
        merchantPreferenceService.isSavedCardIdSupported("id");
    }

    @Test
    public void isReturnVPAEnabled() {
        merchantPreferenceService.isReturnVPAEnabled("id");
    }

    @Test
    public void isPwpEnabled() {
        merchantPreferenceService.isPwpEnabled("id");
    }

    @Test
    public void isReturnPrepaidEnabled() {
        merchantPreferenceService.isReturnPrepaidEnabled("id");
    }

    @Test
    public void isDynamicFeeMerchant() {
        merchantPreferenceService.isDynamicFeeMerchant("id");
    }

    @Test
    public void isOnusRentPaymentMerchant() {
        merchantPreferenceService.isOnusRentPaymentMerchant("id");
    }

    @Test
    public void isMinimalPromoMerchant() {
        merchantPreferenceService.isMinimalPromoMerchant("id");
    }

    @Test
    public void showOnlyPaytmPaymodePaymentOffers() {
        merchantPreferenceService.showOnlyPaytmPaymodePaymentOffers("id");
    }

    @Test
    public void isAddDescriptionMandatorySupported() {
        merchantPreferenceService.isAddDescriptionMandatorySupported("id");
    }

    @Test
    public void getDescTextFormat() {
        merchantPreferenceService.getDescTextFormat("id");
    }

    @Test
    public void isSendCardSchemeEncryptedParamEnabled() {
        merchantPreferenceService.isSendCardSchemeEncryptedParamEnabled("id");
    }

    @Test
    public void isEnhanceQrCodeDisabled() {
        merchantPreferenceService.isEnhanceQrCodeDisabled("id");
    }

    @Test
    public void isAutoAppInvokeAllowed() {
        merchantPreferenceService.isAutoAppInvokeAllowed("id");
    }

    @Test
    public void isCheckUPIAccountSupported() {
        merchantPreferenceService.isCheckUPIAccountSupported("id");
    }

    @Test
    public void isAddNPayWithUPICollectSupported() {
        merchantPreferenceService.isAddNPayWithUPICollectSupported("id");
    }

    @Test
    public void isCollectBoxEnabledForAddNPay() {
        merchantPreferenceService.isCollectBoxEnabledForAddNPay("id");
    }

    @Test
    public void isCostBasedPreferenceEnabled() {
        merchantPreferenceService.isCostBasedPreferenceEnabled("id");
    }

    @Test
    public void isUpiAppsPayModeDisabled() {
        merchantPreferenceService.isUpiAppsPayModeDisabled("id", true);
    }

    @Test
    public void isBlockCheckoutJS() {
        merchantPreferenceService.isBlockCheckoutJS("id");
    }

    @Test
    public void isAllowRegisteredUserOnlyLogin() {
        merchantPreferenceService.isAllowRegisteredUserOnlyLogin("id");
    }

    @Test
    public void isAutoRefundPreferenceEnabled() {
        merchantPreferenceService.isAutoRefundPreferenceEnabled("id");
    }

    @Test
    public void isCustomerFeedbackEnabled() {
        merchantPreferenceService.isCustomerFeedbackEnabled("id", true);
    }

    @Test
    public void isBlockBulkApplyPromo() {
        merchantPreferenceService.isBlockBulkApplyPromo("id");
    }

    @Test
    public void isFlexiSubscriptionEnabled() {
        merchantPreferenceService.isFlexiSubscriptionEnabled("id", true);
    }

    @Test
    public void isDccEnabledMerchant() {
        merchantPreferenceService.isDccEnabledMerchant("id");
    }

    @Test
    public void isLocationPermission() {
        merchantPreferenceService.isLocationPermission("id");
    }

    @Test
    public void getBankTransferCheckoutFlow() {
        merchantPreferenceService.getBankTransferCheckoutFlow("id");
    }

    @Test
    public void isTxnPaidTimePreferenceEnabled() {
        merchantPreferenceService.isTxnPaidTimePreferenceEnabled("id", true);
    }

    @Test
    public void isEncryptedCardMerchant() {
        merchantPreferenceService.isEncryptedCardMerchant("id");
    }

    @Test
    public void isBlockLinkFPODisablePaymode() {
        merchantPreferenceService.isBlockLinkFPODisablePaymode("id", true);
    }

    @Test
    public void isWalletOnlyMerchant() {
        merchantPreferenceService.isWalletOnlyMerchant("id");
    }

    @Test
    public void isSubWalletSegregationEnabled() {
        merchantPreferenceService.isSubWalletSegregationEnabled("id");

    }
}
