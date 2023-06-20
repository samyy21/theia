package com.paytm.pgplus.theia.nativ.model.payview.response;

import java.io.Serializable;

public class NativePromoCodeData implements Serializable {

    private static final long serialVersionUID = 1071165806624704161L;

    private String promoCode;
    private String promoMsg;
    private boolean promoCodeValid;
    private String promoCodeTypeName;
    private String promoCodeMsg;

    public NativePromoCodeData() {
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoMsg() {
        return promoMsg;
    }

    public void setPromoMsg(String promoMsg) {
        this.promoMsg = promoMsg;
    }

    public boolean isPromoCodeValid() {
        return promoCodeValid;
    }

    public void setPromoCodeValid(boolean promoCodeValid) {
        this.promoCodeValid = promoCodeValid;
    }

    public String getPromoCodeTypeName() {
        return promoCodeTypeName;
    }

    public void setPromoCodeTypeName(String promoCodeTypeName) {
        this.promoCodeTypeName = promoCodeTypeName;
    }

    public String getPromoCodeMsg() {
        return promoCodeMsg;
    }

    public void setPromoCodeMsg(String promoCodeMsg) {
        this.promoCodeMsg = promoCodeMsg;
    }

    @Override
    public String toString() {
        return "NativePromoCodeData{" + "promoCode='" + promoCode + '\'' + ", promoMsg='" + promoMsg + '\''
                + ", promoCodeValid=" + promoCodeValid + ", promoCodeTypeName='" + promoCodeTypeName + '\''
                + ", promoCodeMsg='" + promoCodeMsg + '\'' + '}';
    }
}
