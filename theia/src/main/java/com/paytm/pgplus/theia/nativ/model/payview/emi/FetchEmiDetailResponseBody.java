package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.EmiChannel;

public class FetchEmiDetailResponseBody extends BaseResponseBody {

    @JsonProperty("emiDetail")
    private EmiChannel emiChannel;

    private final static long serialVersionUID = -7136451177876077749L;

    public EmiChannel getEmiChannel() {
        return emiChannel;
    }

    public void setEmiChannel(EmiChannel emiChannel) {
        this.emiChannel = emiChannel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FetchEmiDetailResponseBody{");
        sb.append("emiChannel=").append(emiChannel);
        sb.append('}');
        return sb.toString();
    }
}
