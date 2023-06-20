package com.paytm.pgplus.theia.models.splitSettlement;

import com.paytm.pgplus.facade.acquiring.models.response.AcquiringResponseBody;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class SplitSettlementResponseBody extends AcquiringResponseBody {

    private static final long serialVersionUID = -8226882371066823350L;
    private String mid;
    private String acqId;
    private String orderId;
}
