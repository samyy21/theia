package com.paytm.pgplus.theia.nativ.model.preauth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.models.Money;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PreAuthDetails implements Serializable {

    private static final long serialVersionUID = 4897536807153005694L;
    /**
     * This is intentionally made non-primitive. So as the object remains null
     * in false cases.
     */
    private Boolean preAuth;
    private Long maxBlockSeconds;
    private EPreAuthType preAuthType;
    private Money maxBlockAmount;
    private Integer expressCapturePercentage;
    private Boolean isDisabled;

    public PreAuthDetails() {
    }

    public PreAuthDetails(Boolean preAuth) {
        this.preAuth = preAuth;
    }
}
