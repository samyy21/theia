/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;
import java.util.Map;

/**
 * @author amit.dubey
 *
 */
public final class PayOption implements Serializable {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = 1L;

    private String payMethodName;
    private String payMethodOldName;
    private String payAmountCurrencyType;
    private String payAmountValue;
    private Map<String, String> extendInfo;
    private String transAmountCurrencyType;
    private String transAmountValue;
    private String chargeAmountValue;
    private String prepaidCard;

    /**
     * @param payMethodVal
     * @param payAmountCurrencyType
     * @param payAmountValue
     * @param extendInfo
     */
    public PayOption(String payMethodName, String payMethodOldName, String payAmountCurrencyType,
            String payAmountValue, Map<String, String> extendInfo, String transAmountCurrencyType,
            String transAmountValue, String chargeAmountValue) {
        this.payMethodName = payMethodName;
        this.payMethodOldName = payMethodOldName;
        this.payAmountCurrencyType = payAmountCurrencyType;
        this.payAmountValue = payAmountValue;
        this.extendInfo = extendInfo;
        this.transAmountCurrencyType = transAmountCurrencyType;
        this.transAmountValue = transAmountValue;
        this.chargeAmountValue = chargeAmountValue;
    }

    /**
     * @return the payMethodName
     */
    public String getPayMethodName() {
        return payMethodName;
    }

    /**
     * @param payMethodName
     *            the payMethodName to set
     */
    public void setPayMethodName(String payMethodName) {
        this.payMethodName = payMethodName;
    }

    /**
     * @return the payMethodOldName
     */
    public String getPayMethodOldName() {
        return payMethodOldName;
    }

    /**
     * @param payMethodOldName
     *            the payMethodOldName to set
     */
    public void setPayMethodOldName(String payMethodOldName) {
        this.payMethodOldName = payMethodOldName;
    }

    /**
     * @return the payAmountCurrencyType
     */
    public String getPayAmountCurrencyType() {
        return payAmountCurrencyType;
    }

    /**
     * @param payAmountCurrencyType
     *            the payAmountCurrencyType to set
     */
    public void setPayAmountCurrencyType(String payAmountCurrencyType) {
        this.payAmountCurrencyType = payAmountCurrencyType;
    }

    /**
     * @return the payAmountValue
     */
    public String getPayAmountValue() {
        return payAmountValue;
    }

    /**
     * @param payAmountValue
     *            the payAmountValue to set
     */
    public void setPayAmountValue(String payAmountValue) {
        this.payAmountValue = payAmountValue;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * @param extendInfo
     *            the extendInfo to set
     */
    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PayOption [payMethodName=").append(payMethodName).append(", payMethodOldName=")
                .append(payMethodOldName).append(", payAmountCurrencyType=").append(payAmountCurrencyType)
                .append(", payAmountValue=").append(payAmountValue).append(", extendInfo=").append(extendInfo)
                .append(", transAmountCurrencyType=").append(transAmountCurrencyType).append(", transAmountValue=")
                .append(transAmountValue).append("]");
        return builder.toString();
    }

    /**
     * @return the transAmountCurrencyType
     */
    public String getTransAmountCurrencyType() {
        return transAmountCurrencyType;
    }

    /**
     * @param transAmountCurrencyType
     *            the transAmountCurrencyType to set
     */
    public void setTransAmountCurrencyType(String transAmountCurrencyType) {
        this.transAmountCurrencyType = transAmountCurrencyType;
    }

    /**
     * @return the transAmountValue
     */
    public String getTransAmountValue() {
        return transAmountValue;
    }

    /**
     * @param transAmountValue
     *            the transAmountValue to set
     */
    public void setTransAmountValue(String transAmountValue) {
        this.transAmountValue = transAmountValue;
    }

    public String getChargeAmountValue() {
        return chargeAmountValue;
    }

    public void setChargeAmountValue(String chargeAmountValue) {
        this.chargeAmountValue = chargeAmountValue;
    }

    public String getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(String prepaidCard) {
        this.prepaidCard = prepaidCard;
    }
}