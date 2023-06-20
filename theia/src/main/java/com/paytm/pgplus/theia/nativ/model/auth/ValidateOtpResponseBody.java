package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;

import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponseBody;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateOtpResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -4140758923054531338L;

    public ValidateOtpResponseBody() {
    }

    private boolean isAuthenticated;

    @JsonProperty("cashierData")
    private NativeCashierInfoResponseBody cashierData;

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public NativeCashierInfoResponseBody getCashierData() {
        return cashierData;
    }

    public void setCashierData(NativeCashierInfoResponseBody cashierData) {
        this.cashierData = cashierData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpResponseBody{");
        sb.append("isAuthenticated=").append(isAuthenticated);
        sb.append(", cashierData=").append(cashierData);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
