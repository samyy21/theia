/**
 *
 */
package com.paytm.pgplus.theia.supercashoffer.enums;

import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;

/**
 * @author Ankit
 */

public enum SupercashResponseConstants {

    SUCCESS_RESPONSE_CODE("0000", "Txn Success", SystemResponseCode.SUCCESS_RESPONSE_CODE), ILLEGAL_PARAMS("4001",
            "illegal parameters", SystemResponseCode.PARAM_ILLEGAL), INTERNAL_PROCESSING_ERROR("5001",
            "internal server error", SystemResponseCode.INTERNAL_PROCESSING_ERROR), MERCHANT_NOT_ELIGIBLE("4003",
            "Merchant not eligible", SystemResponseCode.INVALID_MID), REQUEST_CREATION_ERROR("4002",
            "error while preparing request", SystemResponseCode.INTERNAL_PROCESSING_ERROR), MAPPING_SERVICE_ERROR(
            "4004", "error while getting data from mapping service", SystemResponseCode.INTERNAL_PROCESSING_ERROR), INVALID_TOKEN(
            "4005", "invalid sso token", SystemResponseCode.PARAM_ILLEGAL);

    private String code;
    private String message;
    private SystemResponseCode systemResponseCode;

    public SystemResponseCode getSystemResponseCode() {
        return systemResponseCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    SupercashResponseConstants(String code, String message, SystemResponseCode systemResponseCode) {
        this.code = code;
        this.message = message;
        this.systemResponseCode = systemResponseCode;
    }

}
