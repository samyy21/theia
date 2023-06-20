package com.paytm.pgplus.theia.nativ.model.auth;

import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;

public class NativeUserLogoutServiceReqRes {

    private NativeCashierInfoResponse cashierInfoResponse;

    private CreateAccessTokenServiceRequest createAccessTokenServiceRequest;

    public NativeCashierInfoResponse getCashierInfoResponse() {
        return cashierInfoResponse;
    }

    public void setCashierInfoResponse(NativeCashierInfoResponse cashierInfoResponse) {
        this.cashierInfoResponse = cashierInfoResponse;
    }

    public CreateAccessTokenServiceRequest getCreateAccessTokenServiceRequest() {
        return createAccessTokenServiceRequest;
    }

    public void setCreateAccessTokenServiceRequest(CreateAccessTokenServiceRequest createAccessTokenServiceRequest) {
        this.createAccessTokenServiceRequest = createAccessTokenServiceRequest;
    }

}
