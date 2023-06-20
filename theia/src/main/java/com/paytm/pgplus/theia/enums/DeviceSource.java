package com.paytm.pgplus.theia.enums;

/**
 * Created by prashant on 4/8/16.
 */
public enum DeviceSource {
    PG("PG"), PGPLUS("PGPLUS"), BLANK("BLANK");

    private String value;

    DeviceSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeviceSource getDeviceSourceByValue(final String source) {
        if (source != null) {
            for (DeviceSource element : DeviceSource.values()) {
                if (source.equals(element.getValue())) {
                    return element;
                }
            }
        }
        return BLANK;
    }
}
