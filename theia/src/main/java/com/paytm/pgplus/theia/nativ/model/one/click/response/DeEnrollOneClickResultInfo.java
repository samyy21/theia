package com.paytm.pgplus.theia.nativ.model.one.click.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.enums.EPayMode;
import com.paytm.pgplus.theia.nativ.model.payview.response.PayOption;

public class DeEnrollOneClickResultInfo {

    @JsonProperty("paymentFlow")
    private EPayMode paymentFlow;

    @JsonProperty("merchantPayOption")
    private PayOption merchantPayOption;
}
