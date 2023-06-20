package com.paytm.pgplus.theia.offline.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.pgproxycommon.enums.NativeValidationExceptionType;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class BinDetailException extends BaseException {

    private static final long serialVersionUID = -1413749389329942042L;

    public BinDetailException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public BinDetailException() {
        super();
    }

    public static BinDetailException getException() {
        return new BinDetailException(OfflinePaymentUtils.resultInfo(ResultCode.BIN_NUMBER_EXCEPTION));
    }

    public static BinDetailException getException(ResultCode resultCode) {
        return new BinDetailException(OfflinePaymentUtils.resultInfo(resultCode));
    }

    public static BinDetailException getException(ResultCode resultCode, String msg) {
        ResultInfo resultInfo = OfflinePaymentUtils.resultInfo(resultCode);
        resultInfo.setResultMsg(msg);
        return new BinDetailException(resultInfo);
    }

    @Override
    public NativeValidationExceptionType getNativeValidationType() {
        return NativeValidationExceptionType.Native_Bin_Detail_Fetching_Exception;
    }
}
