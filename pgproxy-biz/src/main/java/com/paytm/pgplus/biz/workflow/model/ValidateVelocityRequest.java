package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateVelocityRequest implements Serializable {
    private static final long serialVersionUID = -7405486786507166744L;

    private ValidateVelocityRequestHead head;
    private ValidateVelocityRequestBody body;

}
