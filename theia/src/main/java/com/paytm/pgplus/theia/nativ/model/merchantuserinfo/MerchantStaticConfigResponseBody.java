package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.cache.model.MerchantStaticConfig;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantStaticConfigResponseBody extends BaseResponseBody {
    private static final long serialVersionUID = 4508306882411746079L;

    @JsonProperty("merchantStaticConfig")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MerchantStaticConfig merchantStaticConfig;

    public MerchantStaticConfig getMerchantStaticConfig() {
        return merchantStaticConfig;
    }

    public void setMerchantStaticConfig(MerchantStaticConfig merchantStaticConfig) {
        this.merchantStaticConfig = merchantStaticConfig;
    }

    @Override
    public String toString() {
        return "MerchantStaticConfigResponseBody{" + "merchantStaticConfig=" + merchantStaticConfig + '}';
    }

}
