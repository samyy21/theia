package com.paytm.pgplus.theia.models;

import java.io.Serializable;
import java.util.List;

public class FPODisablePaymentMode implements Serializable {
    private static final long serialVersionUID = 1281487678460942663L;
    private String paymode;
    private List<String> paymodeChannels;
    private String merchantType;
    private List<String> merchantLimitType;

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public List<String> getPaymodeChannels() {
        return paymodeChannels;
    }

    public void setPaymodeChannels(List<String> paymodeChannels) {
        this.paymodeChannels = paymodeChannels;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public List<String> getMerchantLimitType() {
        return merchantLimitType;
    }

    public void setMerchantLimitType(List<String> merchantLimitType) {
        this.merchantLimitType = merchantLimitType;
    }

    @Override
    public String toString() {
        return "FPODisablePaymentMode{" + "paymode='" + paymode + '\'' + ", paymodeChannels=" + paymodeChannels
                + ", merchantType='" + merchantType + '\'' + ", merchantLimitType=" + merchantLimitType + '}';
    }
}
