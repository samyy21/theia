package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponseBody;
import com.paytm.pgplus.response.BaseResponseBody;

import java.io.Serializable;

public class NativeBinCardHashAPIResponseBody extends BaseResponseBody {

    private String cardHash;
    private NativeBinDetailResponseBody cardDetails;

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public NativeBinDetailResponseBody getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(NativeBinDetailResponseBody cardDetails) {
        this.cardDetails = cardDetails;
    }
}
