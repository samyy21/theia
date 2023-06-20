package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCredit extends BalanceChannel {

    private static final long serialVersionUID = -1508497003305821099L;

    public DigitalCredit(AccountInfo balanceInfo) {
        super(balanceInfo);
    }

    public DigitalCredit() {
    }

}
