package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusRequest implements Serializable {

    private static final long serialVersionUID = -689374653646037425L;
    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private TransactionStatusRequestBody body;

    @JsonIgnore
    private transient HttpServletRequest httpServletRequest;

    @JsonIgnore
    private transient InitiateTransactionRequestBody orderDetail;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public TransactionStatusRequestBody getBody() {
        return body;
    }

    public void setBody(TransactionStatusRequestBody body) {
        this.body = body;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public InitiateTransactionRequestBody getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(InitiateTransactionRequestBody orderDetail) {
        this.orderDetail = orderDetail;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionStatusRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
