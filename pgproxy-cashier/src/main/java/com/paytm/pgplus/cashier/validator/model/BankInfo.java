/*
 * This code contains copyright information which is the proprietary property
 * 
 * of Tarang Software Technologies Pvt Ltd . No part of this code may be reproduced, stored or
 * transmitted in any form without the prior written permission.
 * 
 * Copyright (C) Tarang Software Technologies Pvt Ltd 2012. All rights reserved.
 * ------------------------------------------------------------------------------ Author : Sadanandh
 * Description: This class is used to hold the bank related information
 * ------------------------------------------------------------------------------ Change History
 * ------------------------------------------------------------------------------
 * 
 * ------------------------------------------------------------------------------
 */
package com.paytm.pgplus.cashier.validator.model;

import java.io.Serializable;

/**
 * The Class BankInfo. This class is used to hold the bank related information
 */
public class BankInfo implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -1848136009897079153L;
    private Long bankId;
    private String bankName;
    private String displayName;
    private long isAtm;
    private String bankWebLogo;
    private String bankWapLogo;
    private String issuingBankName;
    private double bankFee;
    private String errorMessage;
    private Long errorCode;
    private boolean maintainence;
    private boolean lowPercentage;
    private long createTime;
    private int sequence;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the issuing bank name.
     *
     * @return the issuing bank name
     */
    public String getIssuingBankName() {
        return issuingBankName;
    }

    /**
     * Sets the issuing bank name.
     *
     * @param issuingBankName
     *            the new issuing bank name
     */
    public void setIssuingBankName(String issuingBankName) {
        this.issuingBankName = issuingBankName;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage
     *            the new error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public Long getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code.
     * 
     * @param errorCode
     *            the new error code
     */
    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets the bank id.
     * 
     * @return the bank id
     */
    public Long getBankId() {
        return bankId;
    }

    /**
     * Sets the bank id.
     * 
     * @param bankId
     *            the new bank id
     */
    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    /**
     * Gets the bank name.
     * 
     * @return the bank name
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * Sets the bank name.
     * 
     * @param bankName
     *            the new bank name
     */
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    /**
     * @return the bankWebLogo
     */
    public String getBankWebLogo() {
        return bankWebLogo;
    }

    /**
     * @param bankWebLogo
     *            the bankWebLogo to set
     */
    public void setBankWebLogo(String bankWebLogo) {
        this.bankWebLogo = bankWebLogo;
    }

    /**
     * @return the bankWapLogo
     */
    public String getBankWapLogo() {
        return bankWapLogo;
    }

    /**
     * @param bankWapLogo
     *            the bankWapLogo to set
     */
    public void setBankWapLogo(String bankWapLogo) {
        this.bankWapLogo = bankWapLogo;
    }

    public long getIsAtm() {
        return isAtm;
    }

    public void setIsAtm(long isAtm) {
        this.isAtm = isAtm;
    }

    /**
     * Gets the bank fee.
     * 
     * @return the bank fee
     */
    public double getBankFee() {
        return bankFee;
    }

    /**
     * Sets the bank fee.
     * 
     * @param bankFee
     *            the new bank fee
     */
    public void setBankFee(double bankFee) {
        this.bankFee = bankFee;
    }

    public boolean isMaintainence() {
        return maintainence;
    }

    public void setMaintainence(boolean maintainence) {
        this.maintainence = maintainence;
    }

    public boolean isLowPercentage() {
        return lowPercentage;
    }

    public void setLowPercentage(boolean lowPercentage) {
        this.lowPercentage = lowPercentage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bankName == null) ? 0 : bankName.hashCode());
        result = prime * result + (int) (isAtm ^ (isAtm >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BankInfo other = (BankInfo) obj;
        if (bankName == null) {
            if (other.bankName != null)
                return false;
        } else if (!bankName.equals(other.bankName))
            return false;
        if (isAtm != other.isAtm)
            return false;
        return true;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "BankInfo [bankId=" + bankId + ", bankName=" + bankName + ", bankWebLogo=" + bankWebLogo
                + ", bankWapLogo=" + bankWapLogo + ", isAtm=" + isAtm + ", errorMessage=" + errorMessage
                + ", errorCode=" + errorCode + ", issuingBankName=" + issuingBankName + ", bankFee=" + bankFee + "]";
    }

}
