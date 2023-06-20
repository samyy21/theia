package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.enums.UPIPollStatus;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpiTransactionStatusResponseBody implements Serializable {

    private static final long serialVersionUID = 2342344423L;

    private ResultInfo resultInfo;

    private UPIPollStatus pollStatus;

}
