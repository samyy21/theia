package com.paytm.pgplus.theia.nativ.model.cardindexnumber;

import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;

import java.io.Serializable;

public class NativeCardIndexNumberServReq implements Serializable {

    private final static long serialVersionUID = -7574155339279820433L;

    private InitiateTransactionRequestBody orderDetail;
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String effectivePaytmSsoToken;
    private boolean isSavedCardId;

    public InitiateTransactionRequestBody getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(InitiateTransactionRequestBody orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getEffectivePaytmSsoToken() {
        return effectivePaytmSsoToken;
    }

    public void setEffectivePaytmSsoToken(String effectivePaytmSsoToken) {
        this.effectivePaytmSsoToken = effectivePaytmSsoToken;
    }

    public boolean isSavedCardId() {
        return isSavedCardId;
    }

    public void setSavedCardId(boolean savedCardId) {
        isSavedCardId = savedCardId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
