package com.paytm.pgplus.theia.nativ.model.cardtoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchCardTokenDetailRequest extends BaseRequest {

    @Valid
    @NotBlank
    @JsonProperty("head")
    private TokenRequestHeader head;

    @NotBlank
    private FetchCardTokenDetailRequestBody body;

    @Override
    public String toString() {
        return "CardTokenDetailRequest{" + "head=" + head + ", body=" + body + '}';
    }
}
