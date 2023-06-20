package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoDetails implements Serializable {
    private static final long serialVersionUID = 5104978415829432972L;

    private String promocode;

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }

    @Override
    public String toString() {
        return (new MaskToStringBuilder(this)).toString();
    }
}