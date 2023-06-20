package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.models.PromoCartDetails;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplyItemOffers implements Serializable {
    private final static long serialVersionUID = -6262490824812945321L;
    private Map<String, String> promoContext; // passthrough data

    private PromoCartDetails cartDetails; // This is required for item offers in

    // AND Offers flow

    public Map<String, String> getPromoContext() {
        return promoContext;
    }

    public void setPromoContext(Map<String, String> promoContext) {
        this.promoContext = promoContext;
    }

    public PromoCartDetails getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(PromoCartDetails cartDetails) {
        this.cartDetails = cartDetails;
    }

    @Override
    public String toString() {
        return "ApplyItemOffers{" + "promoContext=" + promoContext + ", cartDetails=" + cartDetails + '}';
    }
}
