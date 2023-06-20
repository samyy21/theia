package com.paytm.pgplus.theia.enums;

public enum CardValidationStatus {
    AUTHORIZED("0000", "S", "Success", "AUTHORIZED", "S"), DECLINED("0000", "S", "Success", "DECLINED", "F"), INVALID_REQUEST(
            "0000", "S", "Success", "INVALID_REQUEST", "F"), SERVER_ERROR("00000900", "U", "System error",
            "SERVER_ERROR", "U"), FAILED("0001", "F", "FAILED", "FAILED", "F"), UNKNOWN("0000", "S", "Success",
            "UNKNOWN", "U");
    private String resultCodeId;
    private String resultStatus;
    private String resultMsg;
    private String code;
    private String performanceStatus;

    /**
     * @param resultCodeId
     * @param resultStatus
     * @param resultMsg
     * @param resultCode
     */
    private CardValidationStatus(String resultCodeId, String resultStatus, String resultMsg, String resultCode,
            String performanceStatus) {
        this.resultCodeId = resultCodeId;
        this.resultStatus = resultStatus;
        this.resultMsg = resultMsg;
        this.code = resultCode;
        this.performanceStatus = performanceStatus;
    }

    /**
     * @return the resultCodeId
     */
    public String getResultCodeId() {
        return resultCodeId;
    }

    /**
     * @return the resultStatus
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the resultMsg
     */
    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * @return the resultCode
     */
    public String getCode() {
        return code;
    }

    public String getPerformanceStatus() {
        return performanceStatus;
    }
}