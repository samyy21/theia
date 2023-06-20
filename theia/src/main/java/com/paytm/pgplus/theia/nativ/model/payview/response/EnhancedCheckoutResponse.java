package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.cache.model.MerchantStaticConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;

@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class EnhancedCheckoutResponse implements Serializable {

    private final static long serialVersionUID = 7984550283911348478L;
    @JsonProperty
    private NativeCashierInfoResponseBody fpoResponse;
    @JsonProperty
    private String txnToken;
    @JsonProperty
    private String mid;
    @JsonProperty
    private String orderId;
    @JsonProperty
    private String txnAmount;
    @JsonProperty
    private MerchantStaticConfig merchantStaticConfig;
    @JsonProperty
    private String callbackUrl;
}
