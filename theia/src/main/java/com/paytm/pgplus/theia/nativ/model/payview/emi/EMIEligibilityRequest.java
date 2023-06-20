package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.SecureRequestHeader;

import java.io.Serializable;

/**
 * Created by charu on 07/10/18.
 */

public class EMIEligibilityRequest implements Serializable {

    private static final long serialVersionUID = -5376778700909576998L;

    @JsonProperty("head")
    private SecureRequestHeader head;

    @JsonProperty("body")
    private EMIEligibilityRequestBody body;

    public SecureRequestHeader getHead() {
        return head;
    }

    public EMIEligibilityRequestBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EMIEligibilityRequest{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
