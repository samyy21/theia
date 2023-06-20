/**
 * 
 */
package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author amit.dubey
 *
 */
public class BaseCashierPayOptionBill implements Serializable {
    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 133289841878356349L;

    private String payerAccountNo;
    private String cardCacheToken;
    private Boolean saveChannelInfoAfterPay;
    private Map<String, String> channelInfo;
    private Map<String, String> extendInfo;

    /**
     * @param payerAccountNo
     * @param cardCacheToken
     * @param saveChannelInfoAfterPay
     * @param channelInfo
     * @param extendInfo
     */
    public BaseCashierPayOptionBill(String payerAccountNo, String cardCacheToken, Boolean saveChannelInfoAfterPay,
            Map<String, String> channelInfo, Map<String, String> extendInfo) {
        this.payerAccountNo = payerAccountNo;
        this.cardCacheToken = cardCacheToken;
        this.saveChannelInfoAfterPay = saveChannelInfoAfterPay;
        this.channelInfo = channelInfo;
        this.extendInfo = extendInfo;
    }

    /**
     * @return the payerAccountNo
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * @return the cardCacheToken
     */
    public String getCardCacheToken() {
        return cardCacheToken;
    }

    /**
     * @return the saveChannelInfoAfterPay
     */
    public Boolean getSaveChannelInfoAfterPay() {
        return saveChannelInfoAfterPay;
    }

    /**
     * @return the channelInfo
     */
    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }
}
