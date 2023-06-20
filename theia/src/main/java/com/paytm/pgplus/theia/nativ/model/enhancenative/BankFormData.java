package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import java.io.Serializable;

/**
 * Created by rahulverma on 7/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankFormData extends BaseResponse {
    private static final long serialVersionUID = -4218958012611582746L;
    ResponseHeader head;
    BankRedirectionDetail body;

    public BankFormData() {
        this.head = new ResponseHeader();
    }

    public BankFormData(BankRedirectionDetail body) {
        this.body = body;
        this.head = new ResponseHeader();
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public BankRedirectionDetail getBody() {
        return body;
    }

    public void setBody(BankRedirectionDetail body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "BankFormData{" + "head=" + head + ", body=" + body + '}';
    }
}
