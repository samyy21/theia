package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.Money;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfo implements Serializable {

    private static final long serialVersionUID = 8569028972045477998L;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonIgnore
    private String payerAccountNo;
    @NotNull
    @Valid
    private Money balance;

    @JsonIgnore
    private boolean isPayerAccountExists;

    private List<SubWalletDetails> subWalletDetails;

    public AccountInfo(String payerAccountNo, Money accountBalance) {
        this.payerAccountNo = payerAccountNo;
        this.balance = accountBalance;
        this.isPayerAccountExists = true;
    }

    public AccountInfo() {
        this.isPayerAccountExists = false;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public Money getAccountBalance() {
        return balance;
    }

    public void setAccountBalance(Money accountBalance) {
        this.balance = accountBalance;
    }

    public boolean isPayerAccountExists() {
        return isPayerAccountExists;
    }

    public void setPayerAccountExists(boolean payerAccountExists) {
        isPayerAccountExists = payerAccountExists;
    }

    public List<SubWalletDetails> getSubWalletDetails() {
        return subWalletDetails;
    }

    public void setSubWalletDetails(List<SubWalletDetails> subWalletDetails) {
        this.subWalletDetails = subWalletDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BalanceInfo{");
        sb.append("payerAccountNo='").append(payerAccountNo).append('\'');
        sb.append(", accountBalance=").append(balance).append('\'');
        sb.append(", subWalletDetails=").append(subWalletDetails);
        sb.append('}');
        return sb.toString();
    }
}
