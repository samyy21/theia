package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpiTransactionStatusRequestBody implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 121312312312321L;

    private String mid;

    private String cashierRequestId;

    private String transId;

    private String paymentMode;

    private String txnToken;

    private boolean asyncTxnStatusFlow;
}
