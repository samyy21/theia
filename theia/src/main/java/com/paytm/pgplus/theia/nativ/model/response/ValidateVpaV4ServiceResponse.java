package com.paytm.pgplus.theia.nativ.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateVpaV4ServiceResponse extends BaseResponseBody {

    private String vpa;
    private boolean isValid;
    private FeatureDetails featureDetails;

}
