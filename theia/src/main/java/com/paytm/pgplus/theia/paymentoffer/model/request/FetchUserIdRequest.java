package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUserIdRequest implements Serializable {

    private static final long serialVersionUID = 1413010987892956856L;

    @NotNull
    @Valid
    private TokenRequestHeader head;

    @NotNull
    @Valid
    private FetchUserIdRequestBody body;
}
