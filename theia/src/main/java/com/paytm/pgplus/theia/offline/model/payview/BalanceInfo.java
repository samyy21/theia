package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mysql.jdbc.StringUtils;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceInfo implements Serializable {

    private static final long serialVersionUID = 8569028972045477998L;

    @ApiModelProperty(required = true)
    @NotBlank
    private String payerAccountNo;
    @NotNull
    @Valid
    private Money accountBalance;

    private boolean isPayerAccountExists;

    public BalanceInfo(String payerAccountNo, Money accountBalance, boolean isAccountBalanceInPaise) {
        if (isAccountBalanceInPaise && accountBalance != null) {
            String amtStr = AmountUtils.getTransactionAmountInRupee(accountBalance.getValue());
            // getTransactionAmountInRupee method trim leading zero and return
            // .00 instead
            // of 0.00 can't change this method as it is part of pgproxycommon
            // and can lead
            // to issue in other modules.
            // hack to handle amount start with dot eg : .00 etc
            if (!StringUtils.isNullOrEmpty(amtStr) && amtStr.startsWith(".")) {
                amtStr = "0" + amtStr;
            }
            accountBalance.setValue(amtStr);
        }
        this.payerAccountNo = payerAccountNo;
        this.accountBalance = accountBalance;
        this.isPayerAccountExists = true;
    }

    public BalanceInfo() {
        this.isPayerAccountExists = false;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public Money getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Money accountBalance) {
        this.accountBalance = accountBalance;
    }

    public boolean isPayerAccountExists() {
        return isPayerAccountExists;
    }

    public void setPayerAccountExists(boolean payerAccountExists) {
        isPayerAccountExists = payerAccountExists;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceInfo{");
        sb.append("payerAccountNo='").append(payerAccountNo).append('\'');
        sb.append(", accountBalance=").append(accountBalance);
        sb.append('}');
        return sb.toString();
    }
}
