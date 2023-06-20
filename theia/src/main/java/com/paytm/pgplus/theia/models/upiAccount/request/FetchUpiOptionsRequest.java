package com.paytm.pgplus.theia.models.upiAccount.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUpiOptionsRequest {

    @NotBlank
    private FetchUpiOptionsRequestHeader head;

    @NotBlank
    private FetchUpiOptionsRequestBody body;

    public FetchUpiOptionsRequest() {
    }

    public FetchUpiOptionsRequestHeader getHead() {
        return head;
    }

    public void setHead(FetchUpiOptionsRequestHeader head) {
        this.head = head;
    }

    public FetchUpiOptionsRequestBody getBody() {
        return body;
    }

    public void setBody(FetchUpiOptionsRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
