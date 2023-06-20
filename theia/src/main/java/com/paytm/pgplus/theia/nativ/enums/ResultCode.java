/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.enums;

public enum ResultCode {

    SUCCESS("0000", "S", "SUCCESS"), PREFERENCE_KEY_DOES_NOT_EXISTS("7001", "F", "Preference Key Doesn't exists"), VALUE_NOT_FROM_OPTION_LIST(
            "7002", "F", "Preference value not in Option List"), INVALID_LIST_SIZE("7003", "F",
            "Invalid Request List Size"), SYSTEM_ERROR("00000900", "F", "SYSTEM_ERROR");

    private String resultCode;
    private String resultStatus;
    private String resultMsg;

    private ResultCode(String resultCode, String resultStatus, String resultMsg) {
        this.resultStatus = resultStatus;
        this.resultMsg = resultMsg;
        this.resultCode = resultCode;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public static ResultCode getEnumByCode(String code) {
        for (ResultCode rc : values()) {
            if (code.equalsIgnoreCase(rc.getResultCode())) {
                return rc;
            }
        }
        return ResultCode.SYSTEM_ERROR;
    }

}