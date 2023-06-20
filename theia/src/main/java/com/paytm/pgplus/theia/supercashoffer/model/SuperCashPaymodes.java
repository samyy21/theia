package com.paytm.pgplus.theia.supercashoffer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.supercashoffers.enums.PIIPayModes;
import com.paytm.pgplus.facade.supercashoffers.models.SuperCashPayModeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperCashPaymodes implements Serializable {
    private static final long serialVersionUID = 8151123084872389358L;

    private PIIPayModes paymode;
    private List<SuperCashPayModeResponse> offers;
}
