package com.paytm.pgplus.theia.acs.enums;

public enum AcsUrlPlaceholder {

    MID("{midPlaceholder}"), ORDER_ID("{orderIdPlaceholder}"), UNIQUE_ID("{uniqueIdPlaceholder}"), ;

    private final String placeholderValue;

    private AcsUrlPlaceholder(String placeholder) {
        this.placeholderValue = placeholder;
    }

    public String getPlaceholderValue() {
        return placeholderValue;
    }

}
