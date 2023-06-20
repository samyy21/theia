package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "MerchantUserInfoResponse", description = "MerchantUserInfoResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantUserInfoResponse implements Serializable {

    private static final long serialVersionUID = -2721981909117156303L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private MerchantUserInfoResponseBody body;

    public MerchantUserInfoResponse() {
    }

    public MerchantUserInfoResponse(ResponseHeader head, MerchantUserInfoResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public MerchantUserInfoResponseBody getBody() {
        return body;
    }

    public void setBody(MerchantUserInfoResponseBody body) {
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
