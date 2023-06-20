package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantStaticConfigResponse implements Serializable {
    private static final long serialVersionUID = 2663977588008881888L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private MerchantStaticConfigResponseBody body;
}
