package com.paytm.pgplus.theia.nativ.model.postTransactionSplit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PostTransactionSplitResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 34108079567304914L;

    private String mid;
    private String orderId;
    private String acqId;

    public PostTransactionSplitResponseBody(ResultInfo resultInfo, String mid, String orderId, String acqId) {
        super(resultInfo);
        this.mid = mid;
        this.orderId = orderId;
        this.acqId = acqId;
    }

    public PostTransactionSplitResponseBody(ResultInfo resultInfo) {
        super(resultInfo);
    }

}
