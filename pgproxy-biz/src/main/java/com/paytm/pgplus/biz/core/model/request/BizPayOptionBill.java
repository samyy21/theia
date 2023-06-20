package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.biz.enums.ECurrency;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.model.DccPaymentInfo;
import com.paytm.pgplus.common.model.TpvInfo;
import com.paytm.pgplus.common.model.VanInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BizPayOptionBill implements Serializable {

    private static final long serialVersionUID = 1L;

    private String payOption;
    private EPayMethod payMethod;
    private String transAmount;
    private String chargeAmount;
    private final ECurrency currency = ECurrency.INR;
    private boolean topupAndPay;
    private String payerAccountNo;
    private String cardCacheToken;
    private boolean saveChannelInfoAfterPay;
    private Map<String, String> channelInfo;
    private Map<String, String> extendInfo;
    private String payMode;
    private List<String> templateIds;
    private String virtualPaymentAddr;
    private String settleType;
    private boolean isDeepLinkFlow;

    /*
     * To save asset after successful payment on the basis of merchant consent
     */
    private boolean saveAssetForMerchant;

    /*
     * To save asset after successful payment for non-loggedIn user
     */
    private boolean saveCardAfterPay;

    /*
     * To save asset after successful payment for loggedIn user
     */
    private boolean saveAssetForUser;

    private boolean prepaidCard;
    private Map<String, String> feeRateFactorsInfo;
    private DccPaymentInfo dccPaymentInfo;
    /**
     * VanInfo for checout flow
     */
    private VanInfo vanInfo;
    private List<TpvInfo> tpvInfos;
    private String payConfirmFlowType;
    private String lastFourDigits;
    private boolean upiCC;

    public BizPayOptionBill() {

    }

    public BizPayOptionBill(final String payOption, final EPayMethod payMethod, final String transAmount,
            final String chargeAmount) {
        this.payOption = payOption;
        this.payMethod = payMethod;
        this.transAmount = transAmount;
        this.chargeAmount = chargeAmount;
    }

    public BizPayOptionBill(final String payOption, final EPayMethod payMethod, final String transAmount,
            final String chargeAmount, final Map<String, String> channelInfo, final Map<String, String> extendInfo,
            String cardCacheToken) {
        this.payOption = payOption;
        this.payMethod = payMethod;
        this.transAmount = transAmount;
        this.chargeAmount = chargeAmount;
        this.channelInfo = channelInfo;
        this.extendInfo = extendInfo;
        this.cardCacheToken = cardCacheToken;
    }

    public BizPayOptionBill(final String payOption, final EPayMethod payMethod, final String transAmount,
            final String chargeAmount, final Map<String, String> channelInfo, final Map<String, String> extendInfo,
            final VanInfo vanInfo, final List<TpvInfo> tpvInfos, String cardCacheToken) {
        this.payOption = payOption;
        this.payMethod = payMethod;
        this.transAmount = transAmount;
        this.chargeAmount = chargeAmount;
        this.channelInfo = channelInfo;
        this.extendInfo = extendInfo;
        this.vanInfo = vanInfo;
        this.tpvInfos = tpvInfos;
        this.cardCacheToken = cardCacheToken;
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

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public boolean isTopupAndPay() {
        return topupAndPay;
    }

    public void setTopupAndPay(boolean topupAndPay) {
        this.topupAndPay = topupAndPay;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getCardCacheToken() {
        return cardCacheToken;
    }

    public void setCardCacheToken(String cardCacheToken) {
        this.cardCacheToken = cardCacheToken;
    }

    public boolean isSaveChannelInfoAfterPay() {
        return saveChannelInfoAfterPay;
    }

    public void setSaveChannelInfoAfterPay(boolean saveChannelInfoAfterPay) {
        this.saveChannelInfoAfterPay = saveChannelInfoAfterPay;
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

    public ECurrency getCurrency() {
        return currency;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public List<String> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(List<String> templateIds) {
        this.templateIds = templateIds;
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

    public String getVirtualPaymentAddr() {
        return virtualPaymentAddr;
    }

    public void setVirtualPaymentAddr(String virtualPaymentAddr) {
        this.virtualPaymentAddr = virtualPaymentAddr;
    }

    public boolean isSaveAssetForMerchant() {
        return saveAssetForMerchant;
    }

    public void setSaveAssetForMerchant(boolean saveAssetForMerchant) {
        this.saveAssetForMerchant = saveAssetForMerchant;
    }

    public boolean isSaveCardAfterPay() {
        return saveCardAfterPay;
    }

    public void setSaveCardAfterPay(boolean saveCardAfterPay) {
        this.saveCardAfterPay = saveCardAfterPay;
    }

    public boolean isSaveAssetForUser() {
        return saveAssetForUser;
    }

    public void setsaveAssetForUser(boolean saveAssetForUser) {
        this.saveAssetForUser = saveAssetForUser;
    }

    public DccPaymentInfo getDccPaymentInfo() {
        return dccPaymentInfo;
    }

    public void setDccPaymentInfo(DccPaymentInfo dccPaymentInfo) {
        this.dccPaymentInfo = dccPaymentInfo;
    }

    public VanInfo getVanInfo() {
        return vanInfo;
    }

    public void setVanInfo(VanInfo vanInfo) {
        this.vanInfo = vanInfo;
    }

    public List<TpvInfo> getTpvInfos() {
        return tpvInfos;
    }

    public void setTpvInfos(List<TpvInfo> tpvInfos) {
        this.tpvInfos = tpvInfos;
    }

    public String getPayConfirmFlowType() {
        return payConfirmFlowType;
    }

    public void setPayConfirmFlowType(String payConfirmFlowType) {
        this.payConfirmFlowType = payConfirmFlowType;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getSettleType() {
        return settleType;
    }

    public void setSettleType(String settleType) {
        this.settleType = settleType;
    }

    public boolean isDeepLinkFlow() {
        return isDeepLinkFlow;
    }

    public void setDeepLinkFlow(boolean deepLinkFlow) {
        isDeepLinkFlow = deepLinkFlow;
    }

    public boolean isUpiCC() {
        return upiCC;
    }

    public void setUpiCC(boolean upiCC) {
        this.upiCC = upiCC;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PayOptionBill [payOption=").append(payOption).append(", payMethod=").append(payMethod)
                .append(", transAmount=").append(transAmount).append(", chargeAmount=").append(chargeAmount)
                .append(", topupAndPay=").append(topupAndPay).append(", payerAccountNo=").append(payerAccountNo)
                .append(", cardCacheToken=").append(cardCacheToken).append(", payMode=").append(payMode)
                .append(", saveChannelInfoAfterPay=").append(saveChannelInfoAfterPay).append(", extendInfo=")
                .append(extendInfo).append(", templateIds=").append(templateIds).append(", virtualPaymentAddr=")
                .append(virtualPaymentAddr).append("]").append(", saveAssetForMerchant=").append(saveAssetForMerchant)
                .append(", saveCardAfterPay=").append(saveCardAfterPay).append(", saveAssetForUser=")
                .append(saveAssetForUser).append(", prepaidCard=").append(prepaidCard).append(", feeRateFactorsInfo=")
                .append(feeRateFactorsInfo).append(", dccPaymentInfo=").append(dccPaymentInfo)
                .append(",payConfirmFlowType=").append(payConfirmFlowType).append(", lastFourDigits=")
                .append(lastFourDigits).append(",settleType=").append(settleType).append(",isDeepLinkFlow=")
                .append(isDeepLinkFlow).append(",upiCC=").append(upiCC).append("]");
        return builder.toString();
    }

}
