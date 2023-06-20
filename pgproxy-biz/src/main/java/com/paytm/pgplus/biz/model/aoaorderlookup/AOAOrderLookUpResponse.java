package com.paytm.pgplus.biz.model.aoaorderlookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AOAOrderLookUpResponse extends BaseResponseBody implements Serializable {

    @JsonProperty("orderExists")
    private boolean orderExists;

    @JsonProperty("orderInfo")
    private AOAOrderInfo orderInfo;

    @Override
    public String toString() {
        return "AOAOrderLookUpResponse{" + "orderExists=" + orderExists + ", orderInfo=" + orderInfo + '}';
    }
}
