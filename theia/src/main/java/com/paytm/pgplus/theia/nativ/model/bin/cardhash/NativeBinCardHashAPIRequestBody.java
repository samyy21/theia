package com.paytm.pgplus.theia.nativ.model.bin.cardhash;

import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

import java.io.Serializable;

public class NativeBinCardHashAPIRequestBody implements Serializable {

    private final static long serialVersionUID = 8951828208931812964L;

    private String mid;

    @Mask(prefixNoMaskLen = 4, suffixNoMaskLen = 4)
    private String cardNumber;

    private String savedCardId;

    private boolean eightDigitBinRequired;

    private String paymentFlow;

    private CardTokenInfo cardTokenInfo;

    public String getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(String paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    public boolean isEightDigitBinRequired() {
        return eightDigitBinRequired;
    }

    public void setEightDigitBinRequired(boolean eightDigitBinRequired) {
        this.eightDigitBinRequired = eightDigitBinRequired;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
