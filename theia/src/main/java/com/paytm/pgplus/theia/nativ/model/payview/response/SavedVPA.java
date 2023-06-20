package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.models.ItemLevelPaymentOffer;
import com.paytm.pgplus.models.PaymentOfferDetails;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedVPA extends UPI {

    private static final long serialVersionUID = 4495509181109368213L;
    private VPADetails vpaDetails;
    private PaymentOfferDetails paymentOfferDetails;
    private ItemLevelPaymentOffer paymentOfferDetailsV2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentOfferDetails nonHybridPaymentOfferDetails;

    public SavedVPA() {
    }

    public PaymentOfferDetails getNonHybridPaymentOfferDetails() {
        return nonHybridPaymentOfferDetails;
    }

    public void setNonHybridPaymentOfferDetails(PaymentOfferDetails nonHybridPaymentOfferDetails) {
        this.nonHybridPaymentOfferDetails = nonHybridPaymentOfferDetails;
    }

    public VPADetails getVpaDetails() {
        return this.vpaDetails;
    }

    public void setVpaDetails(VPADetails vpaDetails) {
        this.vpaDetails = vpaDetails;
    }

    public PaymentOfferDetails getPaymentOfferDetails() {
        return paymentOfferDetails;
    }

    public ItemLevelPaymentOffer getPaymentOfferDetailsV2() {
        return paymentOfferDetailsV2;
    }

    public void setPaymentOfferDetailsV2(ItemLevelPaymentOffer paymentOfferDetailsV2) {
        this.paymentOfferDetailsV2 = paymentOfferDetailsV2;
    }

    public void setPaymentOfferDetails(PaymentOfferDetails paymentOfferDetails) {
        this.paymentOfferDetails = paymentOfferDetails;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
