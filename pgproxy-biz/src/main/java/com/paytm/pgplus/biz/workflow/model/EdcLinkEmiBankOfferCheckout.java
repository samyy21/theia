package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EdcLinkEmiBankOfferCheckout implements Serializable {

    private static final long serialVersionUID = -3516011229760991113L;
    private String status;
    private String error;
    private EdcLinkEmiBankOfferCheckoutData data;

}
