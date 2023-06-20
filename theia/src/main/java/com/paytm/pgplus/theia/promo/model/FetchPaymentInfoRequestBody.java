package com.paytm.pgplus.theia.promo.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class FetchPaymentInfoRequestBody {

    public FetchPaymentInfoRequestBody() {
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
