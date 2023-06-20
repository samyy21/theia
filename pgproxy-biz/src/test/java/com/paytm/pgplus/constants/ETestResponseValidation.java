package com.paytm.pgplus.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Naman on 23/05/17.
 */
public enum ETestResponseValidation {

    SUCCESSFULLY_PROCESSED("SUCCESSFULLY_PROCESSED", "Boolean"), USER_DETAILS_NOT_NULL("USER_DETAILS_NOT_NULL", "Y"), MERCHANT_SAVED_CARD(
            "MERCHANT_SAVED_CARD", "NUMBER"), ADD_AND_PAY_SAVED_CARD("ADD_AND_PAY_SAVED_CARD", "NUMBER"), MERCHANT_PAY_METHODS(
            "MERCHANT_PAY_METHODS", "PayMethod"), ADD_AND_PAY_PAY_METHODS("ADD_AND_PAY_PAY_METHODS", "PayMethod"), TRANS_ID_NOT_BLANK(
            "TRANS_ID_NOT_BLANK", "Boolean"), QUERY_PAYMENT_STATUS("QUERY_PAYMENT_STATUS", "Y"), QUERY_TRANSACTION_STATUS(
            "QUERY_TRANSACTION_STATUS", "Y"), PAYMENT_DONE("PAYMENT_DONE", "Boolean"), ASSERT_NULL_RESPONSE(
            "ASSERT_NULL_RESPONSE", "Boolean"), BULK_FEE_CONSULT("BULK_FEE_CONSULT", "PayMethod");

    private String name;
    private String value;

    ETestResponseValidation(String name, String value) {

        this.name = name;
        this.value = value;
    }

    public static ETestResponseValidation getTestResponseChecks(final String responseParam) {

        for (final ETestResponseValidation check : ETestResponseValidation.values()) {
            if (StringUtils.isNotBlank(responseParam) && responseParam.equals(check.getName())) {
                return check;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ETestResponseValidation{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
