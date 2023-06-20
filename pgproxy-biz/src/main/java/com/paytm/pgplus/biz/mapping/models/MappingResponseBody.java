/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.mapping.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author vaishakh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingResponseBody<T> {

    private T response;
    private MappingResultInfo resultInfo;

    /**
     * @return the resultInfo
     */
    public MappingResultInfo getResultInfo() {
        return resultInfo;
    }

    /**
     * @return the response
     */
    public T getResponse() {
        return response;
    }

    @JsonCreator
    public MappingResponseBody(@JsonProperty("response") T response,
            @JsonProperty("resultInfo") MappingResultInfo resultInfo) {
        this.response = response;
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        return "MappingResponseBody{" + "response=" + response + ", resultInfo=" + resultInfo + '}';
    }
}
