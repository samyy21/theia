package com.paytm.pgplus.theia.supercashoffer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperCashOfferResponseBody implements Serializable {

    private static final long serialVersionUID = 5451983800787594336L;

    @JsonProperty("supercashPayModes")
    private List<SuperCashPaymodes> supercashPayModes;

}
