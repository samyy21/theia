package com.paytm.pgplus.biz.enums;

/**
 * Created by charu on 11/05/20.
 */

public enum BinConfigAttributesEnum {

    EIGHT_DIGIT_BIN_HASH("eightDigitBinHash", "eightDigitBinHash"), ONE_CLICK_SUPPORTED("oneClickSupported",
            "ONE_CLICK_SUPPORTED"), PREPAID_CARD("prepaid card", "PREPAID_CARD"), CORPORATE_CARD("corporateCard",
            "corporate card"), INDIAN("Supporting Region", "INDIAN"), CUSTOM_DISPLAY_NAME("customDisplayName",
            "CUSTOM_DISPLAY_NAME"), ZERO_SUCCESS_RATE("ZeroSuccessRate", "ZERO_SUCCESS_RATE"), INST_NAME("instName",
            "instName"), COUNTRY_CODE("countryCode", "COUNTRY_CODE"), COUNTRY("country", "COUNTRY"), COUNTRY_CODE_ISO(
            "countryCodeIso", "COUNTRY_CODE_ISO"), CURRENCY("currency", "CURRENCY"), CURRENCY_CODE("currencyCode",
            "CURRENCY_CODE"), CURRENCY_CODE_ISO("currencyCodeIso", "CURRENCY_CODE_ISO"), SYMBOL("symbol", "SYMBOL"), CURRENCY_PRECISION(
            "currencyPrecision", "CURRENCY_PRECISION"), CATEGORY("category", "CATEGORY"), ACCOUNT_RANGE_CARD_BIN(
            "accountRangeCardBin", "accountRangeCardBin");

    private String description;
    private String value;

    BinConfigAttributesEnum(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
