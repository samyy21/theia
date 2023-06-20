package com.paytm.pgplus.theia.csrf;

/**
 * Created by ankitsinghal on 05/06/17.
 */
public enum CSRFConfiguration {
    DISABLED("0"), ENABLED("1"), ONLY_LOGGING("2");

    private String value;

    CSRFConfiguration(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CSRFConfiguration getConfigurationByValue(final String value) {
        if (value != null) {
            for (CSRFConfiguration element : CSRFConfiguration.values()) {
                if (value.equals(element.getValue())) {
                    return element;
                }
            }
        }
        // Return DISABLED as default.
        return DISABLED;
    }
}
