package com.paytm.pgplus.theia.models.bin;

import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;

public class BinDetailsRequestHead {

    private String mid;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
