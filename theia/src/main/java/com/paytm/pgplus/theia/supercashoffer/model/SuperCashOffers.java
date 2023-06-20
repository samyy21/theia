package com.paytm.pgplus.theia.supercashoffer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperCashOffers implements Serializable {

    private static final long serialVersionUID = 6939579615272226540L;

    @JsonProperty("fetchSupercashOffers")
    private Boolean fetchSupercashOffers;

    @JsonProperty("supercashPayModes")
    List<SuperCashPaymodes> supercashPayModes;

}
