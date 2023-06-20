package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.paytm.pgplus.response.BaseResponseBody;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayMethod;

public class FetchEmiPayChannelResponseBody extends BaseResponseBody {

    @JsonProperty("emiPayOption")
    private PayMethod payMethod;

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    private final static long serialVersionUID = -7136451177876077749L;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("payMethod", payMethod).toString();
    }

}
