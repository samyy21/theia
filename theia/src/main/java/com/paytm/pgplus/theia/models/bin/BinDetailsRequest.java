package com.paytm.pgplus.theia.models.bin;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

public class BinDetailsRequest {

    private BinDetailsRequestHead head;

    private BinDetailsRequestBody body;

    public BinDetailsRequestHead getHead() {
        return head;
    }

    public void setHead(BinDetailsRequestHead head) {
        this.head = head;
    }

    public BinDetailsRequestBody getBody() {
        return body;
    }

    public void setBody(BinDetailsRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
