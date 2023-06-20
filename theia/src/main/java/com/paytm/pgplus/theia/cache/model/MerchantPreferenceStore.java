package com.paytm.pgplus.theia.cache.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author amitdubey
 * @date Nov 5, 2016
 */
public class MerchantPreferenceStore implements Serializable {
    private static final long serialVersionUID = 8540697572173244249L;

    private Map<String, MerchantPreference> preferences;
    private String merchantId;

    public MerchantPreferenceStore(String merchantId) {
        this.merchantId = merchantId;
        this.preferences = new HashMap<>();
    }

    public MerchantPreferenceStore() {
        this.preferences = new HashMap<>();
    }

    /**
     * @return the preferences
     */
    public Map<String, MerchantPreference> getPreferences() {
        return preferences;
    }

    /**
     * @param preferences
     *            the preferences to set
     */
    public void setPreferences(Map<String, MerchantPreference> preferences) {
        this.preferences = preferences;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @param merchantId
     *            the merchantId to set
     */
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantPreferenceStore [preferences=").append(preferences).append(", merchantId=")
                .append(merchantId).append("]");
        return builder.toString();
    }

}
