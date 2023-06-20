package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;

public class NativeBinCardHashAPIServiceRes {

    private NativeBinDetailResponse binDetailResponse;
    private String cardHash;
    private String eightDiginBin;

    public NativeBinDetailResponse getBinDetailResponse() {
        return binDetailResponse;
    }

    public void setBinDetailResponse(NativeBinDetailResponse binDetailResponse) {
        this.binDetailResponse = binDetailResponse;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getEightDiginBin() {
        return eightDiginBin;
    }

    public void setEightDiginBin(String eightDiginBin) {
        this.eightDiginBin = eightDiginBin;
    }
}
