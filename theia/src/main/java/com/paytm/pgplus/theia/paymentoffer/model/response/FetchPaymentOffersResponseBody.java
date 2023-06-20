package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.nativ.BaseResponseBody;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchPaymentOffersResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 8147529211606529137L;
    private List<PaymentOffersData> paymentOffers;

    public List<PaymentOffersData> getPaymentOffers() {
        return paymentOffers;
    }

    public void setPaymentOffers(List<PaymentOffersData> paymentOffers) {
        this.paymentOffers = paymentOffers;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }
}
