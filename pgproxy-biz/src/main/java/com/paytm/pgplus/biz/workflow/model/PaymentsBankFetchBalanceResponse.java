package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

/**
 * @author kartik
 * @date 18-Sep-2017
 */
public class PaymentsBankFetchBalanceResponse implements Serializable {

    private static final long serialVersionUID = -635630585689117973L;

    private String userId;
    private String accountNumber;
    private String effectiveBalance;
    private String slfdBalance;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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
        builder.append("PaymentsBankFetchBalanceResponse [userId=");
        builder.append(userId);
        builder.append(", accountNumber=");
        builder.append(accountNumber);
        builder.append(", effectiveBalance=");
        builder.append(effectiveBalance);
        builder.append(", slfdBalance=");
        builder.append(slfdBalance);
        builder.append("]");
        return builder.toString();
    }

}
