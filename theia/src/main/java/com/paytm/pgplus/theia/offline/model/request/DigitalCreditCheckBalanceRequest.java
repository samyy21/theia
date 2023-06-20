package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 17/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditCheckBalanceRequest extends BaseRequest {

    private static final long serialVersionUID = 6392711486341576377L;

    @NotNull
    @Valid
    private RequestHeader head;
    @NotNull
    @Valid
    private DigitalCreditCheckBalanceRequestBody body;

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
        this.head = head;
    }

    public DigitalCreditCheckBalanceRequestBody getBody() {
        return body;
    }

    public void setBody(DigitalCreditCheckBalanceRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DigitalCreditCheckBalanceRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
