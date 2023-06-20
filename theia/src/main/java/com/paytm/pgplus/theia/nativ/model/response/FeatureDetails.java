package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureDetails implements Serializable {

    private static final long serialVersionUID = 4557847913731660711L;
    private Boolean pspSupported;
    private Boolean bankSupported;
}
