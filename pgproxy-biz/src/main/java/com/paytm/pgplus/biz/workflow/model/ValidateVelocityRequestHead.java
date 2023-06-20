package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.Mask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateVelocityRequestHead implements Serializable {

    private static final long serialVersionUID = 7937944597958736030L;

    private String version;
    private String clientId;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String clientSecret;
}
