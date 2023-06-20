package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.paytm.pgplus.common.bankForm.model.BankForm;

@JsonInclude(Include.NON_NULL)
public class PaymentS2SResponseBody extends BaseS2SResponseBody {

    private static final long serialVersionUID = -6000561423782144314L;

    private String orderId;

    private String paymentMode;

    private BankRedirectionDetail bankRequest;

    private BankForm bankForm;

    public PaymentS2SResponseBody() {
    }

    public PaymentS2SResponseBody(BizResultInfo resultInfo) {
        super(resultInfo);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public BankRedirectionDetail getBankRequest() {
        return bankRequest;
    }

    public void setBankRequest(BankRedirectionDetail bankRequest) {
        this.bankRequest = bankRequest;
    }

    public BankForm getBankForm() {
        return bankForm;
    }

    public void setBankForm(BankForm bankForm) {
        this.bankForm = bankForm;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaymentS2SResponseBody{");
        sb.append("resultInfo=").append(resultInfo);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", bankRequest='").append(bankRequest != null ? true : false).append('\'');
        sb.append(", bankForm='").append(bankForm != null ? true : false).append('\'');
        sb.append('}');
        return sb.toString();
    }
}