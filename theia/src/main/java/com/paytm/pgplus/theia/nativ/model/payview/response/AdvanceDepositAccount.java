package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvanceDepositAccount extends BalanceChannel {

    private static final long serialVersionUID = 1766354709074712909L;

    public AdvanceDepositAccount(AccountInfo balanceInfo) {
        super(balanceInfo);
    }

    public AdvanceDepositAccount() {
    }
}
