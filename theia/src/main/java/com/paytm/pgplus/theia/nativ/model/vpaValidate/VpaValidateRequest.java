package com.paytm.pgplus.theia.nativ.model.vpaValidate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@ApiModel(value = "VpaValidateRequest", description = "Request for validating vpa")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpaValidateRequest implements Serializable {

    private static final long serialVersionUID = -8032687710814805135L;
    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private VpaValidateRequestBody body;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public VpaValidateRequestBody getBody() {
        return body;
    }

    public void setBody(VpaValidateRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
