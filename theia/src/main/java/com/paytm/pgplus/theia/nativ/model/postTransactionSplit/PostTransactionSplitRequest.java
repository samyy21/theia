package com.paytm.pgplus.theia.nativ.model.postTransactionSplit;

import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@ToString
public class PostTransactionSplitRequest implements Serializable {

    private static final long serialVersionUID = -566706503152543177L;

    private TokenRequestHeader head;
    private PostTransactionSplitRequestBody body;

}
