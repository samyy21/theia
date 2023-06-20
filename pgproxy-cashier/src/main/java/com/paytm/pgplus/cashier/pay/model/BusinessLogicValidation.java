package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;

import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.facade.enums.TransType;

public class BusinessLogicValidation implements Serializable {

    private static final long serialVersionUID = -2993178530023549321L;

    private String cardNumber;
    private String binNumber;
    private String mid;
    private String amount;
    private ValidationRequest validationRequest;
    private BinCardRequest binCard;
    private TransType transType;
    private PaymentType paymentType;
    private String bankName;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public ValidationRequest getValidationRequest() {
        return validationRequest;
    }

    public void setValidationRequest(ValidationRequest validationRequest) {
        this.validationRequest = validationRequest;
    }

    public BinCardRequest getBinCard() {
        return binCard;
    }

    public void setBinCard(BinCardRequest binCard) {
        this.binCard = binCard;
    }

    public TransType getTransType() {
        return transType;
    }

    public void setTransType(TransType transType) {
        this.transType = transType;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

}
