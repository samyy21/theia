package com.paytm.pgplus.theia.emiSubvention.model.request.validate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.paytm.pgplus.facade.emisubvention.models.Item;
import com.paytm.pgplus.facade.emisubvention.models.OfferDetail;
import com.paytm.pgplus.theia.emiSubvention.model.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateEmiRequestBody implements Serializable {

    private static final long serialVersionUID = -829097400399137918L;
    private String planId;
    private String mid;
    private String customerId;
    private String orderId;
    private List<Item> items;
    private boolean generateTokenForIntent;
    private String cacheCardToken;
    private PaymentDetails paymentDetails;
    private Double price;
    private Double subventionAmount;
    private OfferDetail offerDetails;
    private String referenceId;

}
