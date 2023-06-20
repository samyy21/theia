package com.paytm.pgplus.theia.nativ.model.cardtoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTokenDetailResponse implements Serializable {

    @JsonProperty("head")
    private ResponseHeader head;

    @JsonProperty
    private CardTokenDetailResponseBody body;

}
