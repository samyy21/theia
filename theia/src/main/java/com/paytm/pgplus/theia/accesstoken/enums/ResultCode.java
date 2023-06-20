package com.paytm.pgplus.theia.accesstoken.enums;

public enum ResultCode {

    SUCCESS("0000", "S", "Success", "SUCCESS"), FAILED("0009", "F", "Failed", "FAILED"), UNKNOWN_ERROR("9999", "F",
            "Something went wrong.", "UNKNOWN_ERROR"), SYSTEM_ERROR("00000900", "U", "System Error.", "SYSTEM_ERROR"),

    REQUEST_PARAMS_VALIDATION("1001", "F", "Request parameters are not valid.", "REQUEST_PARAMS_VALIDATION"), MISSING_MANDATORY_ELEMENT(
            "1007", "F", "Missing mandatory element.", "MISSING_MANDATORY_ELEMENT"), SESSION_EXPIRED("1006", "F",
            "Your Session has expired.", "SESSION_EXPIRED"), INCONSISTENT_REPEAT_REQUEST("2023", "F",
            "Repeat Request Inconsistent.", "INCONSISTENT_REPEAT_REQUEST"), INVALID_MID("2006", "F", "Invalid Mid.",
            "INVALID_MID"), INVALID_TOKEN_TYPE("2021", "F", "Invalid token type.", "INVALID_TOKEN_TYPE"), TOKEN_VALIDATION_FAILED(
            "1002", "F", "Token validation failed.", "TOKEN_VALIDATION_FAILED"), INVALID_ACCESS_TOKEN("2100", "F",
            "Invalid Access Token.", "INVALID_ACCESS_TOKEN"), INVALID_SSO_TOKEN("2004", "F", "Invalid SSO Token.",
            "INVALID_SSO_TOKEN"), INVALID_CHECKSUM("2005", "F", "Invalid Checksum.", "INVALID_CHECKSUM"), INVALID_FK_SSO_TOKEN(
            "2004", "F", "Invalid SSO Token.", "INVALID_FK_SSO_TOKEN"), INVALID_CARD_PREAUTH_TYPE("1001", "F",
            "Invalid cardPreAuthType", "INVALID_CARD_PREAUTH_TYPE"), INVALID_PREAUTH_BLOCK_SECONDS("1001", "F",
            "Invalid preAuthBlockSeconds", "INVALID_PREAUTH_BLOCK_SECONDS");

    private String id;
    private String status;
    private String message;
    private String code;

    /**
     * @param id
     * @param status
     * @param message
     * @param resultCode
     */
    private ResultCode(String id, String status, String message, String resultCode) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.code = resultCode;
    }

    /**
     * @return the resultCodeId
     */
    public String getId() {
        return id;
    }

    /**
     * @return the resultStatus
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the resultMsg
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the resultCode
     */
    public String getCode() {
        return code;
    }

}
