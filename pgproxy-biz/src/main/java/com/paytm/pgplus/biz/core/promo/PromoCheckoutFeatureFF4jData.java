package com.paytm.pgplus.biz.core.promo;

public class PromoCheckoutFeatureFF4jData {

    private boolean success;
    private boolean featurePromoCheckoutRetryFlagEnabled;
    private boolean featurePromoSaveCheckoutDataFlagEnabled;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFeaturePromoCheckoutRetryFlagEnabled() {
        return featurePromoCheckoutRetryFlagEnabled;
    }

    public void setFeaturePromoCheckoutRetryFlagEnabled(boolean featurePromoCheckoutRetryFlagEnabled) {
        this.featurePromoCheckoutRetryFlagEnabled = featurePromoCheckoutRetryFlagEnabled;
    }

    public boolean isFeaturePromoSaveCheckoutDataFlagEnabled() {
        return featurePromoSaveCheckoutDataFlagEnabled;
    }

    public void setFeaturePromoSaveCheckoutDataFlagEnabled(boolean featurePromoSaveCheckoutDataFlagEnabled) {
        this.featurePromoSaveCheckoutDataFlagEnabled = featurePromoSaveCheckoutDataFlagEnabled;
    }
}
