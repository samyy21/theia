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
public class PG2ResultInfo implements Serializable {
    private static final long serialVersionUID = -1256535332140392515L;
    private String resultCodeId;
    private String resultStatus;
    private String resultMsg;
    private String respTime;
}
