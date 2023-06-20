package com.paytm.pgplus.theia.supercashoffer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.supercashoffers.models.SuperCashOfferPaymode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SuperCashOfferRequestBody implements Serializable {

    private static final long serialVersionUID = 540507140101672908L;

    @JsonProperty("paymodes")
    List<SuperCashOfferPaymode> payModes;

    @JsonProperty("mid")
    String mid;

    @JsonProperty("userId")
    private String userId;

    Map<String, String> promoContext;
    String source;
    String amount;
    boolean hybrid;

}
