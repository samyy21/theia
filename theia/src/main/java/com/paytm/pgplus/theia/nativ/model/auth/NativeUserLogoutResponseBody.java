package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeUserLogoutResponseBody extends BaseResponseBody {

    private NativeCashierInfoResponseBody cashierData;

    public NativeCashierInfoResponseBody getCashierData() {
        return cashierData;
    }

    public void setCashierData(NativeCashierInfoResponseBody cashierData) {
        this.cashierData = cashierData;
    }

    @Override
    public String toString() {
        return "NativeUserLogoutResponseBody{" + "cashierData=" + cashierData + '}';
    }
}
