package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpiTransactionStatusRequest implements Serializable {

    private static final long serialVersionUID = 11231232131L;

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private UpiTransactionStatusRequestBody body;

}
