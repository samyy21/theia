package com.paytm.pgplus.biz.enums;

import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.facade.validators.ParameterValidator;

public enum GenerateEsnEnum {
    SYSTEM_ERROR("SYSTEM_ERROR", "010", "SYSTEM_ERROR"), FAIL("FAIL", "009", "Fail"), SUCCESS("SUCCESS", "001",
            "success"), PLATFORM_ERROR("SYSTEM_ERROR", "011", "Platform System Error");

    String resultCode;
    String resultCodeId;
    String resultMsg;

    private GenerateEsnEnum(String resultCode, String resultCodeId, String resultMsg) {
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

    public static GenerateEsnEnum getGenerateEsnResultByResultCode(final String resultCode)
            throws FacadeInvalidParameterException {
        ParameterValidator.validateInputStringParam(resultCode);
        for (final GenerateEsnEnum iterator : GenerateEsnEnum.values()) {
            if (resultCode.equals(iterator.resultCode)) {
                return iterator;
            }
        }
        throw new FacadeInvalidParameterException("Given value is not recognized in the system");
    }
}
