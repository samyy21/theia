package com.paytm.pgplus.theia.nativ.model.postTransactionSplit;

import com.paytm.pgplus.response.ResponseHeader;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class PostTransactionSplitResponse implements Serializable {

    private static final long serialVersionUID = -5082598026653891685L;

    private ResponseHeader head;
    private PostTransactionSplitResponseBody body;
}
