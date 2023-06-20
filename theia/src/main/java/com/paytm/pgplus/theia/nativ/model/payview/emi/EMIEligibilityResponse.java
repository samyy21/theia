package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.SecureResponseHeader;

import java.io.Serializable;

/**
 * Created by charu on 07/10/18.
 */

public class EMIEligibilityResponse implements Serializable {

    private static final long serialVersionUID = -896841413043963022L;

    @JsonProperty("head")
    private SecureResponseHeader head;

    @JsonProperty("body")
    private EMIEligibilityResponseBody body;

    public EMIEligibilityResponse(SecureResponseHeader head, EMIEligibilityResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public EMIEligibilityResponseBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EMIEligibilityResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
