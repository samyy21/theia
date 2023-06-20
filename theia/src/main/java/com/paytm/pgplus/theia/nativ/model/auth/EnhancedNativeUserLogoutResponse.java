package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedNativeUserLogoutResponse implements Serializable {

    private static final long serialVersionUID = -7521049648825284631L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private EnhancedLogoutResponseBody body;

    public EnhancedNativeUserLogoutResponse() {
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public EnhancedLogoutResponseBody getBody() {
        return body;
    }

    public void setBody(EnhancedLogoutResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhancedNativeUserLogoutResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
