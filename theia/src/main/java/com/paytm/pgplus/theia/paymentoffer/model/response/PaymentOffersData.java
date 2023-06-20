package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.facade.paymentpromotion.models.response.SearchOffer;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentOffersData implements Serializable {

    private static final long serialVersionUID = 4274960836912091604L;
    private String promocode;
    private SearchOffer offer;
    private String termsUrl;
    private String termsTitle;
    private String validFrom;
    private String validUpto;
    private String isPromoVisible;

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }

    public String getTermsUrl() {
        return termsUrl;
    }

    public void setTermsUrl(String termsUrl) {
        this.termsUrl = termsUrl;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUpto() {
        return validUpto;
    }

    public void setValidUpto(String validUpto) {
        this.validUpto = validUpto;
    }

    public String getIsPromoVisible() {
        return isPromoVisible;
    }

    public void setIsPromoVisible(String isPromoVisible) {
        this.isPromoVisible = isPromoVisible;
    }

    public SearchOffer getOffer() {
        return offer;
    }

    public void setOffer(SearchOffer offer) {
        this.offer = offer;
    }

    public String getTermsTitle() {
        return termsTitle;
    }

    public void setTermsTitle(String termsTitle) {
        this.termsTitle = termsTitle;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
