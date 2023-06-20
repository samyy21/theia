package com.paytm.pgplus.theia.nativ.model.vpaValidate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.enums.ERequestType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVpaV4RequestBody {

    @ApiModelProperty(notes = "VPA is mandatory.")
    @JsonProperty("vpa")
    private String vpa;

    @ApiModelProperty(notes = "Mid is mandatory.")
    @JsonProperty("mid")
    private String mid;

    @ApiModelProperty(notes = "request type can't be null")
    @JsonProperty("requestType")
    private ERequestType requestType;

    // this is set after request is received at controller
    private String queryParams;
}