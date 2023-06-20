/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.models.TwoFARespData;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class BalanceChannelInfoBiz implements Serializable {

    private static final long serialVersionUID = 6L;

    private String payerAccountNo;

    private String accountBalance;

    private TwoFARespData twoFAConfig;

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(final String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(final String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public TwoFARespData getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFARespData twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    @Override
    public String toString() {
        return "BalanceChannelInfoBiz [payerAccountNo=" + payerAccountNo + ", accountBalance=" + accountBalance + "]";
    }

}
