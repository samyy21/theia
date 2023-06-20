package com.paytm.pgplus.theia.paymentoffer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchUserIdResponseBody implements Serializable {

    private static final long serialVersionUID = -6626633646868917739L;

    private String responseMsg;
    private String encUserId;
}
