package com.paytm.pgplus.biz.core.model.ppb;

import java.io.Serializable;

/**
 * @author kartik
 * @date 17-Sep-2017
 */
public class SavingsAccountDetailBiz implements Serializable {

    private static final long serialVersionUID = 2936304024730478602L;

    private String accountNumber;
    private String effectiveBalance;
    private String slfdBalance;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavingsAccountDetailBiz [accountNumber=");
        builder.append(accountNumber);
        builder.append(", effectiveBalance=");
        builder.append(effectiveBalance);
        builder.append(", slfdBalance=");
        builder.append(slfdBalance);
        builder.append("]");
        return builder.toString();
    }
}
