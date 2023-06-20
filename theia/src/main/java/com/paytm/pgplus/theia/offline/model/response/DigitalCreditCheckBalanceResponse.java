package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 17/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditCheckBalanceResponse extends BaseResponse {

    private static final long serialVersionUID = -1442980937400306235L;
    @NotNull
    @Valid
    private ResponseHeader head;
    @NotNull
    @Valid
    private DigitalCreditCheckBalanceResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public DigitalCreditCheckBalanceResponseBody getBody() {
        return body;
    }

    public void setBody(DigitalCreditCheckBalanceResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DigitalCreditCheckBalanceResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
