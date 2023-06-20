package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchAllPaymentOffersRequestBody implements Serializable {

    private static final long serialVersionUID = 7792583230757451841L;
    @NotBlank
    private String mid;

    private String orderId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
