package com.paytm.pgplus.theia.enums;

public enum CybersourceResponseEnum {
    AUTHORIZED("201", "201", "AUTHORIZED", "Authorized"),

    EXPIRED_CARD("201", "300", "EXPIRED_CARD", "Decline - Expired card"), INVALID_ACCOUNT("201", "301",
            "INVALID_ACCOUNT", "Decline - Invalid account number"), PROCESSOR_DECLINED("201", "302",
            "PROCESSOR_DECLINED",
            "Decline - General decline of the card. No other information provided by the issuing bank"), INSUFFICIENT_FUND(
            "201", "303", "INSUFFICIENT_FUND", "Decline - Insufficient funds in the account"), STOLEN_LOST_CARD("201",
            "304", "STOLEN_LOST_CARD", "Decline - Stolen or lost card"), ISSUER_UNAVAILABLE("201", "305",
            "ISSUER_UNAVAILABLE", "Decline - Issuer unavailable"), UNAUTHORIZED_CARD("201", "306", "UNAUTHORIZED_CARD",
            "Decline - Unauthorized card"), EXCEEDS_CREDIT_LIMIT("201", "307", "EXCEEDS_CREDIT_LIMIT",
            "Decline - Credit limit exceeded"), DECLINED_CHECK("201", "308", "DECLINED_CHECK", "Generic decline"), BLACKLISTED_CUSTOMER(
            "201", "309", "BLACKLISTED_CUSTOMER", "Decline - Customer blacklisted due to some unknown reason"), SUSPENDED_ACCOUNT(
            "201", "310", "SUSPENDED_ACCOUNT", "Decline - Customer's account is frozen"), PAYMENT_REFUSED("201", "311",
            "PAYMENT_REFUSED", "Decline - Payer’s account is not set up to process such transactions"), GENERAL_DECLINE(
            "201", "312", "GENERAL_DECLINE", "General decline"), SCORE_EXCEEDS_THRESHOLD("201", "313",
            "SCORE_EXCEEDS_THRESHOLD", "Decline - Fraud score exceeds threshold"), AUTHORIZED_RISK_DECLINED("201",
            "314", "AUTHORIZED_RISK_DECLINED", "Decline - Risk rejected"), PENDING_REVIEW("201", "315",
            "PENDING_REVIEW", "Pending Review"), DECLINED("201", "316", "DECLINED", "Declined by bank"), INVALID_REQUEST(
            "201", "317", "INVALID_REQUEST", "Invalid request"), ZERO_SUCCESS_RATE("201", "318", "ZERO_SUCCESS_RATE",
            "Decline - Zero success rate."),

    MISSING_FIELD("400", "410", "MISSING_FIELD", "Declined - The request is missing one or more fields"), INVALID_DATA(
            "400", "411", "INVALID_DATA", "Declined - One or more fields in the request contains invalid data"), DUPLICATE_REQUEST(
            "400", "412", "DUPLICATE_REQUEST", "Duplicate request"), INVALID_CARD("400", "413", "INVALID_CARD",
            "Invalid account number"), CARD_TYPE_NOT_ACCEPTED("400", "414", "CARD_TYPE_NOT_ACCEPTED",
            "The card type is not accepted"),

    UNKNOWN_RESPONSE("500", "500", "UNKNOWN_RESPONSE", "Unknown reason");

    private String cybersourceResponseCode;
    private String responseCode;
    private String responseStatus;
    private String responseMessage;

    private CybersourceResponseEnum(String cybersourceResponseCode, String responseCode, String responseStatus,
            String responseMessage) {
        this.cybersourceResponseCode = cybersourceResponseCode;
        this.responseCode = responseCode;
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
    }

    public static CybersourceResponseEnum getByCodeAndStatus(String code, String status) {
        for (CybersourceResponseEnum responseEnum : CybersourceResponseEnum.values()) {
            if (responseEnum.getCybersourceResponseCode().equals(code)
                    && responseEnum.getResponseStatus().equalsIgnoreCase(status)) {
                return responseEnum;
            }
        }
        return getDefaultUnknownResponse();
    }

    public static CybersourceResponseEnum getDefaultUnknownResponse() {
        return UNKNOWN_RESPONSE;
    }

    public String getCybersourceResponseCode() {
        return cybersourceResponseCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
