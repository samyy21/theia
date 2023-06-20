package com.paytm.pgplus.theia.models.splitSettlement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.facade.acquiring.models.request.AcquiringRequestBody;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SplitSettlementRequestBody extends AcquiringRequestBody {

    private static final long serialVersionUID = -6615434706333571031L;
    private String merchantId;
    private String orderId;
    private String acqId;
    private String requestId;
    private String clientId;
    private List<SplitCommandInfo> splitCommandInfoList;

}
