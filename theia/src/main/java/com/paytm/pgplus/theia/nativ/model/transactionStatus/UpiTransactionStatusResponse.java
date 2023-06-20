package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.ResponseHeader;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpiTransactionStatusResponse implements Serializable {
    private static final long serialVersionUID = 726104176053530999L;

    @NotNull
    @Valid
    private ResponseHeader head;

    @NotNull
    @Valid
    private UpiTransactionStatusResponseBody body;
}
