/**
 * 
 */
package com.paytm.pgplus.theia.merchant.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author namanjain
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankInfoData {

    private Long bankId;
    private String bankName;
    private String bankCode;
    private String bankDisplayName;
    private String bankKey;
    private String alipayBankCode;
    private String bankWapLogo;
    private String bankWebLogo;
    private int bankSequence;

    public int getBankSequence() {
        return bankSequence;
    }

    public void setBankSequence(int bankSequence) {
        this.bankSequence = bankSequence;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankDisplayName() {
        return bankDisplayName;
    }

    public void setBankDisplayName(String bankDisplayName) {
        this.bankDisplayName = bankDisplayName;
    }

    public String getBankKey() {
        return bankKey;
    }

    public void setBankKey(String bankKey) {
        this.bankKey = bankKey;
    }

    public String getAlipayBankCode() {
        return alipayBankCode;
    }

    public void setAlipayBankCode(String alipayBankCode) {
        this.alipayBankCode = alipayBankCode;
    }

    public String getBankWapLogo() {
        return bankWapLogo;
    }

    public void setBankWapLogo(String bankWapLogo) {
        this.bankWapLogo = bankWapLogo;
    }

    public String getBankWebLogo() {
        return bankWebLogo;
    }

    public void setBankWebLogo(String bankWebLogo) {
        this.bankWebLogo = bankWebLogo;
    }

    public String getIssuingBankName() {
        return issuingBankName;
    }

    public void setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
    }

    private String issuingBankName;

}
