package com.paytm.pgplus.theia.offline.model.request;

public class EmiDetails {

    private String amount;

    public EmiDetails(String amount) {
        this.amount = amount;
    }

    public EmiDetails() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EmiDetails [amount=");
        builder.append(amount);
        builder.append("]");
        return builder.toString();
    }
}
