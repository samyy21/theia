package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchUserIdResponse implements Serializable {

    private static final long serialVersionUID = -4297000440525284005L;

    private ResponseHeader head;

    private FetchUserIdResponseBody body;
}
