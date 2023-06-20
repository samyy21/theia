package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author kartik
 * @date 19-04-2017
 */
public class ExternalAccountInfoBiz implements Serializable {

    private static final long serialVersionUID = -7006090657439079808L;

    private String accountBalance;
    private String externalAccountNo;
    private String extendInfo;

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getExternalAccountNo() {
        return externalAccountNo;
    }

    public void setExternalAccountNo(String externalAccountNo) {
        this.externalAccountNo = externalAccountNo;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExternalAccountInfoBiz [accountBalance=");
        builder.append(accountBalance);
        builder.append(", externalAccountNo=");
        builder.append(externalAccountNo);
        builder.append(", extendInfo=");
        builder.append(extendInfo);
        builder.append("]");
        return builder.toString();
    }

}
