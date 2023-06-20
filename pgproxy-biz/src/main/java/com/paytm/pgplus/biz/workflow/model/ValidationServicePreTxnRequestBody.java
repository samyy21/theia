package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.facade.acquiring.models.ValidationModelInfo;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationServicePreTxnRequestBody implements Serializable {

    private static final long serialVersionUID = -6186983107277024896L;
    private String orderId;
    private String mid;
    private String validationMode;
    private ValidationModelInfo validationInfo;
    private String paymentStatus;
}
