package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUserPaytmOfferServiceRequest {

    public FetchUserPaytmOfferServiceRequest() {
    }

    public FetchUserPaytmOfferServiceRequest(String simplifiedPromoCode) {
        this.simplifiedPromoCode = simplifiedPromoCode;
    }

    private String simplifiedPromoCode;

    public String getSimplifiedPromoCode() {
        return simplifiedPromoCode;
    }

    public void setSimplifiedPromoCode(String simplifiedPromoCode) {
        this.simplifiedPromoCode = simplifiedPromoCode;
    }

}
