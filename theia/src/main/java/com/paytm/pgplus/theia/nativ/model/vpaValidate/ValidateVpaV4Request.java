package com.paytm.pgplus.theia.nativ.model.vpaValidate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "VpaValidateV4Request", description = "Request for validating vpa v4")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVpaV4Request implements Serializable {

    private static final long serialVersionUID = -8032687710814805135L;

    @JsonProperty("head")
    @Valid
    @NotNull(message = "head can't be null")
    private TokenRequestHeader head;

    @JsonProperty("body")
    @Valid
    @NotNull(message = "body can't be null")
    private ValidateVpaV4RequestBody body;

}
