package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wallet extends BalanceChannel {

    private static final long serialVersionUID = -1841771049100481693L;

    public Wallet(BalanceInfo balanceInfo) {
        super(balanceInfo);
    }

    public Wallet() {
    }

}
