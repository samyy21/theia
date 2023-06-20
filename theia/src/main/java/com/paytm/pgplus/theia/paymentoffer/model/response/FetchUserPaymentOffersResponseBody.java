package com.paytm.pgplus.theia.paymentoffer.model.response;

import java.io.Serializable;

public class FetchUserPaymentOffersResponseBody extends FetchPaymentOffersResponseBody implements Serializable {

    private static final long serialVersionUID = -2101764252101397507L;

    private boolean paytmOffersAvailable;

    public boolean isPaytmOffersAvailable() {
        return paytmOffersAvailable;
    }

    public void setPaytmOffersAvailable(boolean paytmOffersAvailable) {
        this.paytmOffersAvailable = paytmOffersAvailable;
    }
}
