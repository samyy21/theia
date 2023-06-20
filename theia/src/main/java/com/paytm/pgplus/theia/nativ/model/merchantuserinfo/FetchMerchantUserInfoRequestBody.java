package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FetchMerchantUserInfoRequestBody implements Serializable {

    private static final long serialVersionUID = -1497896935971183384L;

    @JsonProperty("mid")
    private String mid;
    @JsonProperty("orderId")
    private String orderId;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("mid", mid).append("orderId", orderId).toString();
    }

}
