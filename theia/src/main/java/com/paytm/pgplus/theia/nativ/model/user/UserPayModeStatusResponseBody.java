package com.paytm.pgplus.theia.nativ.model.user;

import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

import java.util.List;

public class UserPayModeStatusResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 7560771227029830590L;

    private List<UserPayModeStatus> paymentModeStatus;

    public UserPayModeStatusResponseBody() {
    }

    public UserPayModeStatusResponseBody(List<UserPayModeStatus> paymentModeStatus) {
        this.paymentModeStatus = paymentModeStatus;
    }

    public UserPayModeStatusResponseBody(ResultInfo resultInfo, List<UserPayModeStatus> paymentModeStatus) {
        super(resultInfo);
        this.paymentModeStatus = paymentModeStatus;
    }

    public List<UserPayModeStatus> getPaymentModeStatus() {
        return paymentModeStatus;
    }

    public void setPaymentModeStatus(List<UserPayModeStatus> paymentModeStatus) {
        this.paymentModeStatus = paymentModeStatus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPayModeStatusResponseBody{");
        sb.append("paymentModeStatus=").append(paymentModeStatus);
        sb.append('}');
        return sb.toString();
    }
}
