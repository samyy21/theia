package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PPI extends BalanceChannel {

    private static final long serialVersionUID = -1841771049100481693L;

    public PPI(AccountInfo balanceInfo) {
        super(balanceInfo);
    }

    public PPI() {
    }

}
