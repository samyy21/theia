package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AoaTimeoutCenterOrderResponse {
    private static final long serialVersionUID = -7415732875931254475L;

    @JsonProperty("response")
    private List<OrderInfoResponse> response;

    public List<OrderInfoResponse> getResponse() {
        return response;
    }

    public void setResponse(List<OrderInfoResponse> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "AoaTimeoutCenterOrderResponseBody{" + "response=" + response + '}';
    }
}
