package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.paytm.pgplus.theia.sessiondata.MerchantInfo;

import java.io.Serializable;

public class NativeUpiData implements Serializable {

    private static final long serialVersionUID = 4669567221106539875L;

    private UPIPollResponse upiPollResponse;
    private MerchantInfo merchantInfo;

    public UPIPollResponse getUpiPollResponse() {
        return upiPollResponse;
    }

    public void setUpiPollResponse(UPIPollResponse upiPollResponse) {
        this.upiPollResponse = upiPollResponse;
    }

    public MerchantInfo getMerchantInfo() {
        return merchantInfo;
    }

    public void setMerchantInfo(MerchantInfo merchantInfo) {
        this.merchantInfo = merchantInfo;
    }
}
