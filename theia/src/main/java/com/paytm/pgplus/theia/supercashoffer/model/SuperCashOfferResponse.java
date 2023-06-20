package com.paytm.pgplus.theia.supercashoffer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.biz.workflow.model.HeadResponse;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperCashOfferResponse implements Serializable {

    private static final long serialVersionUID = 6939579615272226540L;

    private SuperCashOfferResponseBody body;
    private Boolean status;
    private String error;

}
