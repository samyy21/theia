package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.request.TokenRequestHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStaticConfigRequest implements Serializable {

    private static final long serialVersionUID = 2447784980112657293L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private MerchantStaticConfigRequestBody body;
}
