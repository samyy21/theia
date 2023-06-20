package com.paytm.pgplus.theia.models.upiAccount.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.response.ResponseHeader;
import org.hibernate.validator.constraints.NotBlank;

public class UpiAccountResponse<T> {
    @JsonProperty(value = "head")
    @NotBlank
    private ResponseHeader head;

    @JsonProperty(value = "body")
    @NotBlank
    private T body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
