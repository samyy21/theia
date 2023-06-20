package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateVpaV4ResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -5701850692855095651L;

    @JsonProperty("vpa")
    private String vpa;

    @JsonProperty("valid")
    private boolean isValid;

    @JsonProperty("featureDetails")
    private FeatureDetails featureDetails;

}
