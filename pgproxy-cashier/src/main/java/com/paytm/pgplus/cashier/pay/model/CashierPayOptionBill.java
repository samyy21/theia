/**
 *
 */
package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;
import java.util.Map;

import com.paytm.pgplus.cashier.looper.model.CashierMoney;
import com.paytm.pgplus.facade.enums.PayMethod;

/**
 * @author amit.dubey
 *
 */
public final class CashierPayOptionBill implements Serializable {

    /** serial version UID */
    private static final long serialVersionUID = -1798794729404710490L;

    private String payOption;
    private PayMethod payMethod;
    private CashierMoney transAmount;
    private CashierMoney chargeAmount;

    private Boolean topupAndPay;
    private String payerAccountNo;
    private String cardCacheToken;
    private Boolean saveChannelInfoAfterPay;
    private Map<String, String> channelInfo;
    private Map<String, String> extendInfo;
    private String issuingCountry;

    /**
     * @param payOption
     * @param payMethod
     * @param transAmount
     * @param chargeAmount
     * @param topupAndPay
     */
    public CashierPayOptionBill(String payOption, PayMethod payMethod, CashierMoney transAmount,
            CashierMoney chargeAmount) {
        this.payOption = payOption;
        this.payMethod = payMethod;
        this.transAmount = transAmount;
        this.chargeAmount = chargeAmount;
    }

    /**
     * @return the payOption
     */
    public String getPayOption() {
        return payOption;
    }

    /**
     * @return the payMethod
     */
    public PayMethod getPayMethod() {
        return payMethod;
    }

    /**
     * @return the transAmount
     */
    public CashierMoney getTransAmount() {
        return transAmount;
    }

    /**
     * @return the chargeAmount
     */
    public CashierMoney getChargeAmount() {
        return chargeAmount;
    }

    /**
     * @return the topupAndPay
     */
    public Boolean getTopupAndPay() {
        return topupAndPay;
    }

    /**
     * @param topupAndPay
     *            the topupAndPay to set
     */
    public void setTopupAndPay(Boolean topupAndPay) {
        this.topupAndPay = topupAndPay;
    }

    /**
     * @return the payerAccountNo
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * @param payerAccountNo
     *            the payerAccountNo to set
     */
    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    /**
     * @return the cardCacheToken
     */
    public String getCardCacheToken() {
        return cardCacheToken;
    }

    /**
     * @param cardCacheToken
     *            the cardCacheToken to set
     */
    public void setCardCacheToken(String cardCacheToken) {
        this.cardCacheToken = cardCacheToken;
    }

    /**
     * @return the saveChannelInfoAfterPay
     */
    public Boolean getSaveChannelInfoAfterPay() {
        return saveChannelInfoAfterPay;
    }

    /**
     * @param saveChannelInfoAfterPay
     *            the saveChannelInfoAfterPay to set
     */
    public void setSaveChannelInfoAfterPay(Boolean saveChannelInfoAfterPay) {
        this.saveChannelInfoAfterPay = saveChannelInfoAfterPay;
    }

    /**
     * @return the channelInfo
     */
    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    /**
     * @param channelInfo
     *            the channelInfo to set
     */
    public void setChannelInfo(Map<String, String> channelInfo) {
        this.channelInfo = channelInfo;
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

    public String getIssuingCountry() {
        return issuingCountry;
    }

    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CashierPayOptionBill [payOption=").append(payOption).append(", payMethod=").append(payMethod)
                .append(", transAmount=").append(transAmount).append(", chargeAmount=").append(chargeAmount)
                .append(", topupAndPay=").append(topupAndPay).append(", payerAccountNo=").append(payerAccountNo)
                .append(", cardCacheToken=").append(cardCacheToken).append(", saveChannelInfoAfterPay=")
                .append(saveChannelInfoAfterPay).append(", channelInfo=").append(channelInfo).append(", extendInfo=")
                .append(extendInfo).append(", issuingCountry=").append(issuingCountry).append("]");
        return builder.toString();
    }

}