package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.EPayMethod;

import java.io.Serializable;
import java.util.Map;

public class BizAoaPayOptionBill implements Serializable {

    private static final long serialVersionUID = 162538489L;

    private String payOption;
    private EPayMethod payMethod;
    private String transAmount;
    private String cardCacheToken;
    private Map<String, String> channelInfo;
    private Map<String, String> extendInfo;
    private boolean prepaidCard;
    private Map<String, String> feeRateFactorsInfo;

    public BizAoaPayOptionBill(String payOption, EPayMethod payMethod, String transAmount, String cardCacheToken,
            Map<String, String> channelInfo, Map<String, String> extendInfo) {
        this.payOption = payOption;
        this.payMethod = payMethod;
        this.transAmount = transAmount;
        this.cardCacheToken = cardCacheToken;
        this.channelInfo = channelInfo;
        this.extendInfo = extendInfo;
    }

    public String getPayOption() {
        return payOption;
    }

    public void setPayOption(String payOption) {
        this.payOption = payOption;
    }

    public EPayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(EPayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(String transAmount) {
        this.transAmount = transAmount;
    }

    public String getCardCacheToken() {
        return cardCacheToken;
    }

    public void setCardCacheToken(String cardCacheToken) {
        this.cardCacheToken = cardCacheToken;
    }

    public Map<String, String> getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(Map<String, String> channelInfo) {
        this.channelInfo = channelInfo;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public Map<String, String> getFeeRateFactorsInfo() {
        return feeRateFactorsInfo;
    }

    public void setFeeRateFactorsInfo(Map<String, String> feeRateFactorsInfo) {
        this.feeRateFactorsInfo = feeRateFactorsInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizAoaPayOptionBill{");
        sb.append("payOption='").append(payOption).append('\'');
        sb.append(", payMethod=").append(payMethod);
        sb.append(", transAmount='").append(transAmount).append('\'');
        sb.append(", cardCacheToken='").append(cardCacheToken).append('\'');
        sb.append(", channelInfo=").append(channelInfo);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append(", prepaidCard=").append(prepaidCard);
        sb.append(", feeRateFactorsInfo=").append(feeRateFactorsInfo);
        sb.append('}');
        return sb.toString();
    }
}
