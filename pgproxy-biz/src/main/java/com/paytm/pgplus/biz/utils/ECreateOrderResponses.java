/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.utils;

import org.apache.commons.lang3.StringUtils;

public enum ECreateOrderResponses {

    SUCCESS("SUCCESS", "01"), PARAM_ILLEGAL("PARAM_ILLEGAL", "501"), SYSTEM_ERROR("SYSTEM_ERROR", "501"), ACCESS_DENIED(
            "ACCESS_DENIED", "501"), AMOUNT_EXCEEDS_LIMIT("AMOUNT_EXCEEDS_LIMIT", "106"), USER_NOT_EXISTS(
            "USER_NOT_EXISTS", "501"), USER_STATUS_ABNORMAL("USER_STATUS_ABNORMAL", "130"), REPEAT_REQ_INCONSISTENT(
            "REPEAT_REQ_INCONSISTENT", "325"), SUCCESS_IDEMPOTENT_ERROR("SUCCESS_IDEMPOTENT_ERROR", "325"), PARAM_MISSING(
            "PARAM_MISSING", "501"), INVALID_SIGNATURE("INVALID_SIGNATURE", "501"), KEY_NO_FOUND("KEY_NO_FOUND", "501"), NO_INTERFACE_DEF(
            "NO_INTERFACE_DEF", "501"), API_IS_INVALID("API_IS_INVALID", "501"), MSG_PARSE_ERROR("MSG_PARSE_ERROR",
            "501"), OAUTH_FAILED("OAUTH_FAILED", "501"), FUNCTION_NOT_MATCH("FUNCTION_NOT_MATCH", "501"), VERIFY_CLIENT_SECRET_FAIL(
            "VERIFY_CLIENT_SECRET_FAIL", "501"), CLIENT_FORBIDDEN_ACCESS_API("CLIENT_FORBIDDEN_ACCESS_API", "501"), UNKNOWN_CLIENT(
            "UNKNOWN_CLIENT", "501"), INVALID_CLIENT_STATUS("INVALID_CLIENT_STATUS", "501"), ;

    String internalResultCode;
    String externalResponseCode;

    private ECreateOrderResponses(final String internalResultCode, final String externalResponseCode) {
        this.internalResultCode = internalResultCode;
        this.externalResponseCode = externalResponseCode;
    }

    /**
     * @return the internalResultCode
     */
    public String getInternalResultCode() {
        return internalResultCode;
    }

    /**
     * @return the externalResponseCode
     */
    public String getExternalResponseCode() {
        return externalResponseCode;
    }

    public static ECreateOrderResponses getResponseDetailsByCode(final String internalResultCode) throws Exception {
        if (StringUtils.isNotBlank(internalResultCode)) {
            for (final ECreateOrderResponses createOrderResponses : ECreateOrderResponses.values()) {
                if (internalResultCode.equals(createOrderResponses.getInternalResultCode())) {
                    return createOrderResponses;
                }
            }
        }
        return ECreateOrderResponses.SYSTEM_ERROR;
    }
}