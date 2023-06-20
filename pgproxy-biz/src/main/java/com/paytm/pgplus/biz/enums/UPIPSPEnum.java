package com.paytm.pgplus.biz.enums;

import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.validators.ParameterValidator;

/**
 * @author Santosh chourasia
 *
 */
public enum UPIPSPEnum {
    SYSTEM_ERROR("SYSTEM_ERROR", "010", "payment fail due to some technical error"), FAIL("FAIL", "009",
            "payment Failure"), SUCCESS("SUCCESS", "001", "success"), ORDER_NOT_FOUND_FAIL("FAIL", "005",
            "Order doesn't exist");

    String resultCode;
    String resultCodeId;
    String resultMsg;

    private UPIPSPEnum(String resultCode, String resultCodeId, String resultMsg) {
        this.resultCode = resultCode;
        this.resultCodeId = resultCodeId;
        this.resultMsg = resultMsg;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public static UPIPSPEnum getUPIPSPResultByResultCode(final String resultCode)
            throws FacadeInvalidParameterException {
        ParameterValidator.validateInputStringParam(resultCode);
        for (final UPIPSPEnum iterator : UPIPSPEnum.values()) {
            if (resultCode.equals(iterator.resultCode)) {
                return iterator;
            }
        }
        throw new FacadeInvalidParameterException("Given value is not recognized in the system");
    }
}
