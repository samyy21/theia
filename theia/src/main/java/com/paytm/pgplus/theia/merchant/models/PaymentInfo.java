package com.paytm.pgplus.theia.merchant.models;

import com.paytm.pgplus.cashier.enums.PaymentType;

/**
 * @author Lalit Mehra
 * @since April 7, 2016
 *
 */
public class PaymentInfo {

    private Long walletBalance;
    private Long serviceAmount;
    private Long chargeFeeAmount;
    private boolean topupAndPay;

    private PaymentType paymentType;

    public Long getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Long walletBalance) {
        this.walletBalance = walletBalance;
    }

    public Long getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(Long serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public Boolean getTopupAndPay() {
        return topupAndPay;
    }

    public void setTopupAndPay(Boolean topupAndPay) {
        this.topupAndPay = topupAndPay;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * @return the chargeAmount
     */
    public Long getChargeFeeAmount() {
        return chargeFeeAmount;
    }

    /**
     * @param chargeAmount
     *            the chargeAmount to set
     */
    public void setChargeFeeAmount(Long chargeFeeAmount) {
        this.chargeFeeAmount = chargeFeeAmount;
    }

    /**
     * @param topupAndPay
     *            the topupAndPay to set
     */
    public void setTopupAndPay(boolean topupAndPay) {
        this.topupAndPay = topupAndPay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ walletBalance=").append(walletBalance).append(", serviceAmount=").append(serviceAmount)
                .append(", chargeFeeAmount=").append(chargeFeeAmount).append(", topupAndPay").append(topupAndPay)
                .append(", paymentType").append(paymentType).append("]");

        return sb.toString();
    }
}