package com.paytm.pgplus.theia.nativ.model.PCF;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;
import java.math.BigDecimal;

public class PCFFeeCharges implements Serializable {

    private static final long serialVersionUID = 7504890990812306965L;
    private Money feeAmount;
    private Money taxAmount;
    private Money totalTransactionAmount;

    public PCFFeeCharges() {
    }

    public PCFFeeCharges(Money feeAmount, Money taxAmount, Money totalTransactionAmount) {
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.totalTransactionAmount = totalTransactionAmount;
    }

    public Money getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Money feeAmount) {
        this.feeAmount = feeAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Money getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(Money totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

    @Override
    public String toString() {
        return "PCFFeeCharges{" + "feeAmount=" + feeAmount + ", taxAmount=" + taxAmount + ", totalTransactionAmount="
                + totalTransactionAmount + '}';
    }

}
