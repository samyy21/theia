package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "MerchantInfoResponse", description = "MerchantInfoResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantInfoResponse implements Serializable {

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private MerchantInfoResponseBody body;

    public MerchantInfoResponse() {
    }

    public MerchantInfoResponse(ResponseHeader head, MerchantInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public MerchantInfoResponseBody getBody() {
        return body;
    }

    public void setBody(MerchantInfoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantUserInfoResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
