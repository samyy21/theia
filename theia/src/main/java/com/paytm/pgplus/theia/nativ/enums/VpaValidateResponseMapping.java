package com.paytm.pgplus.theia.nativ.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum VpaValidateResponseMapping {
    INVALID_VIRTUAL_ADDRESS("37", "Invalid VPA, Try Again", "Invalid UPI Number, Try Again"), INVALID_PAYEE_NAME(
            "INT-1074", "Merchant VPA incorrect", "Merchant VPA incorrect"), RESPAUTHDETAIL_TXN_NOT_PERMITTED_TO_VPA(
            "ZE", "VPA restricted. Try again.", "UPI Number restricted. Try again."), FAILURE("INT-1766",
            "Error in Verification, Try Again", "UPI Number does not exist"), DEFAULT_ERROR("",
            "Error in Verification, Try Again", "Error in Verification, Try Again");
    private String code;
    private String vpaErrorMsg;
    private String numericIdErrorMsg;

    private static final Map<String, VpaValidateResponseMapping> findByCode = new HashMap<>();

    public String getCode() {
        return code;
    }

    public String getVpaErrorMsg() {
        return vpaErrorMsg;
    }

    public String getNumericIdErrorMsg() {
        return numericIdErrorMsg;
    }

    static {
        for (VpaValidateResponseMapping s : EnumSet.allOf(VpaValidateResponseMapping.class)) {
            findByCode.put(s.getCode(), s);
        }
    }

    VpaValidateResponseMapping(String code, String vpaErrorMsg, String numericIdErrorMsg) {
        this.code = code;
        this.vpaErrorMsg = vpaErrorMsg;
        this.numericIdErrorMsg = numericIdErrorMsg;
    }

    public static VpaValidateResponseMapping get(String resultCodeId) {
        return findByCode.get(resultCodeId);
    }
}
