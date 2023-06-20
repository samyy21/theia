package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.request.SecureRequestHeader;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationServicePreTxnRequest implements Serializable {

    private static final long serialVersionUID = -6186983107277024896L;

    private SecureRequestHeader head;
    private ValidationServicePreTxnRequestBody body;
}
