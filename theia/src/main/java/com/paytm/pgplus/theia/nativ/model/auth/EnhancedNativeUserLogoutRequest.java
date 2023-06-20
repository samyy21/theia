package com.paytm.pgplus.theia.nativ.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedNativeUserLogoutRequest implements Serializable {

    private static final long serialVersionUID = 4974278680030515557L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    private EnhancedNativeUserLogoutRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public EnhancedNativeUserLogoutRequestBody getBody() {
        return body;
    }

    public void setBody(EnhancedNativeUserLogoutRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhancedNativeUserLogoutRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
