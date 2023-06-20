package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;
import com.paytm.pgplus.theia.oltpu.models.MerchantRequest;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Created by anamika on 22/10/18.
 */
public class ValidateVpaRequest implements Serializable {

    private static final long serialVersionUID = -7058198477030488002L;

    @JsonProperty("head")
    private SecureRequestHeader head;

    @JsonProperty("body")
    private MerchantRequest body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public void setHead(SecureRequestHeader head) {
        this.head = head;
    }

    public MerchantRequest getBody() {
        return body;
    }

    public void setBody(MerchantRequest body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}
