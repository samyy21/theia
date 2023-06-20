package com.paytm.pgplus.biz.core.model.wallet;

import java.io.Serializable;

public class WalletUserDetails implements Serializable {

    private static final long serialVersionUID = 22L;

    private String userBalance;

    public String getUserBalance() {
        return userBalance;
    }

    public void setUserBalance(String userBalance) {
        this.userBalance = userBalance;
    }

}
