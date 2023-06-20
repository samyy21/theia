package com.paytm.pgplus.biz.core.model.MerchantCenterService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pg2RestResponse implements Serializable {
    private static final long serialVersionUID = -70127809970068569L;
    private PG2ResultInfo resultInfo;
    private Object body;
}
