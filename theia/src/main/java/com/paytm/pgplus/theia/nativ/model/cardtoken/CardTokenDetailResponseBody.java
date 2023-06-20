package com.paytm.pgplus.theia.nativ.model.cardtoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.facade.coft.model.TokenInfo;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardTokenDetailResponseBody extends BaseResponseBody {

    private TokenInfo tokenInfo;

}
