package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.ResultInfo;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationServicePreTxnResponseBody implements Serializable {

    private static final long serialVersionUID = -2959715371325908755L;
    private ResultInfo resultInfo;
    private String mid;
    private String orderId;
}
