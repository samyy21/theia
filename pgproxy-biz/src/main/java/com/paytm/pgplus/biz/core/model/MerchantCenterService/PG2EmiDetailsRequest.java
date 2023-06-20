package com.paytm.pgplus.biz.core.model.MerchantCenterService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PG2EmiDetailsRequest implements Serializable {
    private static final long serialVersionUID = 1517468792996122580L;
    private String merchantId;
}
