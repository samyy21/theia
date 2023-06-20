package com.paytm.pgplus.theia.nativ.model.postTransactionSplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.SplitSettlementInfoData;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class PostTransactionSplitRequestBody implements Serializable {

    private static final long serialVersionUID = 626108389832694123L;

    private String mid;
    private String orderId;
    private String acqId;
    @JsonProperty("splitSettlementInfo")
    private SplitSettlementInfoData splitSettlementInfoData;
}
