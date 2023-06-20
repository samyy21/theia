package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;
import java.util.List;

public class PSULimit implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7692218146709841678L;

    private List<String> nbCapBankList;
    private List<String> capMerchantList;
    private List<String> cardCapBankList;
    private String nbCapMaxAmount;
    private String nbCapApplicable;
    private String cardCapMaxAmount;
    private String cardCapApplicable;
    private String sbiCardEnabled;

    public List<String> getNbCapBankList() {
        return nbCapBankList;
    }

    public void setNbCapBankList(List<String> nbCapBankList) {
        this.nbCapBankList = nbCapBankList;
    }

    public List<String> getCapMerchantList() {
        return capMerchantList;
    }

    public void setCapMerchantList(List<String> capMerchantList) {
        this.capMerchantList = capMerchantList;
    }

    public List<String> getCardCapBankList() {
        return cardCapBankList;
    }

    public void setCardCapBankList(List<String> cardCapBankList) {
        this.cardCapBankList = cardCapBankList;
    }

    public String getNbCapMaxAmount() {
        return nbCapMaxAmount;
    }

    public void setNbCapMaxAmount(String nbCapMaxAmount) {
        this.nbCapMaxAmount = nbCapMaxAmount;
    }

    public String getNbCapApplicable() {
        return nbCapApplicable;
    }

    public void setNbCapApplicable(String nbCapApplicable) {
        this.nbCapApplicable = nbCapApplicable;
    }

    public String getCardCapMaxAmount() {
        return cardCapMaxAmount;
    }

    public void setCardCapMaxAmount(String cardCapMaxAmount) {
        this.cardCapMaxAmount = cardCapMaxAmount;
    }

    public String getCardCapApplicable() {
        return cardCapApplicable;
    }

    public void setCardCapApplicable(String cardCapApplicable) {
        this.cardCapApplicable = cardCapApplicable;
    }

    public String getSbiCardEnabled() {
        return sbiCardEnabled;
    }

    public void setSbiCardEnabled(String sbiCardEnabled) {
        this.sbiCardEnabled = sbiCardEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PSULimit [nbCapBankList=");
        builder.append(nbCapBankList);
        builder.append(", capMerchantList=");
        builder.append(capMerchantList);
        builder.append(", cardCapBankList=");
        builder.append(cardCapBankList);
        builder.append(", nbCapMaxAmount=");
        builder.append(nbCapMaxAmount);
        builder.append(", nbCapApplicable=");
        builder.append(nbCapApplicable);
        builder.append(", cardCapMaxAmount=");
        builder.append(cardCapMaxAmount);
        builder.append(", cardCapApplicable=");
        builder.append(cardCapApplicable);
        builder.append(", sbiCardEnabled=");
        builder.append(sbiCardEnabled);
        builder.append("]");
        return builder.toString();
    }
}