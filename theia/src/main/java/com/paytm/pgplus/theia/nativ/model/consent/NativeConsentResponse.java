package com.paytm.pgplus.theia.nativ.model.consent;

import java.io.Serializable;

/**
 * Created by: satyamsinghrajput at 23/10/19
 */
public class NativeConsentResponse implements Serializable {

    private static final long serialVersionUID = 7535606701067840685L;

    private NativeConsentResponseHead head;
    private NativeConsentResponseBody body;

    public NativeConsentResponseHead getHead() {
        return head;
    }

    public void setHead(NativeConsentResponseHead head) {
        this.head = head;
    }

    public NativeConsentResponseBody getBody() {
        return body;
    }

    public void setBody(NativeConsentResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NativeConsentResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
