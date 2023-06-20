package com.paytm.pgplus.theia.cache.model;

import java.io.Serializable;

/**
 * @author amitdubey
 * @date Nov 5, 2016
 */
public class MerchantPreference implements Serializable {
    private static final long serialVersionUID = -7089261384707819466L;

    private String preferenceName;
    private boolean enabled;
    private String preferenceValue;

    /**
     * @return the preferenceName
     */
    public String getPreferenceName() {
        return preferenceName;
    }

    /**
     * @param preferenceName
     *            the preferenceName to set
     */
    public void setPreferenceName(String preferenceName) {
        this.preferenceName = preferenceName;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the preferenceValue
     */
    public String getPreferenceValue() {
        return preferenceValue;
    }

    /**
     * @param preferenceValue
     *            the preferenceValue to set
     */
    public void setPreferenceValue(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MerchantPreference [preferenceName=").append(preferenceName).append(", enabled=")
                .append(enabled).append(", preferenceValue=").append(preferenceValue).append("]");
        return builder.toString();
    }
}