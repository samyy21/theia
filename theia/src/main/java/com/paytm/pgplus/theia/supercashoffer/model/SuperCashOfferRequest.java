package com.paytm.pgplus.theia.supercashoffer.model;

import com.paytm.pgplus.theia.nativ.model.common.SsoTokenRequestHeader;
import lombok.Data;

import java.io.Serializable;

@Data
public class SuperCashOfferRequest implements Serializable {

    private static final long serialVersionUID = -276923852678627978L;

    private SuperCashOfferRequestBody body;
    private SsoTokenRequestHeader head;

}
