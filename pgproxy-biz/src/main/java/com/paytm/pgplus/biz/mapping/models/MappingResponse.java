/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingResponse<T> {

    private MappingServiceResponseHeader head;
    private MappingResponseBody<T> body;

    /**
     * @return the head
     */
    @JsonProperty("head")
    public MappingServiceResponseHeader getHead() {
        return head;
    }

    /**
     * @return the body
     */
    @JsonProperty("body")
    public MappingResponseBody<T> getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MappingResponse{" + "head=" + head + ", body=" + body + '}';
    }

}
