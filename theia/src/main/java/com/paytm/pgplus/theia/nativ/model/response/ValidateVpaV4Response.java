package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResponseHeader;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "ValidateVpaV4Response", description = "Request for validating vpa v4")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVpaV4Response implements Serializable {

    private static final long serialVersionUID = -1063750989558481714L;

    @JsonProperty("head")
    @Valid
    @NotNull(message = "head can't be null")
    private ResponseHeader head;

    @JsonProperty("body")
    @Valid
    @NotNull(message = "body can't be null")
    private ValidateVpaV4ResponseBody body;

}
