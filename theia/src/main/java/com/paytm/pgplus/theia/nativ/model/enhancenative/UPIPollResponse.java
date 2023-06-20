package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

import java.io.Serializable;

/**
 * Created by rahulverma on 7/3/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIPollResponse extends BaseResponse {
    private static final long serialVersionUID = -8711881765332301625L;

    ResponseHeader head;
    UPIPollResponseBody body;

    public UPIPollResponse(ResponseHeader head, UPIPollResponseBody body) {
        this.head = head;
        this.body = body;
    }

    public UPIPollResponse(UPIPollResponseBody body) {
        this.body = body;
        this.head = new ResponseHeader();
    }

    public UPIPollResponse() {
        this.head = new ResponseHeader();
    }

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public UPIPollResponseBody getBody() {
        return body;
    }

    public void setBody(UPIPollResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "UPIPollResponse{" + "head=" + head + ", body=" + body + '}';
    }
}
