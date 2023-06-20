package com.paytm.pgplus.theia.models.bin;

import com.paytm.pgplus.customaspects.annotations.Mask;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

public class BinDetailsRequestBody {

    @Mask(prefixNoMaskLen = 6)
    private String bin;

    private String orderId;

    private String checksum;

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
