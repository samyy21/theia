/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.enums;

import com.paytm.pgplus.response.ResultInfo;
import org.apache.commons.lang.StringUtils;

public enum UpsTheiaResponseMapping {
    SUCCESS("200", "UPS_0001", ResultCode.SUCCESS), CATEGORY_NOT_FOUND("200", "UPS_0018",
            ResultCode.PREFERENCE_KEY_DOES_NOT_EXISTS), CATEGORY_PREFERENCE_NOT_FOUND("200", "UPS_0021",
            ResultCode.PREFERENCE_KEY_DOES_NOT_EXISTS), VALUE_NOT_FROM_OPTION_LIST("200", "UPS_0023",
            ResultCode.VALUE_NOT_FROM_OPTION_LIST), INVALID_REQUEST_LIST_SIZE("200", "UPS_1209",
            ResultCode.INVALID_LIST_SIZE), INVALID_PREFERENCE_KEY("400", "UPS_0024",
            ResultCode.PREFERENCE_KEY_DOES_NOT_EXISTS);

    private String code;
    private String upsErrorCode;
    private ResultCode resultCode;

    public String getUpsErrorCode() {
        return upsErrorCode;
    }

    public String getCode() {
        return code;
    }

    public static ResultCode fetchResponseConstantByCode(String upsErrorCode) {
        if (StringUtils.isNotBlank(upsErrorCode)) {
            for (UpsTheiaResponseMapping constants : UpsTheiaResponseMapping.values()) {
                if (upsErrorCode.equals(constants.getUpsErrorCode())) {
                    return constants.resultCode;
                }
            }
        }
        return ResultCode.SYSTEM_ERROR;
    }

    public static ResultInfo fetchResultInfoByCode(String upsErrorCode) {
        if (StringUtils.isNotBlank(upsErrorCode)) {
            for (UpsTheiaResponseMapping constants : UpsTheiaResponseMapping.values()) {
                if (upsErrorCode.equals(constants.getUpsErrorCode())) {
                    ResultCode resultCode = constants.resultCode;
                    return new ResultInfo(resultCode.getResultStatus(), resultCode.getResultCode(),
                            resultCode.getResultMsg());
                }
            }
        }
        ResultCode resultCode = ResultCode.SYSTEM_ERROR;
        return new ResultInfo(resultCode.getResultStatus(), resultCode.getResultCode(), resultCode.getResultMsg());
    }

    public static ResultCode fetchResponseConstantByhttpCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (UpsTheiaResponseMapping constants : UpsTheiaResponseMapping.values()) {
                if (code.equals(constants.getCode())) {
                    return constants.resultCode;
                }
            }
        }
        return ResultCode.SYSTEM_ERROR;
    }

    UpsTheiaResponseMapping(String code, String upsErrorCode, ResultCode resultCode) {
        this.code = code;
        this.upsErrorCode = upsErrorCode;
        this.resultCode = resultCode;
    }

}