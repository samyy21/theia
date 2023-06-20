package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStaticConfigRequestBody implements Serializable {
    private static final long serialVersionUID = -6677463106258760589L;

    @JsonProperty("mid")
    private String mid;
}
