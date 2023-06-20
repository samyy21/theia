package com.paytm.pgplus.theia.nativ.model.common;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;

public class CashierInfoContainerRequest {

    private CashierInfoRequest cashierInfoRequest;
    private PaymentRequestBean paymentRequestBean;

    public CashierInfoContainerRequest(CashierInfoRequest cashierInfoRequest) {
        this.cashierInfoRequest = cashierInfoRequest;
    }

    public CashierInfoContainerRequest(CashierInfoRequest cashierInfoRequest, PaymentRequestBean paymentRequestBean) {
        this.cashierInfoRequest = cashierInfoRequest;
        this.paymentRequestBean = paymentRequestBean;
    }

    public CashierInfoRequest getCashierInfoRequest() {
        return cashierInfoRequest;
    }

    public void setCashierInfoRequest(CashierInfoRequest cashierInfoRequest) {
        this.cashierInfoRequest = cashierInfoRequest;
    }

    public PaymentRequestBean getPaymentRequestBean() {
        return paymentRequestBean;
    }

    public void setPaymentRequestBean(PaymentRequestBean paymentRequestBean) {
        this.paymentRequestBean = paymentRequestBean;
    }

    @Override
    public String toString() {
        return "CashierInfoContainerRequest{" + "cashierInfoRequest=" + cashierInfoRequest + ", paymentRequestBean="
                + paymentRequestBean + '}';
    }

}
