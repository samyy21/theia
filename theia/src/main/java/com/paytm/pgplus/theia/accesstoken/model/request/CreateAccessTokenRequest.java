package com.paytm.pgplus.theia.accesstoken.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.TokenRequestHeader;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.Serializable;

public class CreateAccessTokenRequest implements Serializable {

    private static final long serialVersionUID = 898657394464013348L;

    @JsonProperty(value = "head")
    @Valid
    @NotBlank
    private TokenRequestHeader head;

    @JsonProperty("body")
    private CreateAccessTokenRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public CreateAccessTokenRequestBody getBody() {
        return body;
    }

    public void setBody(CreateAccessTokenRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("CreateAccessTokenRequest {");
        sb.append(" head='").append(head.toString()).append('\'').append(", body='").append(body.toString())
                .append('\'').append('}');
        return sb.toString();
    }
}
