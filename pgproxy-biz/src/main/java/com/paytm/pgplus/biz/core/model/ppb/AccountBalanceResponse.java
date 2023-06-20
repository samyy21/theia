package com.paytm.pgplus.biz.core.model.ppb;

import java.io.Serializable;
import java.util.List;

/**
 * @author kartik
 * @date 17-Sep-2017
 */
public class AccountBalanceResponse implements Serializable {

    private static final long serialVersionUID = 9033198223642856361L;

    private String accountNumber;
    private String effectiveBalance;
    private String slfdBalance;
    private String txnId;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getEffectiveBalance() {
        return effectiveBalance;
    }

    public void setEffectiveBalance(String effectiveBalance) {
        this.effectiveBalance = effectiveBalance;
    }

    public String getSlfdBalance() {
        return slfdBalance;
    }

    public void setSlfdBalance(String slfdBalance) {
        this.slfdBalance = slfdBalance;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccountBalanceResponse [accountNumber=");
        builder.append(accountNumber);
        builder.append(", effectiveBalance=");
        builder.append(effectiveBalance);
        builder.append(", slfdBalance=");
        builder.append(slfdBalance);
        builder.append(", txnId=");
        builder.append(txnId);
        builder.append("]");
        return builder.toString();
    }

}
