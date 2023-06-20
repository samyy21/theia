package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.BaseResponseBody;
import com.paytm.pgplus.models.PaymentOffer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyPromoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 5348429511016532023L;
    private PaymentOffer paymentOffer;

    public PaymentOffer getPaymentOffer() {
        return paymentOffer;
    }

    public void setPaymentOffer(PaymentOffer paymentOffer) {
        this.paymentOffer = paymentOffer;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }

}
