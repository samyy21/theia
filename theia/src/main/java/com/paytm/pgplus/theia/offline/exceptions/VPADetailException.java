package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class VPADetailException extends BaseException {

    private static final long serialVersionUID = -1413749389329942042L;

    public VPADetailException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public VPADetailException() {
        super();
    }

    public static VPADetailException getException() {
        return new VPADetailException(OfflinePaymentUtils.resultInfo(ResultCode.VPA_NOT_FOUND_EXCEPTION));
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_ProfileVPA_Fetching_Exception;
    }
}
