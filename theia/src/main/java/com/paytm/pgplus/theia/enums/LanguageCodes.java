package com.paytm.pgplus.theia.enums;

public enum LanguageCodes {
    Hindi("2", "hi-IN"), Bangla("3", "bn-IN"), Oriya("4", "or-IN"), Marathi("5", "mr-IN"), Malayalam("6", "ml-IN"), Kannada(
            "7", "kn-IN"), Tamil("8", "ta-IN"), Telugu("9", "te-IN"), Gujarati("10", "gu-IN"), Punjabi("11", "pa-IN");

    private final String langId;
    private final String locale;

    LanguageCodes(String langId, String locale) {
        this.langId = langId;
        this.locale = locale;
    }

    public String getLangId() {
        return langId;
    }

    public String getLocale() {
        return locale;
    }

    public static LanguageCodes getEnumByLocale(String locale) {
        for (LanguageCodes code : values()) {
            if (code.locale.equals(locale)) {
                return code;
            }
        }
        return null;
    }
}
