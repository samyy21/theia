package com.paytm.pgplus.theia.nativ.model.auth;

import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;

public class ValidateOtpServiceResponse {

    private ValidateLoginOtpResponse validateLoginOtpResponse;
    private NativeCashierInfoResponse cashierInfoResponse;

    public ValidateLoginOtpResponse getValidateLoginOtpResponse() {
        return validateLoginOtpResponse;
    }

    public void setValidateLoginOtpResponse(ValidateLoginOtpResponse validateLoginOtpResponse) {
        this.validateLoginOtpResponse = validateLoginOtpResponse;
    }

    public NativeCashierInfoResponse getCashierInfoResponse() {
        return cashierInfoResponse;
    }

    public void setCashierInfoResponse(NativeCashierInfoResponse cashierInfoResponse) {
        this.cashierInfoResponse = cashierInfoResponse;
    }

    @Override
    public String toString() {
        return "ValidateOtpServiceResponse{" + "validateLoginOtpResponse=" + validateLoginOtpResponse
                + ", cashierInfoResponse=" + cashierInfoResponse + '}';
    }
}
