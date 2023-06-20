package com.paytm.pgplus.theia.emiSubvention.model.response.tenures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.paytm.pgplus.facade.emisubvention.models.PlanDetail;
import com.paytm.pgplus.response.BaseResponseBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiTenuresResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3465773092852021079L;
    private String bankName;
    private String bankCode;
    private String cardType;
    private String bankLogoUrl;
    private List<PlanDetail> planDetails;

}
