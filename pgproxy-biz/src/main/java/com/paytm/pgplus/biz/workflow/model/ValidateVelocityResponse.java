package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateVelocityResponse implements Serializable {
    private static final long serialVersionUID = 3770814239212913739L;

    private String resultStatus;
    private String resultCode;
    private String resultMsg;
    private String resultCodeId;
    private String brandVerificationCode;
    private List<String> velocityOfferId;

}
