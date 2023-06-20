package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;

import java.io.Serializable;

public class NativeFetchCardIndexNumberAPIResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -5730304059982696854L;

    @JsonProperty(value = "cardIndexNumber")
    private String cardIndexNumber;

    public String getCardIndexNumber() {
        return cardIndexNumber;
    }

    public void setCardIndexNumber(String cardIndexNumber) {
        this.cardIndexNumber = cardIndexNumber;
    }

    @Override
    public String toString() {
        return "NativeFetchCardIndexNumberAPIResponseBody{" + "cardIndexNumber='" + cardIndexNumber + '\'' + '}';
    }
}
