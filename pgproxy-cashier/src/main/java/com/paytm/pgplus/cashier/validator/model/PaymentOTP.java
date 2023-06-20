/**
 * 
 */
package com.paytm.pgplus.cashier.validator.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author amit.dubey
 *
 */
public class PaymentOTP implements Serializable {

    /**
     * Serial version uid
     */
    private static final long serialVersionUID = -197772906764755646L;

    /** The txn mode. */
    private Long otp;
    private String txnMode;
    private String theme;
    private String mid;
    private Double walletBalance;
    private BigDecimal txnAmt;
    private String code;
    private String token;

    /**
     * @return the otp
     */
    public Long getOtp() {
        return otp;
    }

    /**
     * @param otp
     *            the otp to set
     */
    public void setOtp(Long otp) {
        this.otp = otp;
    }

    /**
     * @return the txnMode
     */
    public String getTxnMode() {
        return txnMode;
    }

    /**
     * @param txnMode
     *            the txnMode to set
     */
    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme
     *            the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the mid
     */
    public String getMid() {
        return mid;
    }

    /**
     * @param mid
     *            the mid to set
     */
    public void setMid(String mid) {
        this.mid = mid;
    }

    /**
     * @return the walletBalance
     */
    public Double getWalletBalance() {
        return walletBalance;
    }

    /**
     * @param walletBalance
     *            the walletBalance to set
     */
    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    /**
     * @return the txnAmt
     */
    public BigDecimal getTxnAmt() {
        return txnAmt;
    }

    /**
     * @param txnAmt
     *            the txnAmt to set
     */
    public void setTxnAmt(BigDecimal txnAmt) {
        this.txnAmt = txnAmt;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token
     *            the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

}