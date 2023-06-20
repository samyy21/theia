package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

public class BankOffer implements Serializable {

    private static final long serialVersionUID = -8124744239679678507L;

    private String currency;
    private String amount;
    private String type;
    private String message;
    private String payInFullAmount;
    private String payInFullPercentage;
    private String emiPercentage;
    private String tenure;
    private String productOffer;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayInFullAmount() {
        return payInFullAmount;
    }

    public void setPayInFullAmount(String payInFullAmount) {
        this.payInFullAmount = payInFullAmount;
    }

    public String getPayInFullPercentage() {
        return payInFullPercentage;
    }

    public void setPayInFullPercentage(String payInFullPercentage) {
        this.payInFullPercentage = payInFullPercentage;
    }

    public String getEmiPercentage() {
        return emiPercentage;
    }

    public void setEmiPercentage(String emiPercentage) {
        this.emiPercentage = emiPercentage;
    }

    public String getTenure() {
        return tenure;
    }

    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public String getProductOffer() {
        return productOffer;
    }

    public void setProductOffer(String productOffer) {
        this.productOffer = productOffer;
    }

    @Override
    public String toString() {
        return "BankOffer{" + "currency='" + currency + '\'' + ", amount='" + amount + '\'' + ", type='" + type + '\''
                + ", message='" + message + '\'' + ", payInFullAmount='" + payInFullAmount + '\''
                + ", payInFullPercentage='" + payInFullPercentage + '\'' + ", emiPercentage='" + emiPercentage + '\''
                + ", tenure='" + tenure + '\'' + ", productOffer='" + productOffer + '\'' + '}';
    }
}
