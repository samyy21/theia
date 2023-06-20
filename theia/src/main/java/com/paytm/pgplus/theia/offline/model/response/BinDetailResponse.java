/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetailResponse extends BaseResponse {

    private static final long serialVersionUID = -83475041679782968L;

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty("body")
    private BinDetailResponseBody body;

    public ResponseHeader getHead() {
        return head;
    }

    public void setHead(ResponseHeader head) {
        this.head = head;
    }

    public BinDetailResponseBody getBody() {
        return body;
    }

    public void setBody(BinDetailResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinDetailResponse{");
        sb.append("head=").append(head);
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
