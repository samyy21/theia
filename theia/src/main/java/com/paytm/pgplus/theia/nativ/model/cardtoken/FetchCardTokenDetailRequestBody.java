package com.paytm.pgplus.theia.nativ.model.cardtoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchCardTokenDetailRequestBody implements Serializable {

    @JsonProperty
    private String tokenIndexNumber;

    @JsonProperty
    private String orderId;

    @Override
    public String toString() {
        return "CardTokenDetailRequestBody{" + "tokenIndexNumber='" + tokenIndexNumber + '\'' + ", orderId='" + orderId
                + '\'' + '}';
    }
}
