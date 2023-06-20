package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantData {

    private String paytmId;
    private String paytmWalletId;
    private String alipayId;
    private String alipayWalletId;

    @JsonProperty(value = "paytmId")
    public String getPaytmId() {
        return paytmId;
    }

    @JsonProperty(value = "paytmWalletId")
    public String getPaytmWalletId() {
        return paytmWalletId;
    }

    @JsonProperty(value = "alipayId")
    public String getAlipayId() {
        return alipayId;
    }

    @JsonProperty(value = "alipayWalletId")
    public String getAlipayWalletId() {
        return alipayWalletId;
    }

}
