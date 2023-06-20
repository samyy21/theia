package com.paytm.pgplus.theia.models;

import java.io.Serializable;

/**
 * @author kartik
 * @date 09-03-2017
 */
public class SuccessRateBean implements Serializable {

    private static final long serialVersionUID = -4438763723497779908L;

    private String bankAbbr;
    private String bizPattern;
    private String rate;
    private String cardType;

    public SuccessRateBean(String bankAbbr, String bizPattern, String rate, String cardType) {
        this.bankAbbr = bankAbbr;
        this.bizPattern = bizPattern;
        this.rate = rate;
        this.cardType = cardType;
    }

    public String getBankAbbr() {
        return bankAbbr;
    }

    public void setBankAbbr(String bankAbbr) {
        this.bankAbbr = bankAbbr;
    }

    public String getBizPattern() {
        return bizPattern;
    }

    public void setBizPattern(String bizPattern) {
        this.bizPattern = bizPattern;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SuccessRateBean [bankAbbr=").append(bankAbbr).append(", bizPattern=").append(bizPattern)
                .append(", rate=").append(rate).append(", cardType=").append(cardType).append("]");
        return builder.toString();
    }

}
