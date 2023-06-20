package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PaymentS2SResponseHeader extends SecureS2SResponseHeader {

    private static final long serialVersionUID = -8250715116882466439L;

    public PaymentS2SResponseHeader() {
    }

    public PaymentS2SResponseHeader(String version, String responseTimestamp) {
        super(version, responseTimestamp);
    }

    public PaymentS2SResponseHeader(String clientId, String version, String responseTimestamp, String signature) {
        super(clientId, version, responseTimestamp, signature);
    }

}
