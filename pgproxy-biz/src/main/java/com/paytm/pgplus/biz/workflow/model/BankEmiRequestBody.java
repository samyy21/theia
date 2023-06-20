package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

/**
 * @author harshwardhan
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankEmiRequestBody implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3993707511863260928L;

    private String amount;
    private String bin;
    private String date;
    private String encryptedPan;
    private String mid;
    private String payInFullAmount;
    private String tid;
    private String time;
    private String year;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String cardIndexNumber;
    private String extendedInfo;

    public String getExtendedInfo() {
        return extendedInfo;
    }

    public void setExtendedInfo(String extendedInfo) {
        this.extendedInfo = extendedInfo;
    }

    public String getCardIndexNumber() {
        return cardIndexNumber;
    }

    public void setCardIndexNumber(String cardIndexNumber) {
        this.cardIndexNumber = cardIndexNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEncryptedPan() {
        return encryptedPan;
    }

    public void setEncryptedPan(String encryptedPan) {
        this.encryptedPan = encryptedPan;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getPayInFullAmount() {
        return payInFullAmount;
    }

    public void setPayInFullAmount(String payInFullAmount) {
        this.payInFullAmount = payInFullAmount;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
