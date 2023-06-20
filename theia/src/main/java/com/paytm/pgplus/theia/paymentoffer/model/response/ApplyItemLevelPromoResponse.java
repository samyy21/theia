package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;

import java.io.Serializable;

public class ApplyItemLevelPromoResponse implements Serializable {

    private static final long serialVersionUID = -8682008893398125995L;

    private ResponseHeader head;
    private ApplyItemLevelPromoResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public ApplyItemLevelPromoResponseBody getBody() {
        return body;
    }

    public void setBody(ApplyItemLevelPromoResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
